package ge.eventsapp.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import ge.eventsapp.config.TestConfig;

import java.util.regex.Pattern;

public class LoginPage {
    private final Page page;

    public LoginPage(Page page) {
        this.page = page;
    }

    public void navigate() {
        page.navigate(TestConfig.BASE_URL + "/login");
    }

    public void fillUsername(String username) {
        page.getByLabel("Username").fill(username);
    }

    public void fillPassword(String password) {
        page.getByLabel("Password").fill(password);
    }

    public void clickSignIn() {
        page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName(Pattern.compile("Sign In"))).click();
    }

    public void login(String username, String password) {
        navigate();
        fillUsername(username);
        fillPassword(password);
        clickSignIn();
        // Wait until redirected away from /login or to /login?error
        page.waitForURL(url -> !url.endsWith("/login"));
    }

    public Locator getErrorAlert() {
        return page.getByRole(AriaRole.ALERT);
    }

    public String getErrorAlertText() {
        return getErrorAlert().innerText().trim();
    }

    public boolean isErrorAlertVisible() {
        return getErrorAlert().isVisible();
    }

    public String getCurrentUrl() {
        return page.url();
    }
}
