package offercart;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class CartApiTest {

    @BeforeClass
    public void setup() {
        // Set the base URI for Rest Assured
        RestAssured.baseURI = "http://localhost:9001"; // Replace with your server's base URL
    }
    
    /**
     * Helper method: Mock the user segment API response
     */
    private void mockUserSegment(String segment) {
        // Assuming mock setup using WireMock or a similar library.
        // Replace with your mocking framework's implementation if required.
        given()
            .contentType(ContentType.JSON)
            .body("{\"segment\":\"" + segment + "\"}")
        .when()
            .get("/api/v1/user_segment?user_id=1")
        .then()
            .statusCode(200);
    }

    /**
     * Test case: Apply "FLAT X amount off" offer for segment "p1"
     */
    @Test
    public void when_FlatAmount_Offer_Applied() {
        // Mock the user segment API
        mockUserSegment("p1");

        // Add a flat amount offer for the restaurant and segment "p1"
        given()
            .contentType(ContentType.JSON)
            .body("{\"restaurant_id\":1,\"offer_type\":\"FLATX\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}")
        .when()
            .post("/api/v1/offer")
        .then()
            .statusCode(200)
            .body("response_msg", equalTo("success"));

        // Apply the offer on the cart
        given()
            .contentType(ContentType.JSON)
            .body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}")
        .when()
            .post("/api/v1/cart/apply_offer")
        .then()
            .statusCode(200)
            .body("cart_value", equalTo(190)); // Verify discounted value
    }

    /**
     * Test case: Apply "FLAT X% off" offer for segment "p1"
     */
    @Test
    public void when_FlatPercentage_Offer_Applied() {
        // Mock the user segment API
        mockUserSegment("p1");

        // Add a percentage-based offer for the restaurant and segment "p1"
        given()
            .contentType(ContentType.JSON)
            .body("{\"restaurant_id\":1,\"offer_type\":\"FLATPERCENT\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}")
        .when()
            .post("/api/v1/offer")
        .then()
            .statusCode(200)
            .body("response_msg", equalTo("success"));

        // Apply the offer on the cart
        given()
            .contentType(ContentType.JSON)
            .body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}")
        .when()
            .post("/api/v1/cart/apply_offer")
        .then()
            .statusCode(200)
            .body("cart_value", equalTo(180)); // Verify discounted value
    }

    /**
     * Test case: No offer applicable for segment "p3"
     */
    @Test
    public void testNoOfferForSegment() {
        // Mock the user segment API
        mockUserSegment("p3"); // Segment "p3" is not eligible for any offer

        // Add an offer for a different segment (e.g., "p1")
        given()
            .contentType(ContentType.JSON)
            .body("{\"restaurant_id\":1,\"offer_type\":\"FLATX\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}")
        .when()
            .post("/api/v1/offer")
        .then()
            .statusCode(200)
            .body("response_msg", equalTo("success"));

        // Apply the offer on the cart
        given()
            .contentType(ContentType.JSON)
            .body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}")
        .when()
            .post("/api/v1/cart/apply_offer")
        .then()
            .statusCode(200)
            .body("cart_value", equalTo(200)); // No discount applied
    }

    /**
     * Test case: No offers exist for the restaurant
     */
    @Test
    public void testNoOffersExist() {
        // Mock the user segment API
        mockUserSegment("p1");

        // Do not add any offers for the restaurant

        // Apply the offer on the cart
        given()
            .contentType(ContentType.JSON)
            .body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}")
        .when()
            .post("/api/v1/cart/apply_offer")
        .then()
            .statusCode(200)
            .body("cart_value", equalTo(200)); // No discount applied
    }

   
}
