package offercart;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class CartServiceOfferTest {

	@BeforeClass
	public void setup() {
		// Base URI for the mock server
		RestAssured.baseURI = "http://localhost:1080";
	}

	// Test Case 1: Flat X Amount Off
	@Test()
	public void when_FlatX_Amount_Off_Applied() {
		// Mock: Fetch user segment
		given().queryParam("user_id", 1).when().get("/api/v1/user_segment").then().statusCode(200).body("segment",
				equalTo("p1"));

		// Mock: Add offer
		given().contentType(ContentType.JSON)
				.body("{\"restaurant_id\":1,\"offer_type\":\"FLATX\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}")
				.when().post("/api/v1/offer").then().statusCode(200).body("response_msg", equalTo("success"));

		// Test: Apply offer
		Response response = given().contentType(ContentType.JSON)
				.body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}").when().post("/api/v1/cart/apply_offer")
				.then().assertThat().statusCode(200).extract().response();

		/*
		 * given().contentType(ContentType.JSON)
		 * .body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}").when().post(
		 * "/api/v1/cart/apply_offer")
		 * .then().assertThat().statusCode(200).body("cart_value", equalTo(190));
		 */
	}

	// Test Case 2: Flat X% Off
	@Test
	public void when_FlatX_Percent_Off_Applied() {
		// Mock: Fetch user segment
		given().queryParam("user_id", 1).when().get("/api/v1/user_segment").then().statusCode(200).body("segment",
				equalTo("p1"));

		// Mock: Add offer
		given().contentType(ContentType.JSON).body(
				"{\"restaurant_id\":1,\"offer_type\":\"FLATPERCENT\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}")
				.when().post("/api/v1/offer").then().statusCode(200).body("response_msg", equalTo("success"));

		// Test: Apply offer
		Response response = given().contentType(ContentType.JSON)
				.body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}").when().post("/api/v1/cart/apply_offer")
				.then().assertThat().statusCode(200).body("cart_value", equalTo(180)).extract().response();

	}

	// Test Case 3: No Applicable Offer
	@Test
	public void when_No_ApplicableOffer_Applied() {
		// Mock: Fetch user segment
		given().queryParam("user_id", 1).when().get("/api/v1/user_segment").then().statusCode(200).body("segment",
				equalTo("p1"));

		// Test: Apply offer without adding any offer for the user segment
		Response response = given().contentType(ContentType.JSON)
				.body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}").when().post("/api/v1/cart/apply_offer")
				.then().assertThat().statusCode(200).body("cart_value", equalTo(200)).extract().response();

	}

	// Test Case 4: Invalid User Segment
	@Test
	public void when_passed_Invalid_UserSegment() {
		// Mock: Fetch user segment (returns invalid segment)
		given().queryParam("user_id", 1).when().get("/api/v1/user_segment").then().statusCode(200).body("segment",
				equalTo("invalid"));

		// Test: Apply offer with no valid segment
		Response response = given().contentType(ContentType.JSON)
				.body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}").when().post("/api/v1/cart/apply_offer")
				.then().assertThat().statusCode(200).body("cart_value", equalTo(200)).extract().response();

	}

	// Test Case 5: Missing User Segment
	@Test
	public void when_passed_Missing_UserSegment() {
		// Mock: Fetch user segment (returns null)
		given().queryParam("user_id", 1).when().get("/api/v1/user_segment").then().statusCode(404);

		// Test: Apply offer without a valid user segment
		Response response = given().contentType(ContentType.JSON)
				.body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}").when().post("/api/v1/cart/apply_offer")
				.then().assertThat().statusCode(200).body("cart_value", equalTo(200)).extract().response();

	}

}
