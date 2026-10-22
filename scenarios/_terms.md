# Terms kept untranslated

Scenarios written in Georgian keep these words exactly as below (see the `language-policy` reference of the
scenario-kit skill). Endpoints, HTTP codes, JSON fields, enum values, table/column names, UI labels and
placeholders are always kept and need not be listed here. Add or remove rows freely.

| Term | Note |
|---|---|
| Event | a single occurrence with start/end time, location, status |
| Organization | group that publishes events; one owner |
| Organizer | owner of the event's organization |
| Participant | user who joined an event |
| Capacity | `maxCapacity`, optional participant limit |
| Waitlist | FIFO queue for a full event |
| Draft | `DRAFT` event, visible only to its organizer |
| Published | `PUBLISHED` event |
| Cancelled | `CANCELLED` event, final |
| Completed | derived display status of an ended published event |
| Check-in | marking a participant as present |
| QR code | encodes the personal check-in URL |
| Reminder | `DAY_BEFORE` / `HOUR_BEFORE` message |
| Subscription | following a category or an organization |
| Category | event tag |
| Notification | in-app message for one user |
| Favorite | private bookmark on an event |
| Reaction | like/dislike on a comment |
| Rating | 1–5 score for an organization |
| Ban | admin disables a user (`enabled = false`) |
| Email | |
| Token | password reset or check-in token |
