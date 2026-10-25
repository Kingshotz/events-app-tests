# AGENTS.md

## Stack
- Java 17, Maven
- Playwright for Java (UI), TestNG (runner), AssertJ (assertions)
- rest-assured (API steps), MyBatis mapper interface over H2 (database checks)
- Scenarios live in `scenarios/` (Georgian text; terms in `scenarios/_terms.md` stay in English)

## Environment
- App under test: http://localhost:8081 (must be running in the background: `mvn spring-boot:run` in `events-test-app`)
- Swagger: http://localhost:8081/swagger-ui/index.html
- H2: `jdbc:h2:tcp://localhost:9092/eventsdb;MODE=MSSQLServer;CASE_INSENSITIVE_IDENTIFIERS=TRUE;SCHEMA=EVENTS_SCHEMA` (user `sa`, empty password)
- Seed admin: `admin` / `admin123`

## Project layout
- `src/test/java/ge/eventsapp/pages/` — page classes (all locators live here)
- `src/test/java/ge/eventsapp/api/` — rest-assured helpers
- `src/test/java/ge/eventsapp/db/` — MyBatis mapper interfaces and session factory
- `src/test/java/ge/eventsapp/tests/` — test classes: `S1AdminBansUserTest`, `S2OrganizerCannotDeletePublishedEventTest`
- `docs/` — test run output per test

## Run one test
- Whole class: `mvn test -Dtest=S1AdminBansUserTest`
- Single method: `mvn test -Dtest=S1AdminBansUserTest#step03_adminBansUser`
- Save output: `mvn test -Dtest=S1AdminBansUserTest | tee docs/s1-run.txt`

## Conventions
- One test class per scenario, one `@Test` per scenario step, `priority` = step number.
- Steps share state (ids from earlier steps), so use `dependsOnMethods` or ordered priority in a single class.
- Locators live only in page classes. Use roles, labels, test ids that were actually seen on the real page via Playwright MCP.
- API steps use rest-assured; database checks use a MyBatis mapper interface (no raw JDBC in tests).
- Test data is generated per run (unique suffix); never hardcode usernames, emails or names.
- Dates use UTC, format `yyyy-MM-dd HH:mm:ss`.
- Native `confirm()` dialogs are handled with Playwright `page.onDialog`, registered before the click.

## Workflow
- Start every task in a new session with `/plan`.
- Before writing any locator, open the real pages with Playwright MCP and note what is there.
- Run the test until green; if it cannot be green, write the verdict (TEST / ENVIRONMENT / PRODUCT DEFECT) with evidence in `AI_LOG.md`.

## Never
- Never weaken, remove or change an expected result from the scenario to make a test pass.
- Never invent a locator, role, label or test id that was not seen on the real page.
- Never put locators in test classes.
- Never put the MCP configuration in the global `~/.gemini`; it stays at project level in `.gemini/settings.json`.
- Never install other MCP servers, skills or plugins.
- Never commit to `main` directly; work in `playwright-ai-automation`.

## Rules earned from agent mistakes
- In table rows with multiple badges (e.g. roles and status), always qualify the status badge by column index (e.g. `td:nth-child(4)`) or specific classes rather than selecting the first badge in the row.
- After submitting a form (login, Ban, Unban), always wait for the resulting navigation (`waitForURL` / `waitForNavigation`) before the next action.
- Buttons and links with Bootstrap icons get the icon's CSS `::before` glyph in their accessible name. Do not anchor a `getByRole(..., name=...)` regex on them; use `getByRole(ROLE).filter(hasText = exact visible text)`.
- Every page an assertion depends on (e.g. `/events` for "user is logged in") must be opened with Playwright MCP before its locators are written.
