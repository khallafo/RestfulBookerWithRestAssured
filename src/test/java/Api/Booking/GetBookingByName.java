package Api.Booking;

import Utils.TokenManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.concurrent.TimeUnit;

import static org.testng.Assert.*;

public class GetBookingByName {

    private static final Logger logger = LoggerFactory.getLogger(GetBookingByName.class);
    private final String TEST_FIRSTNAME = "John";
    private final String TEST_LASTNAME = "Doe";

    @BeforeClass
    public void setup() {
        RestAssured.baseURI = "https://restful-booker.herokuapp.com";
        createTestBooking(); // Ensure test data exists
    }

    @Test
    public void getBookingWithValidName() {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", TEST_FIRSTNAME)
                    .queryParam("lastname", TEST_LASTNAME)
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertTrue(response.jsonPath().getList("bookingid").size() > 0);
            assertEquals(response.jsonPath().getString("[0].booking.firstname"), TEST_FIRSTNAME);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithInvalidName() {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", "Invalid")
                    .queryParam("lastname", "Name")
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getList("bookingid").size(), 0);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithPartialName() {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", TEST_FIRSTNAME)
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertTrue(response.jsonPath().getList("bookingid").size() > 0);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithSpecialCharacters() {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", "Jöhn")
                    .queryParam("lastname", "D'oe")
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertTrue(response.jsonPath().getList("bookingid").size() >= 0);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithCaseInsensitiveSearch() {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", TEST_FIRSTNAME.toLowerCase())
                    .queryParam("lastname", TEST_LASTNAME.toUpperCase())
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertTrue(response.jsonPath().getList("bookingid").size() > 0);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithoutAuthentication() {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", TEST_FIRSTNAME)
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithEmptyFirstName() {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", "")
                    .queryParam("lastname", TEST_LASTNAME)
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getList("bookingid").size(), 0);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithEmptyLastName() {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", TEST_FIRSTNAME)
                    .queryParam("lastname", "")
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getList("bookingid").size(), 0);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithLongFirstName() {
        try {
            String longFirstName = "A".repeat(256);
            Response response = RestAssured.given()
                    .queryParam("firstname", longFirstName)
                    .queryParam("lastname", TEST_LASTNAME)
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getList("bookingid").size(), 0);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @Test
    public void getBookingWithLongLastName() {
        try {
            String longLastName = "B".repeat(256);
            Response response = RestAssured.given()
                    .queryParam("firstname", TEST_FIRSTNAME)
                    .queryParam("lastname", longLastName)
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.getStatusCode(), 200);
            assertEquals(response.jsonPath().getList("bookingid").size(), 0);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    private void createTestBooking() {
        try {
            RestAssured.given()
                    .header("Authorization", "Bearer " + TokenManager.getToken())
                    .contentType("application/json")
                    .body("{"
                            + "\"firstname\": \"" + TEST_FIRSTNAME + "\","
                            + "\"lastname\": \"" + TEST_LASTNAME + "\","
                            + "\"totalprice\": 250,"
                            + "\"depositpaid\": true"
                            + "}")
                    .post("/booking");
        } catch (Exception e) {
            logger.error("Exception occurred while creating test booking: ", e);
            fail("Exception occurred while creating test booking: " + e.getMessage());
        }
    }

    @Test(dataProvider = "nameCombinations")
    public void testVariousNameCombinations(String first, String last, int expectedResults) {
        try {
            Response response = RestAssured.given()
                    .queryParam("firstname", first)
                    .queryParam("lastname", last)
                    .get("/booking");

            logger.debug("Response: {}", response.asPrettyString());

            assertEquals(response.jsonPath().getList("bookingid").size(), expectedResults);
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }

    @DataProvider
    public Object[][] nameCombinations() {
        return new Object[][] {
                {TEST_FIRSTNAME, TEST_LASTNAME, 1},
                {"", "", 0},
                {null, TEST_LASTNAME, 0},
                {TEST_FIRSTNAME, "Unknown", 0}
        };
    }

    @Test
    public void testResponseTime() {
        try {
            long responseTime = RestAssured.given()
                    .queryParam("firstname", TEST_FIRSTNAME)
                    .get("/booking")
                    .timeIn(TimeUnit.MILLISECONDS);

            logger.debug("Response time: {} ms", responseTime);

            assertTrue(responseTime < 1000, "Response time too slow: " + responseTime + "ms");
        } catch (Exception e) {
            logger.error("Exception occurred: ", e);
            fail("Exception occurred: " + e.getMessage());
        }
    }
}