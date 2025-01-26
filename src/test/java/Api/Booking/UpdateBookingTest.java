package Api.Booking;

import Utils.TokenManager;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class UpdateBookingTest {
    private int validBookingId;
    private Faker faker;
    private String originalFirstname;
    private String originalLastname;
    private int originalTotalPrice;
    private String originalCheckin;
    private String originalCheckout;
    private String originalAdditionalNeeds;
    private String validToken;

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        validToken = TokenManager.getToken();

        if (validToken == null || validToken.isEmpty()) {
            fail("Failed to obtain valid authentication token");
        }

        generateTestData();
        validBookingId = createTestBooking();
    }

    private void generateTestData() {
        originalFirstname = faker.name().firstName();
        originalLastname = faker.name().lastName();
        originalTotalPrice = faker.number().numberBetween(100, 500);

        LocalDate checkin = faker.date().future(30, TimeUnit.DAYS)
                .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        originalCheckin = checkin.toString();
        originalCheckout = checkin.plusDays(faker.number().numberBetween(1, 14)).toString();
        originalAdditionalNeeds = faker.food().spice();
    }

    @Test
    public void updateBookingWithValidData() {
        String newFirstname = faker.name().firstName();
        String newLastname = faker.name().lastName();
        int newTotalPrice = faker.number().numberBetween(500, 1000);
        LocalDate newCheckin = LocalDate.parse(originalCheckin).plusDays(5);
        String newCheckout = newCheckin.plusDays(faker.number().numberBetween(1, 14)).toString();
        String newAdditionalNeeds = faker.food().ingredient();

        String updatedBody = String.format("{"
                + "\"firstname\": \"%s\","
                + "\"lastname\": \"%s\","
                + "\"totalprice\": %d,"
                + "\"depositpaid\": false,"
                + "\"bookingdates\": {"
                + "\"checkin\": \"%s\","
                + "\"checkout\": \"%s\""
                + "},"
                + "\"additionalneeds\": \"%s\""
                + "}", newFirstname, newLastname, newTotalPrice, newCheckin, newCheckout, newAdditionalNeeds);

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .cookie("token", validToken)
                .contentType(ContentType.JSON)
                .pathParam("id", validBookingId)
                .body(updatedBody)
                .put("/booking/{id}");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.jsonPath().getString("firstname"), newFirstname);
        assertEquals(response.jsonPath().getString("lastname"), newLastname);
        assertEquals(response.jsonPath().getInt("totalprice"), newTotalPrice);
        assertEquals(response.jsonPath().getString("bookingdates.checkin"), newCheckin.toString());
        assertEquals(response.jsonPath().getString("bookingdates.checkout"), newCheckout);
        assertEquals(response.jsonPath().getString("additionalneeds"), newAdditionalNeeds);
    }

    @Test
    public void updateBookingWithInvalidToken() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer invalid_token_123")
                .cookie("token", "invalid_cookie_456")
                .contentType(ContentType.JSON)
                .pathParam("id", validBookingId)
                .body("{}")
                .put("/booking/{id}");

        assertEquals(response.getStatusCode(), 403);
    }

    @Test
    public void updateNonExistentBooking() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .cookie("token", validToken)
                .contentType(ContentType.JSON)
                .pathParam("id", 1)
                .body("{}")
                .put("/booking/{id}");

        assertEquals(response.getStatusCode(), 404);
    }

    @Test
    public void updateBookingWithInvalidDataTypes() {
        String invalidBody = "{"
                + "\"totalprice\": \"three-hundred\","
                + "\"depositpaid\": \"yes\""
                + "}";

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .cookie("token", validToken)
                .contentType(ContentType.JSON)
                .pathParam("id", validBookingId)
                .body(invalidBody)
                .put("/booking/{id}");

        assertEquals(response.getStatusCode(), 400);
    }

    @Test
    public void updateBookingWithoutAuthentication() {
        Response response = RestAssured.given()
                .contentType(ContentType.JSON)
                .pathParam("id", validBookingId)
                .body("{}")
                .put("/booking/{id}");

        assertEquals(response.getStatusCode(), 403);
    }

    @Test
    public void partialUpdateBooking() {
        String newAdditionalNeeds = faker.food().ingredient();
        String partialBody = "{ \"additionalneeds\": \"" + newAdditionalNeeds + "\" }";

        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .cookie("token", validToken)
                .contentType(ContentType.JSON)
                .pathParam("id", validBookingId)
                .body(partialBody)
                .put("/booking/{id}");

        assertEquals(response.getStatusCode(), 200);
        assertEquals(response.jsonPath().getString("additionalneeds"), newAdditionalNeeds);
        assertEquals(response.jsonPath().getString("firstname"), originalFirstname);
        assertEquals(response.jsonPath().getString("lastname"), originalLastname);
        assertEquals(response.jsonPath().getInt("totalprice"), originalTotalPrice);
    }

    @Test
    public void testUpdatePerformance() {
        String tempNeed = faker.food().ingredient();

        long responseTime = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .cookie("token", validToken)
                .contentType(ContentType.JSON)
                .pathParam("id", validBookingId)
                .body("{ \"additionalneeds\": \"" + tempNeed + "\" }")
                .put("/booking/{id}")
                .timeIn(TimeUnit.MILLISECONDS);

        assertTrue(responseTime < 1500, "Response time exceeded 1.5 seconds: " + responseTime + "ms");
    }

    @AfterClass
    public void cleanup() {
        Response response = RestAssured.given()
                .header("Authorization", "Bearer " + validToken)
                .cookie("token", validToken)
                .pathParam("id", validBookingId)
                .delete("/booking/{id}");

        assertEquals(response.getStatusCode(), 201, "Cleanup failed - booking not deleted");
    }

    private int createTestBooking() {
        String createBody = String.format("{"
                        + "\"firstname\": \"%s\","
                        + "\"lastname\": \"%s\","
                        + "\"totalprice\": %d,"
                        + "\"depositpaid\": true,"
                        + "\"bookingdates\": {"
                        + "\"checkin\": \"%s\","
                        + "\"checkout\": \"%s\""
                        + "},"
                        + "\"additionalneeds\": \"%s\""
                        + "}", originalFirstname, originalLastname, originalTotalPrice,
                originalCheckin, originalCheckout, originalAdditionalNeeds);

        return RestAssured.given()
                .contentType(ContentType.JSON)
                .body(createBody)
                .post("/booking")
                .then()
                .statusCode(200)
                .extract()
                .path("bookingid");
    }
}