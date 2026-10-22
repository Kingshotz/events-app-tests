# Admin ბლოკავს მომხმარებელს, რომელიც კარგავს წვდომას, ხოლო Unban წვდომას აღადგენს

| Field | Value |
|---|---|
| Key | TE-T20 |
| Objective | Admin `/admin/users` გვერდზე ბლოკავს (Ban) მომხმარებელს. სცენარი ამოწმებს, რომ დაბლოკილი მომხმარებელი ვეღარ შედის ვებსაიტზე და ვერ იყენებს REST API-ს, მისი კონტენტი (კომენტარი) რჩება, Admin ვერ დაბლოკავს საკუთარ თავს, ხოლო Unban-ის შემდეგ წვდომა სრულად აღდგება. |
| Type | mixed |
| Importance | High — ავტორიზაცია: დაბლოკილმა მომხმარებელმა უნდა დაკარგოს წვდომა |
| Labels | mixed, positive, negative, administration, security |
| Language | ka |
| Covers | /admin/users, POST /admin/users/{id}/ban, POST /admin/users/{id}/unban, POST /api/v1/comment, GET /api/v1/public/comment |
| Steps count | 10 |

## Precondition

- `{env.base_url}` (ვებსაიტი და REST API) ხელმისაწვდომია.
- `{pre.admin_username}` — არსებული ანგარიში `ROLE_ADMIN` როლით, პაროლით `{env.admin_password}` (README-ში მითითებული სატესტო Admin: `admin` / `admin123`).
- `{pre.published_event_id}` — ნებისმიერი არსებული Published Event-ის id (მაგ. `GET /api/v1/public/event`-ის პასუხიდან).
- მონაცემთა ბაზაზე წაკითხვის წვდომა (ცხრილი `usr`).
- ყველა შემოწმება ხდება ახალ შესვლაზე ან ახალ Basic auth მოთხოვნაზე; Ban-მდე გახსნილი სესიის ქცევა ამ სცენარში არ მოწმდება.

## Test data

| Placeholder | Meaning | Origin |
|---|---|---|
| `{random_username}` | დასაბლოკი მომხმარებლის username, 3–24 სიმბოლო | generated |
| `{random_email}` | მისი email | generated |
| `{random_password}` | პაროლი, მინიმუმ 6 სიმბოლო | generated |
| `{random_comment_text}` | კომენტარის უნიკალური ტექსტი | generated |
| `{random_blocked_comment_text}` | კომენტარის ტექსტი Ban-ის დროს | generated |
| `{random_after_unban_text}` | კომენტარის ტექსტი Unban-ის შემდეგ | generated |
| `{pre.admin_username}` | არსებული Admin | precondition |
| `{pre.published_event_id}` | არსებული Published Event | precondition |
| `{env.admin_password}` | Admin-ის პაროლი | environment |
| `{step1.userId}` | მომხმარებლის id | step 1 |
| `{step2.commentId}` | Ban-მდე დაწერილი კომენტარის id | step 2 |

## Steps

| # | Step | Description | Test data | Expected result |
|---|---|---|---|---|
| 1 | მომხმარებლის რეგისტრაცია | გაგზავნეთ `POST /api/v1/public/user`. | {"username":"{random_username}","email":"{random_email}","password":"{random_password}","roles":["ROLE_USER"]} | `201 Created`; `usr`-ში enabled = true. **Save:** userId = body.id |
| 2 | კომენტარის დაწერა | გაგზავნეთ `POST /api/v1/comment?eventId={pre.published_event_id}` მომხმარებლის სახელით. | Basic auth: {random_username} / {random_password}<br>Body: {"text":"{random_comment_text}"} | `201 Created`; body.text = "{random_comment_text}"; body.author = "{random_username}". **Save:** commentId = body.id |
| 3 | მომხმარებლის დაბლოკვა | `/login`-ზე შედით Admin-ით, გახსენით `/admin/users`, იპოვეთ სტრიქონი "{random_username}" (საჭიროების შემთხვევაში გვერდებზე გადასვლით), დააჭირეთ "Ban" და დაადასტურეთ "Ban this user?". | Username: {pre.admin_username}<br>Password: {env.admin_password} | სტრიქონში სტატუსი იცვლება "Active"-დან "Banned"-ზე, ჩანს ღილაკი "Unban"; `usr`-ში id = {step1.userId}-ს enabled = false. |
| 4 | Admin ვერ ბლოკავს თავს | იმავე გვერდზე იპოვეთ სტრიქონი "{pre.admin_username}". | — | სტრიქონში სტატუსი "Active", ღილაკი "Ban" არ ჩანს, ნაცვლად ჩანს "(you)". |
| 5 | დაბლოკილის შესვლა ვებსაიტზე | სხვა სესიაში გახსენით `/login`, შეავსეთ "Username" და "Password" და დააჭირეთ "Sign In". | Username: {random_username}<br>Password: {random_password} | გადამისამართება `/login?error`-ზე; ჩანს "Invalid username or password. Please try again."; მომხმარებელი არ არის შესული. |
| 6 | დაბლოკილის REST მოთხოვნა | გაგზავნეთ `POST /api/v1/comment?eventId={pre.published_event_id}` მომხმარებლის სახელით. | Basic auth: {random_username} / {random_password}<br>Body: {"text":"{random_blocked_comment_text}"} | `401 Unauthorized`; კომენტარი "{random_blocked_comment_text}" არ შეიქმნა. |
| 7 | კონტენტი რჩება | გაგზავნეთ `GET /api/v1/public/comment?eventId={pre.published_event_id}` ავტორიზაციის გარეშე. | — | `200 OK`; პასუხში არის კომენტარი id = {step2.commentId}, text = "{random_comment_text}", author = "{random_username}"; ტექსტი "{random_blocked_comment_text}" არ არის. |
| 8 | ბლოკის მოხსნა | Admin-ის სესიაში `/admin/users`-ზე სტრიქონში "{random_username}" დააჭირეთ "Unban". | — | სტატუსი "Active", ჩანს ღილაკი "Ban"; `usr`-ში id = {step1.userId}-ს enabled = true. |
| 9 | შესვლა Unban-ის შემდეგ | `/login`-ზე შეავსეთ "Username" და "Password" და დააჭირეთ "Sign In". | Username: {random_username}<br>Password: {random_password} | გადამისამართება `/events`-ზე; მომხმარებელი შესულია. |
| 10 | REST Unban-ის შემდეგ | გაგზავნეთ `POST /api/v1/comment?eventId={pre.published_event_id}` მომხმარებლის სახელით. | Basic auth: {random_username} / {random_password}<br>Body: {"text":"{random_after_unban_text}"} | `201 Created`; body.text = "{random_after_unban_text}"; body.author = "{random_username}". |
