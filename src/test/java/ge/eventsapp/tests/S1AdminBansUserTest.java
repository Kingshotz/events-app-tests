package ge.eventsapp.tests;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import ge.eventsapp.api.ApiClient;
import ge.eventsapp.config.TestConfig;
import ge.eventsapp.db.MyBatisUtil;
import ge.eventsapp.db.UserMapper;
import ge.eventsapp.pages.AdminUsersPage;
import ge.eventsapp.pages.EventsPage;
import ge.eventsapp.pages.LoginPage;
import io.restassured.response.Response;
import org.apache.ibatis.session.SqlSession;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

public class S1AdminBansUserTest {

    private Playwright playwright;
    private Browser browser;
    private BrowserContext adminContext;
    private Page adminPage;
    private LoginPage adminLoginPage;
    private AdminUsersPage adminUsersPage;

    private BrowserContext userContext;
    private Page userPage;
    private LoginPage userLoginPage;
    private EventsPage userEventsPage;

    // Test Data (unique per run)
    private String randomUsername;
    private String randomEmail;
    private String randomPassword;
    private String randomCommentText;
    private String randomBlockedCommentText;
    private String randomAfterUnbanText;

    private Long publishedEventId;
    private Long userId;
    private Long commentId;

    @BeforeClass
    public void setup() {
        // Precondition: published event ID from GET /api/v1/public/event
        publishedEventId = ApiClient.getPublishedEventId();

        // Unique test data generated per run
        long timestamp = System.currentTimeMillis();
        String suffix = String.valueOf(timestamp % 1000000000L);
        randomUsername = "usr_" + suffix;
        randomEmail = "usr_" + suffix + "@events.local";
        // Password generated with random suffix per run
        randomPassword = "P@ss_" + UUID.randomUUID().toString().substring(0, 8);
        randomCommentText = "Comment before ban " + UUID.randomUUID();
        randomBlockedCommentText = "Comment during ban " + UUID.randomUUID();
        randomAfterUnbanText = "Comment after unban " + UUID.randomUUID();

        // Playwright Browser and Contexts
        playwright = Playwright.create();
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));

        adminContext = browser.newContext();
        adminPage = adminContext.newPage();
        adminLoginPage = new LoginPage(adminPage);
        adminUsersPage = new AdminUsersPage(adminPage);

        userContext = browser.newContext();
        userPage = userContext.newPage();
        userLoginPage = new LoginPage(userPage);
        userEventsPage = new EventsPage(userPage);
    }

    @AfterClass(alwaysRun = true)
    public void tearDown() {
        if (adminContext != null) adminContext.close();
        if (userContext != null) userContext.close();
        if (browser != null) browser.close();
        if (playwright != null) playwright.close();
    }

    private Boolean checkUserEnabledInDb(Long uid) {
        try (SqlSession session = MyBatisUtil.getSqlSessionFactory().openSession()) {
            UserMapper mapper = session.getMapper(UserMapper.class);
            return mapper.isUserEnabled(uid);
        }
    }

    @Test(priority = 1, description = "1. მომხმარებლის რეგისტრაცია")
    public void step01_registerUser() {
        Response response = ApiClient.registerUser(randomUsername, randomEmail, randomPassword);

        assertThat(response.statusCode()).isEqualTo(201);
        userId = response.jsonPath().getLong("id");
        assertThat(userId).isNotNull();

        Boolean enabled = checkUserEnabledInDb(userId);
        assertThat(enabled).isTrue();
    }

    @Test(priority = 2, dependsOnMethods = "step01_registerUser", description = "2. კომენტარის დაწერა")
    public void step02_postComment() {
        Response response = ApiClient.postComment(randomUsername, randomPassword, publishedEventId, randomCommentText);

        assertThat(response.statusCode()).isEqualTo(201);
        commentId = response.jsonPath().getLong("id");
        assertThat(commentId).isNotNull();
        assertThat(response.jsonPath().getString("text")).isEqualTo(randomCommentText);
        assertThat(response.jsonPath().getString("author")).isEqualTo(randomUsername);
    }

    @Test(priority = 3, dependsOnMethods = "step02_postComment", description = "3. მომხმარებლის დაბლოკვა")
    public void step03_adminBansUser() {
        // Log in as Admin using config credentials
        adminLoginPage.login(TestConfig.ADMIN_USERNAME, TestConfig.ADMIN_PASSWORD);

        // Open /admin/users
        adminUsersPage.navigate();

        // Verify initial status
        String initialStatus = adminUsersPage.getUserStatus(randomUsername);
        assertThat(initialStatus).isEqualTo("Active");

        // Ban user with dialog confirmation and auto-waiting assertion on status badge
        adminUsersPage.banUser(randomUsername);

        // Verify UI status changes to Banned and Unban button is visible
        String updatedStatus = adminUsersPage.getUserStatus(randomUsername);
        assertThat(updatedStatus).isEqualTo("Banned");
        assertThat(adminUsersPage.isUnbanButtonVisible(randomUsername)).isTrue();

        // Verify database: enabled = false
        Boolean enabled = checkUserEnabledInDb(userId);
        assertThat(enabled).isFalse();
    }

    @Test(priority = 4, dependsOnMethods = "step03_adminBansUser", description = "4. Admin ვერ ბლოკავს თავს")
    public void step04_adminCannotBanSelf() {
        adminUsersPage.navigate();

        String status = adminUsersPage.getUserStatus(TestConfig.ADMIN_USERNAME);
        assertThat(status).isEqualTo("Active");

        assertThat(adminUsersPage.isBanButtonVisible(TestConfig.ADMIN_USERNAME)).isFalse();
        assertThat(adminUsersPage.isSelfLabelVisible(TestConfig.ADMIN_USERNAME)).isTrue();
    }

    @Test(priority = 5, dependsOnMethods = "step03_adminBansUser", description = "5. დაბლოკილის შესვლა ვებსაიტზე")
    public void step05_bannedUserLoginAttempt() {
        userLoginPage.login(randomUsername, randomPassword);

        assertThat(userLoginPage.getCurrentUrl()).contains("/login?error");
        assertThat(userLoginPage.isErrorAlertVisible()).isTrue();
        assertThat(userLoginPage.getErrorAlertText()).contains("Invalid username or password. Please try again.");
    }

    @Test(priority = 6, dependsOnMethods = "step03_adminBansUser", description = "6. დაბლოკილის REST მოთხოვნა")
    public void step06_bannedUserRestRequest() {
        Response response = ApiClient.postComment(randomUsername, randomPassword, publishedEventId, randomBlockedCommentText);

        assertThat(response.statusCode()).isEqualTo(401);
    }

    @Test(priority = 7, dependsOnMethods = {"step02_postComment", "step06_bannedUserRestRequest"}, description = "7. კონტენტი რჩება")
    public void step07_contentRemains() {
        Response response = ApiClient.getPublicComments(publishedEventId);

        assertThat(response.statusCode()).isEqualTo(200);

        List<Map<String, Object>> comments = response.jsonPath().getList("$");

        // Comment from step 2 must remain
        Map<String, Object> originalComment = comments.stream()
                .filter(c -> commentId.equals(((Number) c.get("id")).longValue()))
                .findFirst()
                .orElse(null);

        assertThat(originalComment).isNotNull();
        assertThat(originalComment.get("text")).isEqualTo(randomCommentText);
        assertThat(originalComment.get("author")).isEqualTo(randomUsername);

        // Blocked comment from step 6 must not exist
        boolean blockedCommentExists = comments.stream()
                .anyMatch(c -> randomBlockedCommentText.equals(c.get("text")));
        assertThat(blockedCommentExists).isFalse();
    }

    @Test(priority = 8, dependsOnMethods = "step03_adminBansUser", description = "8. ბლოკის მოხსნა")
    public void step08_adminUnbansUser() {
        adminUsersPage.navigate();
        // Unban user with auto-waiting assertion on status badge
        adminUsersPage.unbanUser(randomUsername);

        String status = adminUsersPage.getUserStatus(randomUsername);
        assertThat(status).isEqualTo("Active");
        assertThat(adminUsersPage.isBanButtonVisible(randomUsername)).isTrue();

        Boolean enabled = checkUserEnabledInDb(userId);
        assertThat(enabled).isTrue();
    }

    @Test(priority = 9, dependsOnMethods = "step08_adminUnbansUser", description = "9. შესვლა Unban-ის შემდეგ")
    public void step09_loginAfterUnban() {
        userLoginPage.login(randomUsername, randomPassword);

        // Assert exact URL path is /events
        assertThat(userEventsPage.getUrlPath()).isEqualTo("/events");

        // Assert element that proves the logged-in state (Logout button & user profile link seen on real page)
        assertThat(userEventsPage.getLogoutButton()).isVisible();
        assertThat(userEventsPage.getUserProfileLink(randomUsername)).isVisible();
    }

    @Test(priority = 10, dependsOnMethods = "step08_adminUnbansUser", description = "10. REST Unban-ის შემდეგ")
    public void step10_restAfterUnban() {
        Response response = ApiClient.postComment(randomUsername, randomPassword, publishedEventId, randomAfterUnbanText);

        assertThat(response.statusCode()).isEqualTo(201);
        assertThat(response.jsonPath().getString("text")).isEqualTo(randomAfterUnbanText);
        assertThat(response.jsonPath().getString("author")).isEqualTo(randomUsername);
    }
}
