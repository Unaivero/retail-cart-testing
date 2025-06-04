package com.retailer.cart.api.tests;

import com.retailer.cart.models.Product;
import com.retailer.cart.models.ShoppingCart;
import com.retailer.cart.utils.ConfigReader;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import static org.hamcrest.Matchers.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Tag("api")
public class CartAPITests {

    private static RequestSpecification requestSpec;
    private static ResponseSpecification responseSpec200;
    private static ResponseSpecification responseSpec201;
    private static ResponseSpecification responseSpec204;
    private static ResponseSpecification responseSpec400;
    private static String apiBaseUrl;

    @BeforeAll
    public static void setup() {
        apiBaseUrl = ConfigReader.getApiBaseUrl();
        
        requestSpec = new RequestSpecBuilder()
            .setBaseUri(apiBaseUrl)
            .setContentType(ContentType.JSON)
            .addHeader("Accept", "application/json")
            .addHeader("User-Agent", "RetailCartTestSuite/1.0")
            .build();

        responseSpec200 = new ResponseSpecBuilder()
            .expectStatusCode(200)
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(5000L))
            .build();

        responseSpec201 = new ResponseSpecBuilder()
            .expectStatusCode(201)
            .expectContentType(ContentType.JSON)
            .expectResponseTime(lessThan(3000L))
            .build();
            
        responseSpec204 = new ResponseSpecBuilder()
            .expectStatusCode(204)
            .expectResponseTime(lessThan(3000L))
            .build();
            
        responseSpec400 = new ResponseSpecBuilder()
            .expectStatusCode(400)
            .expectContentType(ContentType.JSON)
            .build();
    }

    @Test
    @DisplayName("Create new shopping cart")
    @Tag("smoke")
    public void testCreateNewCart() {
        String customerId = UUID.randomUUID().toString();
        Map<String, Object> cartRequest = new HashMap<>();
        cartRequest.put("customerId", customerId);
        cartRequest.put("currency", "USD");

        RestAssured.given()
            .spec(requestSpec)
            .body(cartRequest)
        .when()
            .post("/cart")
        .then()
            .spec(responseSpec201)
            .body("id", notNullValue())
            .body("customerId", equalTo(customerId))
            .body("currency", equalTo("USD"))
            .body("items", hasSize(0))
            .body("subtotal", equalTo(0.0f))
            .body("total", equalTo(0.0f))
            .body("createdAt", notNullValue());
    }

    @Test
    @DisplayName("Get cart by ID")
    @Tag("smoke")
    public void testGetCartById() {
        // First create a cart
        String customerId = UUID.randomUUID().toString();
        Map<String, Object> cartRequest = new HashMap<>();
        cartRequest.put("customerId", customerId);
        
        String cartId = RestAssured.given()
            .spec(requestSpec)
            .body(cartRequest)
        .when()
            .post("/cart")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Then retrieve it
        RestAssured.given()
            .spec(requestSpec)
        .when()
            .get("/cart/{cartId}", cartId)
        .then()
            .spec(responseSpec200)
            .body("id", equalTo(cartId))
            .body("customerId", equalTo(customerId))
            .body("items", hasSize(0));
    }

    @Test
    @DisplayName("Add product to cart")
    @Tag("regression")
    public void testAddProductToCart() {
        // Create cart first
        String customerId = UUID.randomUUID().toString();
        Map<String, Object> cartRequest = new HashMap<>();
        cartRequest.put("customerId", customerId);
        
        String cartId = RestAssured.given()
            .spec(requestSpec)
            .body(cartRequest)
        .when()
            .post("/cart")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        // Add product to cart
        Map<String, Object> addItemRequest = new HashMap<>();
        addItemRequest.put("productId", "P001");
        addItemRequest.put("quantity", 2);
        addItemRequest.put("price", 49.99);

        RestAssured.given()
            .spec(requestSpec)
            .body(addItemRequest)
        .when()
            .post("/cart/{cartId}/items", cartId)
        .then()
            .spec(responseSpec200)
            .body("items", hasSize(1))
            .body("items[0].productId", equalTo("P001"))
            .body("items[0].quantity", equalTo(2))
            .body("items[0].price", equalTo(49.99f))
            .body("subtotal", equalTo(99.98f))
            .body("total", equalTo(99.98f));
    }

    @Test
    @DisplayName("Update product quantity in cart")
    @Tag("regression")
    public void testUpdateProductQuantity() {
        // Create cart and add product
        String cartId = createCartWithProduct("P002", 1, 25.00);

        // Update quantity
        Map<String, Object> updateRequest = new HashMap<>();
        updateRequest.put("quantity", 3);

        RestAssured.given()
            .spec(requestSpec)
            .body(updateRequest)
        .when()
            .put("/cart/{cartId}/items/{productId}", cartId, "P002")
        .then()
            .spec(responseSpec200)
            .body("items[0].quantity", equalTo(3))
            .body("subtotal", equalTo(75.00f));
    }

    @Test
    @DisplayName("Remove product from cart")
    @Tag("regression")
    public void testRemoveProductFromCart() {
        // Create cart and add product
        String cartId = createCartWithProduct("P003", 2, 15.50);

        // Remove product
        RestAssured.given()
            .spec(requestSpec)
        .when()
            .delete("/cart/{cartId}/items/{productId}", cartId, "P003")
        .then()
            .spec(responseSpec204);

        // Verify cart is empty
        RestAssured.given()
            .spec(requestSpec)
        .when()
            .get("/cart/{cartId}", cartId)
        .then()
            .spec(responseSpec200)
            .body("items", hasSize(0))
            .body("subtotal", equalTo(0.0f));
    }

    @Test
    @DisplayName("Apply promotion code to cart")
    @Tag("regression")
    public void testApplyPromotionCode() {
        // Create cart and add products
        String cartId = createCartWithProduct("P004", 1, 100.00);

        // Apply promotion
        Map<String, Object> promoRequest = new HashMap<>();
        promoRequest.put("promoCode", "SAVE20");

        RestAssured.given()
            .spec(requestSpec)
            .body(promoRequest)
        .when()
            .post("/cart/{cartId}/promotions", cartId)
        .then()
            .spec(responseSpec200)
            .body("appliedPromotions", hasSize(1))
            .body("appliedPromotions[0].code", equalTo("SAVE20"))
            .body("discountAmount", greaterThan(0.0f))
            .body("total", lessThan(100.0f));
    }

    @Test
    @DisplayName("Apply invalid promotion code")
    @Tag("regression")
    public void testApplyInvalidPromotionCode() {
        String cartId = createCartWithProduct("P005", 1, 50.00);

        Map<String, Object> promoRequest = new HashMap<>();
        promoRequest.put("promoCode", "INVALID123");

        RestAssured.given()
            .spec(requestSpec)
            .body(promoRequest)
        .when()
            .post("/cart/{cartId}/promotions", cartId)
        .then()
            .spec(responseSpec400)
            .body("error", equalTo("INVALID_PROMOTION_CODE"))
            .body("message", containsString("promotion code is invalid"));
    }

    @Test
    @DisplayName("Clear entire cart")
    @Tag("regression")
    public void testClearCart() {
        // Create cart with multiple products
        String cartId = createCartWithProduct("P006", 2, 30.00);
        addProductToExistingCart(cartId, "P007", 1, 45.00);

        // Clear cart
        RestAssured.given()
            .spec(requestSpec)
        .when()
            .delete("/cart/{cartId}/items", cartId)
        .then()
            .spec(responseSpec204);

        // Verify cart is empty
        RestAssured.given()
            .spec(requestSpec)
        .when()
            .get("/cart/{cartId}", cartId)
        .then()
            .spec(responseSpec200)
            .body("items", hasSize(0))
            .body("subtotal", equalTo(0.0f))
            .body("total", equalTo(0.0f));
    }

    @Test
    @DisplayName("Get cart summary with multiple items")
    @Tag("regression")
    public void testCartSummaryWithMultipleItems() {
        String cartId = createCartWithProduct("P008", 2, 25.99);
        addProductToExistingCart(cartId, "P009", 1, 15.50);
        addProductToExistingCart(cartId, "P010", 3, 8.99);

        RestAssured.given()
            .spec(requestSpec)
        .when()
            .get("/cart/{cartId}/summary", cartId)
        .then()
            .spec(responseSpec200)
            .body("itemCount", equalTo(6)) // 2 + 1 + 3
            .body("uniqueItemCount", equalTo(3))
            .body("subtotal", equalTo(94.45f)) // (25.99*2) + 15.50 + (8.99*3)
            .body("tax", greaterThan(0.0f))
            .body("total", greaterThan(94.45f));
    }

    @Test
    @DisplayName("Validate cart schema")
    @Tag("contract")
    public void testCartResponseSchema() {
        String cartId = createCartWithProduct("P011", 1, 29.99);

        RestAssured.given()
            .spec(requestSpec)
        .when()
            .get("/cart/{cartId}", cartId)
        .then()
            .spec(responseSpec200)
            .body(matchesJsonSchemaInClasspath("schemas/cart-schema.json"));
    }

    // Helper methods
    private String createCartWithProduct(String productId, int quantity, double price) {
        String customerId = UUID.randomUUID().toString();
        Map<String, Object> cartRequest = new HashMap<>();
        cartRequest.put("customerId", customerId);
        
        String cartId = RestAssured.given()
            .spec(requestSpec)
            .body(cartRequest)
        .when()
            .post("/cart")
        .then()
            .statusCode(201)
            .extract()
            .path("id");

        addProductToExistingCart(cartId, productId, quantity, price);
        return cartId;
    }

    private void addProductToExistingCart(String cartId, String productId, int quantity, double price) {
        Map<String, Object> addItemRequest = new HashMap<>();
        addItemRequest.put("productId", productId);
        addItemRequest.put("quantity", quantity);
        addItemRequest.put("price", price);

        RestAssured.given()
            .spec(requestSpec)
            .body(addItemRequest)
        .when()
            .post("/cart/{cartId}/items", cartId)
        .then()
            .statusCode(200);
    }
}
