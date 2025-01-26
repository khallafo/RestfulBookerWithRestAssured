package Api.Booking;

import Utils.TokenManager;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;

public class PartialUpdateBookingTest {
    private static final Logger logger = LoggerFactory.getLogger(PartialUpdateBookingTest.class);
    private String VALID_TOKEN;
    private int bookingId;
    private Faker faker;
    private String originalFirstname;
    private String originalLastname;
    private int originalPrice;
    private String originalCheckin;
    private String originalCheckout;
    private String originalAdditionalNeeds;

    @BeforeMethod
    public void setup() {
        try {
            faker = new Faker();
            RestAssured.baseURI = "https://restful-booker.herokuapp.com";
            VALID_TOKEN = TokenManager.getToken();

            // Generate test booking data
            originalFirstname = faker.name().firstName();
            originalLastname = faker.name().lastName();
            originalPrice = faker.number().numberBetween(100, 500);

            LocalDate checkin = faker.date().future(30, TimeUnit.DAYS)
                    .toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            originalCheckin = checkin.toString();
            originalCheckout = checkin.plusDays(faker.number().numberBetween(1, 14)).toString();
            originalAdditionalNeeds = faker.food().spice();

            bookingId = createTestBooking();
        } catch (Exception e) {
            logger.error("Exception occurred during setup: ", e);
            fail("Exception occurred during setup: " + e.getMessage());
        }
    }

    @AfterMethod
    public void cleanup() {
        try {
            RestAssured.given()
                    .header("Authorization", "Bearer " + VALID_TOKEN)
                    .cookie("token", VALID_TOKEN)
                    .pathParam("id", bookingId)
                    .delete("/booking/{id}");
        } catch (Exception e) {
            logger.error("Exception occurred during cleanup: ", e);
            fail("Exception occurred during cleanup: " + e.getMessage());
        }
    }

    @Test
    public void updateMultipleFieldsSimultaneously() {
        try {
            String newFirstname = faker.name().firstName();
            String newLastname = faker.name().lastName();
            String newNeeds = faker.food().ingredient();

            String patchBody = "{"
                    + "\"firstname\": \"" + newFirstname + "\","
                    + "\"lastname\": \"" + newLastname + "\","
                    + "\"additionalneeds\": \"" + newNeeds + "\""
                    + "}";

            Response response = sendPatchRequest(patchBody);
            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getString("firstname"), newFirstname);
            assertEquals(response.jsonPath().getString("lastname"), newLastname);
            assertEquals(response.jsonPath().getString("additionalneeds"), newNeeds);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void updateWithEmptyFields() {
        try {
            String patchBody = "{"
                    + "\"firstname\": \"\","
                    + "\"lastname\": \"\","
                    + "\"additionalneeds\": \"\""
                    + "}";

            Response response = sendPatchRequest(patchBody);
            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getString("firstname"), "");
            assertEquals(response.jsonPath().getString("lastname"), "");
            assertEquals(response.jsonPath().getString("additionalneeds"), "");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void updateWithLongStrings() {
        try {
            String longString = "A".repeat(256);
            String patchBody = "{"
                    + "\"firstname\": \"" + longString + "\","
                    + "\"lastname\": \"" + longString + "\","
                    + "\"additionalneeds\": \"" + longString + "\""
                    + "}";

            Response response = sendPatchRequest(patchBody);
            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getString("firstname"), longString);
            assertEquals(response.jsonPath().getString("lastname"), longString);
            assertEquals(response.jsonPath().getString("additionalneeds"), longString);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void updateWithSpecialCharacters() {
        try {
            String specialChars = "!@#$%^&*()_+";
            String patchBody = "{"
                    + "\"firstname\": \"" + specialChars + "\","
                    + "\"lastname\": \"" + specialChars + "\","
                    + "\"additionalneeds\": \"" + specialChars + "\""
                    + "}";

            Response response = sendPatchRequest(patchBody);
            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getString("firstname"), specialChars);
            assertEquals(response.jsonPath().getString("lastname"), specialChars);
            assertEquals(response.jsonPath().getString("additionalneeds"), specialChars);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void updateWithInvalidToken() {
        try {
            String newFirstname = faker.name().firstName();
            String patchBody = "{"
                    + "\"firstname\": \"" + newFirstname + "\""
                    + "}";

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer invalid_token")
                    .cookie("token", "invalid_token")
                    .contentType("application/json")
                    .pathParam("id", bookingId)
                    .body(patchBody)
                    .patch("/booking/{id}");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 403);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    private Response sendPatchRequest(String body) {
        try {
            return RestAssured.given()
                    .header("Authorization", "Bearer " + VALID_TOKEN)
                    .cookie("token", VALID_TOKEN)
                    .contentType("application/json")
                    .pathParam("id", bookingId)
                    .body(body)
                    .patch("/booking/{id}");
        } catch (Exception e) {
            logger.error("Exception occurred while sending patch request: ", e);
            fail("Exception occurred while sending patch request: " + e.getMessage());
            return null;
        }
    }

    private void verifyAllFieldsUnchanged(Response response) {
        assertEquals(response.jsonPath().getString("firstname"), originalFirstname);
        assertEquals(response.jsonPath().getString("lastname"), originalLastname);
        assertEquals(response.jsonPath().getInt("totalprice"), originalPrice);
        assertEquals(response.jsonPath().getString("bookingdates.checkin"), originalCheckin);
        assertEquals(response.jsonPath().getString("bookingdates.checkout"), originalCheckout);
        assertEquals(response.jsonPath().getString("additionalneeds"), originalAdditionalNeeds);
    }

    private int createTestBooking() {
        try {
            String createBody = "{"
                    + "\"firstname\": \"" + originalFirstname + "\","
                    + "\"lastname\": \"" + originalLastname + "\","
                    + "\"totalprice\": " + originalPrice + ","
                    + "\"depositpaid\": true,"
                    + "\"bookingdates\": {"
                    + "\"checkin\": \"" + originalCheckin + "\","
                    + "\"checkout\": \"" + originalCheckout + "\""
                    + "},"
                    + "\"additionalneeds\": \"" + originalAdditionalNeeds + "\""
                    + "}";

            return RestAssured.given()
                    .contentType("application/json")
                    .body(createBody)
                    .post("/booking")
                    .jsonPath()
                    .getInt("bookingid");
        } catch (Exception e) {
            logger.error("Exception occurred while creating test booking: ", e);
            fail("Exception occurred while creating test booking: " + e.getMessage());
            return -1;
        }
    }
}