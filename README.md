# Booking API Tests

This project contains automated tests for the Booking API using Java, Maven, TestNG, and RestAssured. The tests cover various scenarios for creating, updating, and deleting bookings.

## Project Structure
src └── test └── java └── Api └── Booking ├── PartialUpdateBookingTest.java ├── UpdateBookingTest.java └── Utils ├── TokenManager.java ├── ConfigManager.java
## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher

## Setup

1. Clone the repository:
    ```sh
    git clone https://github.com/yourusername/booking-api-tests.git
    cd booking-api-tests
    ```

2. Install dependencies:
    ```sh
    mvn clean install
    ```

3. Configure the `config.properties` file:
    ```properties
    baseURI=https://restful-booker.herokuapp.com
    ```

## Running Tests

To run all tests, use the following command:
```sh
mvn test

Test Classes
PartialUpdateBookingTest
This class contains tests for partially updating a booking. It includes tests for updating multiple fields, handling empty fields, long strings, special characters, and invalid tokens.  
UpdateBookingTest
This class contains tests for updating a booking. It includes tests for updating with valid data, invalid tokens, non-existent bookings, invalid data types, and performance testing.

Utility Classes
TokenManager
This class handles the generation and management of authentication tokens.
ConfigManager
This class manages configuration properties.  
Logging
The project uses SLF4J for logging important information and errors. 
