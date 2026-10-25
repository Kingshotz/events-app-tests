# PROMPTS.md

Key prompts sent to the agent, grouped by task, exact text. Paste them from the real sessions.

## Task 1 — S1
```
@[c:\Users\User\events-app-tests]  /plan
Automate scenario S1 from scenarios/s1-admin-bans-user.md (read the file yourself, keep the Georgian text as source of truth; terms are in scenarios/_terms.md).

Read AGENTS.md first and follow it.

Requirements:
- Before writing any locator, open the real pages with Playwright MCP (/login, /admin/users, including pagination) and use only roles, labels and test ids you actually saw. Tell me what you saw.
- One test class S1AdminBansUserTest in package ge.eventsapp.tests, one @Test per scenario step (10 tests), priority = step number.
- All locators live in page classes under ge.eventsapp.pages, never in the test class.
- API steps use rest-assured; database checks use a MyBatis mapper interface (H2 URL is in AGENTS.md).
- The "Ban this user?" prompt is a native browser confirm(); register the dialog handler before the click.
- /admin/users is paginated; the row for the generated user may not be on page 1.
- pre.published_event_id comes from GET /api/v1/public/event.
- Do not change or weaken any expected result from the scenario.
- Generate unique test data on every run.
Give me the plan first and do not write code until I approve.
```

### Task 1 — S1, follow-up (review fixes)
```
Read AGENTS.md first and follow it. Fix the following issues in the S1 automation. Do not change or weaken any expected result from scenarios/s1-admin-bans-user.md.

1. Step 9 (EventsPage): isLoaded() only checks that the URL contains "/events", which does not prove the user is logged in. Using Playwright MCP, log in as a normal user, open /events, and tell me what you actually saw. Then assert both: URL path is exactly /events, and an element that proves the logged-in state (use only something you really saw on the page). Do not invent any locator.

2. AdminUsersPage row lookup: findUserRow uses hasText(username), which is a substring match (e.g. "admin" can match "admin2"). Match the username cell (td:nth-child(1)) by exact text instead.

3. Ban/Unban button names: replace Pattern.compile("Ban") with an anchored pattern like ^\s*Ban\s*$ and ^\s*Unban\s*$.

4. Replace page.waitForLoadState() after clicks/navigation with Playwright auto-waiting assertions (for example wait for the status badge text to become "Banned" / "Active"), so the tests do not depend on load timing.

5. Test data: the password must be generated per run (random suffix), not the constant "P@ssword123". Move the admin credentials out of the test class into one config place (constants class or system properties with defaults admin/admin123).

6. Add this rule to AGENTS.md under "Rules earned from agent mistakes" and mirror it in AI_LOG.md: "After submitting a form, always wait for the resulting navigation with waitForURL before the next action."

7. Mutation check: temporarily break ONE expected result (for example in step 7 expect a wrong comment text, or in step 6 expect 200 instead of 401), run the test, confirm it goes red on exactly that step, then revert the change. Record what you changed and what failed in AI_LOG.md under "Mutation check".

8. Run: mvn test -Dtest=S1AdminBansUserTest | tee docs/s1-run.txt. It must be 10/10 green after the mutation is reverted. Update AI_LOG.md (Problems, Result with run count) and PROMPTS.md (add this prompt under Task 1).

Do not commit to main. Work only in the playwright-ai-automation branch.
```
Note: the agent ran out of quota after this prompt (run 3 red). The remaining fix was done with Claude; see AI_LOG.md.

## Task 2 — S2
```
/plan
<paste exact prompt>
```
