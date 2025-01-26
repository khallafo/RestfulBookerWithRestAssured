package Api.Authentication;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class AuthenticationTest {

    @BeforeMethod
    public void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        RestAssured.basePath = "/auth";
    }

    @Test
    public void validCredentialsShouldReturnToken() {
        String requestBody = "{ \"username\": \"admin\", \"password\": \"password123\" }";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post();

        assertEquals(response.getStatusCode(), 200);
        assertNotNull(response.jsonPath().getString("token"));
        System.out.println("Token: " + response.jsonPath().getString("token"));
    }

    @Test
    public void invalidUsernameShouldReturnUnauthorized() {
        String requestBody = "{ \"username\": \"wronguser\", \"password\": \"password123\" }";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post();

        assertEquals(response.getStatusCode(), 401);
        assertTrue(response.getBody().asString().contains("Bad credentials"));
    }

    @Test
    public void invalidPasswordShouldReturnUnauthorized() {
        String requestBody = "{ \"username\": \"admin\", \"password\": \"wrongpass\" }";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post();

        assertEquals(response.getStatusCode(), 401);
        assertTrue(response.getBody().asString().contains("Bad credentials"));
    }

    @Test
    public void missingUsernameShouldReturnBadRequest() {
        String requestBody = "{ \"password\": \"password123\" }";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post();

        assertEquals(response.getStatusCode(), 400);
        assertTrue(response.getBody().asString().contains("Invalid credentials"));
    }

    @Test
    public void missingPasswordShouldReturnBadRequest() {
        String requestBody = "{ \"username\": \"admin\" }";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post();

        assertEquals(response.getStatusCode(), 400);
        assertTrue(response.getBody().asString().contains("Invalid credentials"));
    }

    @Test
    public void emptyCredentialsShouldReturnBadRequest() {
        String requestBody = "{ \"username\": \"\", \"password\": \"\" }";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post();

        assertEquals(response.getStatusCode(), 400);
        assertTrue(response.getBody().asString().contains("Invalid credentials"));
    }

    @Test
    public void invalidContentTypeShouldReturnUnsupportedMediaType() {
        String requestBody = "username=admin&password=password123";

        Response response = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .body(requestBody)
                .post();

        assertEquals(response.getStatusCode(), 415);
    }

    @Test
    public void malformedJSONShouldReturnBadRequest() {
        String requestBody = "{ \"username\": \"admin\", \"password\": }";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post();

        assertEquals(response.getStatusCode(), 400);
        assertTrue(response.getBody().asString().contains("Invalid JSON"));
    }
}