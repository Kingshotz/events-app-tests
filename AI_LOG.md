# AI_LOG.md

One section per session: problems, results, agent mistakes and the rules they earned.
Fill this in from the real sessions only.

## Session 1 — S1 (admin bans user)
- Date: 2026-10-22
- Tool / model: Antigravity / agy, Gemini 3.8 Flash; final fixes with Claude (claude-opus-5-5) after the Antigravity quota ran out
- Plan comment I added before Proceed: i approve and go write tests
- Pages the agent opened with Playwright MCP: /login, /login?error, /admin/users (including pagination). /events was NOT opened by the agent; it was checked later in the fix pass (see below).
- Problems:
  - Run 1: in `LoginPage.login()`, `clickSignIn()` triggered navigation without awaiting `page.waitForURL(...)`, so the next navigation to `/admin/users` raced the login.
  - Run 1: in `AdminUsersPage.getUserStatus()`, `row.locator("span.badge").first()` picked the role badge (`ROLE_USER`, column 3) instead of the status badge (`Active`/`Banned`, column 4).
  - Run 2 (21:02): 10/10 green, but review found weak spots: step 9 only checked that the URL contains `/events` (the page is public, so this does not prove login); row lookup used a substring match (`"admin"` can match `"admin2"`); fixed constant password; admin credentials in the test class.
  - Run 3 (21:18): after the review fixes, red. `step03_adminBansUser` timed out after 30 s waiting for `getByRole(BUTTON, name=/^\s*Ban\s*$/)`; steps 4–10 skipped. See `docs/s1-run.txt`.
  - Root cause (checked on the real page): the user row existed on page 2 with a visible "Ban" button. The button contains `<i class="bi bi-slash-circle">`, whose CSS `::before` glyph (U+F567) becomes part of the accessible name, so an anchored name regex never matches. The same applies to "Unban" and the navbar "Logout" button.
  - Fix pass: Action/Logout buttons are now matched with `getByRole(BUTTON).filter(hasText = exact visible text)`; the row is matched by exact text of the first cell; `findUserRow` follows the real "next" link href and throws `User 'x' was not found on any page of /admin/users` instead of returning a missing locator; Ban/Unban wait for the form-submit navigation before checking the badge. Step 9 now checks the Logout button and the `/profile` link with the username, both seen on the real logged-in `/events` page (anonymous visitors see "Login" / "Register" instead).
- Agent mistakes -> rule added to AGENTS.md:
  - Status badge must be qualified by column (`td:nth-child(4)`), not the first badge in the row.
  - After submitting a form, always wait for the resulting navigation (`waitForURL` / `waitForNavigation`) before the next action.
  - Do not anchor accessible-name regexes on elements that contain icon fonts; match the visible text with `filter(hasText)` instead.
  - Verify every page used by an assertion (here `/events`) with Playwright MCP before writing its locators.
- Result (green / red, run count): run 1 red, run 2 green, run 3 red (TEST defect, see above). Run 4 after the fix pass: <fill in: green/red>
- Mutation check: <fill in after run 4: in step 6 change `isEqualTo(401)` to `isEqualTo(200)`, run, confirm only step 6 (and the steps depending on it) go red, revert>
- Verdict if not green (TEST / ENVIRONMENT / PRODUCT DEFECT) + evidence: run 3 was a TEST defect (locator), not a product defect: on the real page the banned-to-be user row and its "Ban" button were present and correct.

## Session 2 — S2 (organizer cannot delete published event)
- Date:
- Tool / model: Antigravity / agy, Gemini Flash
- Plan comment I added before Proceed:
- Pages the agent opened with Playwright MCP:
- Problems:
- Agent mistakes -> rule added to AGENTS.md:
- Result (green / red, run count):
- Verdict if not green (TEST / ENVIRONMENT / PRODUCT DEFECT) + evidence:
