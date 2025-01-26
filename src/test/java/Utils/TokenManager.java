package Utils;

import io.restassured.response.Response;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.testng.Assert.*;

public class TokenManager {
    private static String authToken;

    public static synchronized String getToken() {
        if (authToken == null) {
            generateToken();
        }
        return authToken;
    }

    private static void generateToken() {
        String requestBody = "{ \"username\": \"admin\", \"password\": \"password123\" }";

        Response response = RestAssured.given()
                .contentType("application/json")
                .body(requestBody)
                .post("https://restful-booker.herokuapp.com/auth");

        authToken = response.jsonPath().getString("token");

    }
}