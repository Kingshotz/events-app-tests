package ge.eventsapp.config;

public class TestConfig {
    public static final String BASE_URL = System.getProperty("app.base.url", "http://localhost:8081");
    public static final String ADMIN_USERNAME = System.getProperty("admin.username", "admin");
    public static final String ADMIN_PASSWORD = System.getProperty("admin.password", "admin123");
    public static final String H2_URL = System.getProperty("h2.url",
            "jdbc:h2:tcp://localhost:9092/eventsdb;MODE=MSSQLServer;CASE_INSENSITIVE_IDENTIFIERS=TRUE;SCHEMA=EVENTS_SCHEMA");
    public static final String H2_USER = System.getProperty("h2.user", "sa");
    public static final String H2_PASSWORD = System.getProperty("h2.password", "");
}
