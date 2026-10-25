package ge.eventsapp.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;

import java.net.URI;
import java.util.regex.Pattern;

public class EventsPage {
    private final Page page;

    public EventsPage(Page page) {
        this.page = page;
    }

    private static Pattern exactText(String text) {
        return Pattern.compile("^\\s*" + Pattern.quote(text) + "\\s*$");
    }

    public String getUrlPath() {
        return URI.create(page.url()).getPath();
    }

    /**
     * Seen on the real /events page: logged-in navbar has
     * <form action="/logout"><button><i class="bi bi-box-arrow-right"></i>Logout</button></form>.
     * Anonymous visitors see "Login" / "Register" instead, so this proves the logged-in state.
     * Matched on visible text because the icon's CSS ::before glyph is part of the accessible name.
     */
    public Locator getLogoutButton() {
        return page.getByRole(AriaRole.BUTTON)
                .filter(new Locator.FilterOptions().setHasText(exactText("Logout")));
    }

    /**
     * Seen on the real /events page: <a href="/profile"><i class="bi bi-person-circle"></i><span>{username}</span></a>.
     */
    public Locator getUserProfileLink(String username) {
        return page.getByRole(AriaRole.LINK)
                .filter(new Locator.FilterOptions().setHasText(exactText(username)));
    }
}
