# Organizer ვერ წაშლის ჩუმად Published Event-ს, რომელსაც მონაწილეები ჰყავს

| Field | Value |
|---|---|
| Key | TE-T21 |
| Objective | Organizer ცდილობს წაშალოს Published Event, რომელშიც Participant უკვე ჩაეწერა — ვებსაიტიდანაც და REST API-დანაც. სცენარი ამოწმებს, რომ წაშლა უარყოფილია, Event და მისი მონაწილეები უცვლელი რჩება და Participant კვლავ ხედავს, რომ Event-ზე მიდის (Event-ის გაუქმება უნდა მოხდეს Cancel-ით, რომელიც მონაწილეებს აცნობებს). |
| Type | mixed |
| Importance | Critical — მონაწილეებიანი Published Event არ უნდა გაქრეს მონაწილეებისთვის შეტყობინების გარეშე |
| Labels | mixed, negative, event-lifecycle |
| Language | ka |
| Covers | POST /events/{id}/delete, DELETE /api/v1/event/{id}, GET /api/v1/public/event/{id} |
| Steps count | 8 |

## Precondition

- `{env.base_url}` (ვებსაიტი და REST API) ხელმისაწვდომია.
- მონაცემთა ბაზაზე წაკითხვის წვდომა (ცხრილები `event`, `events_participants`).
- წინასწარ არსებული მონაცემები არ არის საჭირო — ყველაფერი იქმნება ნაბიჯებში.
- შენიშვნა: სცენარი ამოწმებს, რომ მონაწილეებიანი Published Event-ის წაშლა აკრძალულია; ასეთი Event უნდა გაუქმდეს "Cancel event"-ით, რომელიც მონაწილეებს აცნობებს.

## Test data

| Placeholder | Meaning | Origin |
|---|---|---|
| `{random_owner_username}` | Organizer-ის username, 3–24 სიმბოლო | generated |
| `{random_owner_email}` | Organizer-ის email | generated |
| `{random_attendee_username}` | Participant-ის username | generated |
| `{random_attendee_email}` | Participant-ის email | generated |
| `{random_password}` | პაროლი, მინიმუმ 6 სიმბოლო; ერთი და იგივე ორივე ანგარიშისთვის | generated |
| `{random_org_name}` | Organization-ის უნიკალური სახელი | generated |
| `{random_event_name}` | Event-ის უნიკალური სახელი | generated |
| `{random_future_start}` | დაწყების დრო, ფორმატი `yyyy-MM-dd HH:mm:ss` (UTC), მინიმუმ 2 დღით მომავალში | generated |
| `{random_future_end}` | დასრულების დრო, `{random_future_start}` + 2 საათი | generated |
| `{step1.ownerId}` | Organizer-ის id | step 1 |
| `{step2.orgId}` | Organization-ის id | step 2 |
| `{step3.eventId}` | Event-ის id | step 3 |
| `{step4.attendeeId}` | Participant-ის id | step 4 |

## Steps

| # | Step | Description | Test data | Expected result |
|---|---|---|---|---|
| 1 | Organizer-ის რეგისტრაცია | გაგზავნეთ `POST /api/v1/public/user`. | {"username":"{random_owner_username}","email":"{random_owner_email}","password":"{random_password}","roles":["ROLE_ORGANIZATION_OWNER"]} | `201 Created`. **Save:** ownerId = body.id |
| 2 | Organization-ის შექმნა | გაგზავნეთ `POST /api/v1/organization` Basic auth-ით. | Basic auth: {random_owner_username} / {random_password}<br>Body: {"name":"{random_org_name}"} | `201 Created`. **Save:** orgId = body.id |
| 3 | Published Event-ის შექმნა | გაგზავნეთ `POST /api/v1/event` Basic auth-ით. | Basic auth: {random_owner_username} / {random_password}<br>Body: {"name":"{random_event_name}","startTime":"{random_future_start}","endTime":"{random_future_end}","schedule":"Doors open 30 min before","cityLocation":"Tbilisi","streetLocation":"Rustaveli Ave 1","organizationId":{step2.orgId},"creatorId":{step1.ownerId},"categories":["Music"],"maxCapacity":10,"status":"PUBLISHED"} | `201 Created`; body.status = "PUBLISHED". **Save:** eventId = body.id |
| 4 | Participant-ის რეგისტრაცია | გაგზავნეთ `POST /api/v1/public/user`. | {"username":"{random_attendee_username}","email":"{random_attendee_email}","password":"{random_password}","roles":["ROLE_USER"]} | `201 Created`. **Save:** attendeeId = body.id |
| 5 | Event-ში შესვლა | `/login`-ზე შედით Participant-ის მონაცემებით, გახსენით `/events/{step3.eventId}` და დააჭირეთ "Join Event". | Username: {random_attendee_username}<br>Password: {random_password} | ჩანს ბეჯი "You're attending"; მონაწილეები "1 / 10". |
| 6 | წაშლა ვებსაიტიდან | სხვა სესიაში `/login`-ზე შედით Organizer-ის მონაცემებით, გახსენით `/events/{step3.eventId}`, დააჭირეთ "Delete" და ფანჯარაში "Delete Event" დაადასტურეთ "Delete". | Username: {random_owner_username}<br>Password: {random_password} | წაშლა უარყოფილია: Organizer რჩება `/events/{step3.eventId}`-ზე და ჩანს შეცდომის შეტყობინება (Event-ს მონაწილეები ჰყავს — გამოიყენეთ "Cancel event"); Event-ის გვერდზე არ ჩანს Draft/Cancelled/Completed ბეჯი (Published სტატუსს ბეჯი არ აქვს). |
| 7 | წაშლა REST API-ით | გაგზავნეთ `DELETE /api/v1/event/{step3.eventId}` Organizer-ის სახელით. | Basic auth: {random_owner_username} / {random_password} | `409 Conflict`; body.message არ არის ცარიელი; `204 No Content` არ ბრუნდება. |
| 8 | Event და მონაწილე უცვლელია | გაგზავნეთ `GET /api/v1/public/event/{step3.eventId}`; შეამოწმეთ `events_participants`; Participant-ის სესიაში განაახლეთ `/events/{step3.eventId}`. | events_participants.event_id = {step3.eventId} | `200 OK`; body.status = "PUBLISHED"; body.name = "{random_event_name}"; `events_participants`-ში 1 ჩანაწერია user_id = {step4.attendeeId}; Participant-ის გვერდზე კვლავ ჩანს "You're attending" და "1 / 10". |
