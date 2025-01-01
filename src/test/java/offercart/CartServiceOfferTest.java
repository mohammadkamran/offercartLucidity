/*
 * Created By @Mohammad Kamran
 * Date: 01-Jan-2024
*/

package offercart;

import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.path.json.config.JsonParserType;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.annotations.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import java.io.ObjectInputFilter.Status;

public class CartServiceOfferTest {

	String userSegment_URL = "http://localhost:1080/api/v1/user_segment";
	String resturantOffer_URL = "http://localhost:9001/api/v1/offer";
	String cartOffer_URL = "http://localhost:9001/api/v1/cart/apply_offer";

	// Test Case 1: Flat X Amount Off
	@Test()
	public void when_FlatX_Amount_Off_Applied() {

		given().queryParam("user_id", 1).when().get(userSegment_URL).then().assertThat().statusCode(200);

		given().contentType(ContentType.JSON)
				.body("{\"restaurant_id\":1,\"offer_type\":\"FLATX\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}")
				.when().post(resturantOffer_URL).then().statusCode(200).body("response_msg", equalTo("success"));

		given().contentType(ContentType.JSON).body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":1}").when()
				.post(cartOffer_URL).then().assertThat().statusCode(200).body("cart_value", equalTo(190)).extract()
				.response();
	}

	// Test Case 2: Flat X% Off

	@Test
	public void when_FlatX_Percent_Off_Applied() {

		given().queryParam("user_id", 1).when().get(userSegment_URL).then().assertThat().statusCode(200);

		given().contentType(ContentType.JSON).body(
				"{\"restaurant_id\":2,\"offer_type\":\"FLATPERCENT\",\"offer_value\":10,\"customer_segment\":[\"p1\"]}")
				.when().post(resturantOffer_URL).then().statusCode(200).body("response_msg", equalTo("success"));

		given().contentType(ContentType.JSON).body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":2}").when()
				.post(cartOffer_URL).then().assertThat().statusCode(200).body("cart_value", equalTo(180)).extract()
				.response();

	}

	// Test Case 3: No Applicable Offer

	@Test
	public void when_No_ApplicableOffer_Applied() {

		given().queryParam("user_id", 1).when().get(userSegment_URL).then().assertThat().statusCode(200);

		given().contentType(ContentType.JSON).body("{\"cart_value\":200,\"user_id\":1,\"restaurant_id\":4}").when()
				.post(cartOffer_URL).then().assertThat().statusCode(200).body("cart_value", equalTo(200)).extract()
				.response();

	}

	// Test Case 4: Invalid User Segment for api check

	@Test
	public void when_passed_Invalid_UserSegment() {

		given().queryParam("user_id", 1).when().get(userSegment_URL).then().statusCode(200);

		given().contentType(ContentType.JSON).body("{\"cart_value\":200,\"user_id\":-5,\"restaurant_id\":1}").when()
				.post(cartOffer_URL).then().assertThat().statusCode(200).body("cart_value", equalTo(200)).extract()
				.response();

	}

	// Test Case 5: Missing User Segment for api check

	@Test
	public void when_passed_Missing_UserSegment() {

		given().queryParam("user_id", "").when().get(userSegment_URL).then().statusCode(404);

		given().contentType(ContentType.JSON).body("{\"cart_value\":200,\"user_id\":\"\",\"restaurant_id\":1}").when()
				.post(cartOffer_URL).then().assertThat().statusCode(200).body("cart_value", equalTo(200)).extract()
				.response();

	}

	// Test Case 6: Restaurant doesn't exist for api check

	@Test
	public void when_passed_invalid_resturant() {

		Response response = given().contentType(ContentType.JSON)
				.body("{\"cart_value\":200,\"user_id\":\"ab\",\"restaurant_id\":1}").when().post(cartOffer_URL).then()
				.extract().response();
		String responseString = response.asString();
		JsonPath jsonResponse = JsonPath.with(responseString);
		String error = jsonResponse.getString("error");
		int status = jsonResponse.getInt("status");
		Assert.assertEquals(error, "Bad Request");
		Assert.assertEquals(status, 400);

	}
}
