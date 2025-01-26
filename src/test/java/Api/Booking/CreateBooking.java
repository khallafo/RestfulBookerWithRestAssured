package Api.Booking;

import Utils.TokenManager;
import com.github.javafaker.Faker;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.concurrent.TimeUnit;
import static org.testng.Assert.*;

public class CreateBooking {
    private static final Logger logger = LoggerFactory.getLogger(CreateBooking.class);
    private Faker faker;
    private String validToken;

    @BeforeMethod
    public void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        faker = new Faker();
        validToken = TokenManager.getToken();
    }

    @Test
    public void createBookingWithValidData() {
        try {
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            int totalPrice = faker.number().numberBetween(100, 1000);
            LocalDate checkin = faker.date().future(30, TimeUnit.DAYS).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            LocalDate checkout = checkin.plusDays(faker.number().numberBetween(1, 14));

            String requestBody = String.format("{"
                            + "\"firstname\": \"%s\","
                            + "\"lastname\": \"%s\","
                            + "\"totalprice\": %d,"
                            + "\"depositpaid\": %s,"
                            + "\"bookingdates\": {"
                            + "\"checkin\": \"%s\","
                            + "\"checkout\": \"%s\""
                            + "},"
                            + "\"additionalneeds\": \"%s\""
                            + "}",
                    firstName, lastName, totalPrice, faker.bool().bool(), checkin, checkout, faker.food().ingredient());

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200, "Expected status code 200");
            assertNotNull(response.jsonPath().getInt("bookingid"), "Booking ID should not be null");
            assertEquals(response.jsonPath().getString("booking.firstname"), firstName, "First name should match");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void createBookingWithInvalidToken() {
        try {
            String requestBody = String.format("{"
                            + "\"firstname\": \"%s\","
                            + "\"lastname\": \"%s\""
                            + "}",
                    faker.name().firstName(), faker.name().lastName());

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + faker.regexify("[A-Za-z0-9]{20}"))
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 403, "Expected status code 403");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void createBookingWithMissingRequiredFields() {
        try {
            String requestBody = String.format("{ \"firstname\": \"%s\" }", faker.name().firstName());

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 500, "Expected status code 500");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void createBookingWithInvalidDateFormats() {
        try {
            String requestBody = String.format("{"
                            + "\"firstname\": \"%s\","
                            + "\"lastname\": \"%s\","
                            + "\"bookingdates\": {"
                            + "\"checkin\": \"%s\","
                            + "\"checkout\": \"%s\""
                            + "}"
                            + "}",
                    faker.name().firstName(), faker.name().lastName(),
                    faker.date().past(10, TimeUnit.DAYS).toString(),
                    faker.date().past(5, TimeUnit.DAYS).toString());

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 500, "Expected status code 500");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void createBookingWithInvalidDataTypes() {
        try {
            String requestBody = String.format("{"
                            + "\"firstname\": %d,"
                            + "\"lastname\": %s,"
                            + "\"totalprice\": \"%s\""
                            + "}",
                    faker.number().randomDigit(), faker.bool().bool(), faker.commerce().price());

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 500, "Expected status code 500");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void createBookingWithSpecialCharacters() {
        try {
            String requestBody = String.format("{"
                            + "\"firstname\": \"%s\","
                            + "\"lastname\": \"%s\","
                            + "\"additionalneeds\": \"%s\""
                            + "}",
                    faker.name().firstName().replace("a", "ä"),
                    faker.name().lastName().replace("o", "ö"),
                    faker.lorem().sentence());

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200, "Expected status code 200");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void createBookingWithEdgeCasePrices() {
        try {
            String requestBody = String.format("{"
                            + "\"firstname\": \"%s\","
                            + "\"lastname\": \"%s\","
                            + "\"totalprice\": %d"
                            + "}",
                    faker.name().firstName(), faker.name().lastName(), faker.number().numberBetween(-100, 0));

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertTrue(response.getStatusCode() == 400 || response.getStatusCode() == 500, "Expected status code 400 or 500");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void createBookingWithLongAdditionalNeeds() {
        try {
            String requestBody = String.format("{"
                            + "\"firstname\": \"%s\","
                            + "\"additionalneeds\": \"%s\""
                            + "}",
                    faker.name().firstName(), faker.lorem().characters(1000));

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("application/json")
                    .body(requestBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 500, "Expected status code 200 or 500");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void createBookingWithMalformedJSON() {
        try {
            String malformedBody = String.format("{ \"firstname\": \"%s\", ", faker.name().firstName());

            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + validToken)
                    .contentType("application/json")
                    .body(malformedBody)
                    .post("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 400, "Expected status code 400");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }
}