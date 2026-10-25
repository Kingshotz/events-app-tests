package ge.eventsapp.pages;

import com.microsoft.playwright.Dialog;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import ge.eventsapp.config.TestConfig;

import java.util.regex.Pattern;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

public class AdminUsersPage {
    private static final int MAX_PAGES = 100;

    private final Page page;

    public AdminUsersPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate(TestConfig.BASE_URL + "/admin/users");
    }

    private static Pattern exactText(String text) {
        return Pattern.compile("^\\s*" + Pattern.quote(text) + "\\s*$");
    }

    private Locator rowForUsername(String username) {
        // Seen on the real page: <td class="ps-4 fw-semibold">{username}</td> is the first cell of the row.
        // Exact match on that cell, so "admin" does not match "admin2".
        return page.locator("tbody tr").filter(new Locator.FilterOptions()
                .setHas(page.locator("td:nth-child(1)", new Page.LocatorOptions().setHasText(exactText(username)))));
    }

    /**
     * Buttons in the Action column have a Bootstrap icon whose CSS ::before glyph becomes part of the
     * accessible name, so the name is matched on the visible text (textContent) instead of an anchored name.
     */
    private Locator actionButton(Locator row, String label) {
        return row.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(exactText(label)));
    }

    public Locator findUserRow(String username) {
        Locator row = rowForUsername(username);
        if (row.count() > 0) {
            return row.first();
        }

        // Start from the first page and follow the "next" (bi-chevron-right) link seen on the real page.
        navigate();
        for (int i = 0; i < MAX_PAGES; i++) {
            if (row.count() > 0) {
                return row.first();
            }
            Locator nextItem = page.locator(".pagination li.page-item:has(i.bi-chevron-right)");
            if (nextItem.count() == 0) {
                break;
            }
            String classAttr = nextItem.first().getAttribute("class");
            if (classAttr != null && classAttr.contains("disabled")) {
                break;
            }
            String href = nextItem.first().locator("a.page-link").getAttribute("href");
            page.navigate(TestConfig.BASE_URL + href);
        }

        throw new IllegalStateException("User '" + username + "' was not found on any page of /admin/users");
    }

    public Locator getStatusBadge(String username) {
        return findUserRow(username).locator("td:nth-child(4) span.badge");
    }

    public String getUserStatus(String username) {
        return getStatusBadge(username).innerText().trim();
    }

    public void banUser(String username) {
        Locator banButton = actionButton(findUserRow(username), "Ban");
        // Native confirm("Ban this user?") handler registered BEFORE the click
        page.onceDialog(Dialog::accept);
        // The button submits a form; wait for the resulting page load before reading the table again
        page.waitForNavigation(banButton::click);
        assertThat(getStatusBadge(username)).hasText("Banned");
    }

    public void unbanUser(String username) {
        Locator unbanButton = actionButton(findUserRow(username), "Unban");
        page.waitForNavigation(unbanButton::click);
        assertThat(getStatusBadge(username)).hasText("Active");
    }

    public boolean isBanButtonVisible(String username) {
        Locator banBtn = actionButton(findUserRow(username), "Ban");
        return banBtn.count() > 0 && banBtn.first().isVisible();
    }

    public boolean isUnbanButtonVisible(String username) {
        Locator unbanBtn = actionButton(findUserRow(username), "Unban");
        return unbanBtn.count() > 0 && unbanBtn.first().isVisible();
    }

    public boolean isSelfLabelVisible(String username) {
        Locator youLabel = findUserRow(username).locator("text=(you)");
        return youLabel.count() > 0 && youLabel.first().isVisible();
    }
}
