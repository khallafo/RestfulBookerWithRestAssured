package Api.Booking;

import Utils.TokenManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class GetBooking {

    private static final Logger logger = LoggerFactory.getLogger(GetBooking.class);
    private final int VALID_BOOKING_ID = 1; // Replace with known valid ID

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
    }

    @Test
    public void getBookingWithValidId() {
        try {
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .pathParam("booking_id", VALID_BOOKING_ID)
                    .get("/booking/{booking_id}");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertNotNull(response.jsonPath().getString("firstname"));
            assertNotNull(response.jsonPath().getString("lastname"));
            System.out.println("Booking Details: " + response.asPrettyString());
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithInvalidId() {
        try {
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .pathParam("booking_id", 999999)
                    .get("/booking/{booking_id}");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 404);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithoutAuthentication() {
        try {
            Response response = RestAssured.given()
                    .pathParam("booking_id", VALID_BOOKING_ID)
                    .get("/booking/{booking_id}");

            logger.debug("Response: {}", response.asPrettyString());

            // Some APIs return 200 for GET without auth, others 401
            assertTrue(response.getStatusCode() == 200 || response.getStatusCode() == 401);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithInvalidToken() {
        try {
            Response response = RestAssured.given()
                    .header("Authorization", TokenManager.getToken())
                    .pathParam("booking_id", VALID_BOOKING_ID)
                    .get("/booking/{booking_id}");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 401);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithSpecialCharactersInId() {
        try {
            Response response = RestAssured.given()
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .pathParam("booking_id", "abc$%^")
                    .get("/booking/{booking_id}");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 404);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }
}