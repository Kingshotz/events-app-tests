package ge.eventsapp.api;

import ge.eventsapp.config.TestConfig;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class ApiClient {
    static {
        RestAssured.baseURI = TestConfig.BASE_URL;
    }

    public static Long getPublishedEventId() {
        Response response = given()
                .accept(ContentType.JSON)
                .when()
                .get("/api/v1/public/event")
                .then()
                .statusCode(200)
                .extract()
                .response();

        List<Map<String, Object>> events = response.jsonPath().getList("$");
        for (Map<String, Object> event : events) {
            if ("PUBLISHED".equalsIgnoreCase(String.valueOf(event.get("status")))) {
                return ((Number) event.get("id")).longValue();
            }
        }
        if (!events.isEmpty()) {
            return ((Number) events.get(0).get("id")).longValue();
        }
        throw new IllegalStateException("No published event found from /api/v1/public/event");
    }

    public static Response registerUser(String username, String email, String password) {
        Map<String, Object> body = new HashMap<>();
        body.put("username", username);
        body.put("email", email);
        body.put("password", password);
        body.put("roles", List.of("ROLE_USER"));

        return given()
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/v1/public/user");
    }

    public static Response postComment(String username, String password, Long eventId, String text) {
        Map<String, Object> body = new HashMap<>();
        body.put("text", text);

        return given()
                .auth()
                .preemptive()
                .basic(username, password)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .queryParam("eventId", eventId)
                .body(body)
                .when()
                .post("/api/v1/comment");
    }

    public static Response getPublicComments(Long eventId) {
        return given()
                .accept(ContentType.JSON)
                .queryParam("eventId", eventId)
                .when()
                .get("/api/v1/public/comment");
    }
}
