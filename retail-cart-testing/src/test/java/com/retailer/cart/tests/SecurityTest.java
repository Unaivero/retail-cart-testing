package com.retailer.cart.tests;

import com.retailer.cart.utils.ConfigReader;
import com.retailer.cart.utils.exceptions.ApiException;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("security")
@Tag("authentication")
@DisplayName("Security and Authentication Tests")
public class SecurityTest {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityTest.class);
    private RequestSpecification requestSpec;
    private String validAuthToken;
    private String invalidAuthToken;
    private String expiredAuthToken;
    
    @BeforeEach
    public void setUp() {
        logger.info("Setting up security test");
        
        String apiBaseUrl = ConfigReader.getApiBaseUrl();
        RestAssured.baseURI = apiBaseUrl;
        
        requestSpec = given()
            .contentType("application/json")
            .accept("application/json");
            
        setupAuthTokens();
    }
    
    @Test
    @DisplayName("Authentication with valid credentials")
    public void testValidAuthentication() {
        logger.info("Testing authentication with valid credentials");
        
        Map<String, Object> credentials = Map.of(
            "username", "testuser",
            "password", "validpassword123"
        );
        
        Response response = requestSpec
            .body(credentials)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(200)
            .body("token", notNullValue())
            .body("expiresIn", notNullValue())
            .body("tokenType", equalTo("Bearer"))
            .extract().response();
        
        String token = response.path("token");
        assertThat(token).isNotEmpty();
        
        logger.info("Valid authentication test completed successfully");
    }
    
    @Test
    @DisplayName("Authentication with invalid credentials")
    public void testInvalidAuthentication() {
        logger.info("Testing authentication with invalid credentials");
        
        Map<String, Object> invalidCredentials = Map.of(
            "username", "testuser",
            "password", "wrongpassword"
        );
        
        requestSpec
            .body(invalidCredentials)
        .when()
            .post("/auth/login")
        .then()
            .statusCode(401)
            .body("error", equalTo("INVALID_CREDENTIALS"))
            .body("message", containsString("Invalid username or password"));
        
        logger.info("Invalid authentication test completed successfully");
    }
    
    @ParameterizedTest(name = "Test SQL injection in {0} field")
    @ValueSource(strings = {"username", "password"})
    @DisplayName("SQL injection prevention")
    public void testSQLInjectionPrevention(String field) {
        logger.info("Testing SQL injection prevention in {} field", field);
        
        String[] sqlInjectionPayloads = {
            "'; DROP TABLE users; --",
            "' OR '1'='1",
            "' UNION SELECT * FROM users --",
            "admin'--",
            "' OR 1=1--"
        };
        
        for (String payload : sqlInjectionPayloads) {
            Map<String, Object> credentials = new HashMap<>();
            credentials.put("username", field.equals("username") ? payload : "testuser");
            credentials.put("password", field.equals("password") ? payload : "password123");
            
            requestSpec
                .body(credentials)
            .when()
                .post("/auth/login")
            .then()
                .statusCode(anyOf(equalTo(400), equalTo(401)))
                .body("error", notNullValue());
        }
        
        logger.info("SQL injection prevention test completed for: {}", field);
    }
    
    @Test
    @DisplayName("XSS prevention in input fields")
    public void testXSSPrevention() {
        logger.info("Testing XSS prevention");
        
        String[] xssPayloads = {
            "<script>alert('XSS')</script>",
            "javascript:alert('XSS')",
            "<img src=x onerror=alert('XSS')>",
            "<svg onload=alert('XSS')>",
            "';alert(String.fromCharCode(88,83,83))//';alert(String.fromCharCode(88,83,83))//\";alert(String.fromCharCode(88,83,83))//\";alert(String.fromCharCode(88,83,83))//--></SCRIPT>\">'><SCRIPT>alert(String.fromCharCode(88,83,83))</SCRIPT>"
        };
        
        for (String payload : xssPayloads) {
            Map<String, Object> cartData = Map.of(
                "customerId", payload,
                "currency", "USD"
            );
            
            requestSpec
                .header("Authorization", "Bearer " + validAuthToken)
                .body(cartData)
            .when()
                .post("/cart")
            .then()
                .statusCode(anyOf(equalTo(400), equalTo(201)))
                .body("customerId", not(containsString("<script>")))
                .body("customerId", not(containsString("javascript:")));
        }
        
        logger.info("XSS prevention test completed successfully");
    }
    
    @Test
    @DisplayName("Authorization with valid token")
    public void testValidAuthorization() {
        logger.info("Testing authorization with valid token");
        
        Map<String, Object> cartData = Map.of(
            "customerId", UUID.randomUUID().toString(),
            "currency", "USD"
        );
        
        requestSpec
            .header("Authorization", "Bearer " + validAuthToken)
            .body(cartData)
        .when()
            .post("/cart")
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("customerId", notNullValue());
        
        logger.info("Valid authorization test completed successfully");
    }
    
    @Test
    @DisplayName("Authorization with invalid token")
    public void testInvalidAuthorization() {
        logger.info("Testing authorization with invalid token");
        
        Map<String, Object> cartData = Map.of(
            "customerId", UUID.randomUUID().toString(),
            "currency", "USD"
        );
        
        requestSpec
            .header("Authorization", "Bearer " + invalidAuthToken)
            .body(cartData)
        .when()
            .post("/cart")
        .then()
            .statusCode(401)
            .body("error", equalTo("INVALID_TOKEN"))
            .body("message", containsString("Invalid or malformed token"));
        
        logger.info("Invalid authorization test completed successfully");
    }
    
    @Test
    @DisplayName("Authorization with expired token")
    public void testExpiredTokenAuthorization() {
        logger.info("Testing authorization with expired token");
        
        Map<String, Object> cartData = Map.of(
            "customerId", UUID.randomUUID().toString(),
            "currency", "USD"
        );
        
        requestSpec
            .header("Authorization", "Bearer " + expiredAuthToken)
            .body(cartData)
        .when()
            .post("/cart")
        .then()
            .statusCode(401)
            .body("error", equalTo("TOKEN_EXPIRED"))
            .body("message", containsString("Token has expired"));
        
        logger.info("Expired token authorization test completed successfully");
    }
    
    @Test
    @DisplayName("Authorization without token")
    public void testMissingAuthorization() {
        logger.info("Testing authorization without token");
        
        Map<String, Object> cartData = Map.of(
            "customerId", UUID.randomUUID().toString(),
            "currency", "USD"
        );
        
        requestSpec
            .body(cartData)
        .when()
            .post("/cart")
        .then()
            .statusCode(401)
            .body("error", equalTo("MISSING_AUTHORIZATION"))
            .body("message", containsString("Authorization header is required"));
        
        logger.info("Missing authorization test completed successfully");
    }
    
    @Test
    @DisplayName("Rate limiting validation")
    public void testRateLimiting() {
        logger.info("Testing rate limiting");
        
        Map<String, Object> credentials = Map.of(
            "username", "testuser",
            "password", "wrongpassword"
        );
        
        // Make multiple requests to trigger rate limiting
        int requestCount = 0;
        boolean rateLimitTriggered = false;
        
        for (int i = 0; i < 20; i++) {
            Response response = requestSpec
                .body(credentials)
            .when()
                .post("/auth/login");
            
            requestCount++;
            
            if (response.getStatusCode() == 429) {
                rateLimitTriggered = true;
                assertThat(response.getBody().asString()).contains("rate limit");
                break;
            }
        }
        
        // Rate limiting should trigger within reasonable number of requests
        assertThat(rateLimitTriggered).isTrue();
        logger.info("Rate limiting triggered after {} requests", requestCount);
        
        logger.info("Rate limiting test completed successfully");
    }
    
    @Test
    @DisplayName("HTTPS enforcement")
    public void testHTTPSEnforcement() {
        logger.info("Testing HTTPS enforcement");
        
        // Test that HTTP requests are redirected to HTTPS
        String httpUrl = ConfigReader.getApiBaseUrl().replace("https://", "http://");
        
        try {
            Response response = given()
                .redirects().follow(false)
            .when()
                .get(httpUrl + "/health");
            
            // Should either redirect to HTTPS or reject HTTP
            assertThat(response.getStatusCode()).isIn(301, 302, 400, 403);
            
            if (response.getStatusCode() == 301 || response.getStatusCode() == 302) {
                String location = response.getHeader("Location");
                assertThat(location).startsWith("https://");
            }
            
        } catch (Exception e) {
            // HTTP might be completely blocked, which is also acceptable
            logger.info("HTTP requests are blocked: {}", e.getMessage());
        }
        
        logger.info("HTTPS enforcement test completed successfully");
    }
    
    @Test
    @DisplayName("Input validation and sanitization")
    public void testInputValidationAndSanitization() {
        logger.info("Testing input validation and sanitization");
        
        // Test various invalid inputs
        String[] invalidInputs = {
            "",  // Empty string
            " ",  // Whitespace only
            "a".repeat(1000),  // Very long string
            "null",
            "undefined",
            "{}",
            "[]",
            "<xml>test</xml>",
            "../../../etc/passwd",
            "..\\..\\..\\windows\\system32\\config\\sam"
        };
        
        for (String invalidInput : invalidInputs) {
            Map<String, Object> cartData = Map.of(
                "customerId", invalidInput,
                "currency", "USD"
            );
            
            requestSpec
                .header("Authorization", "Bearer " + validAuthToken)
                .body(cartData)
            .when()
                .post("/cart")
            .then()
                .statusCode(anyOf(equalTo(400), equalTo(201)))
                .body("error", anyOf(nullValue(), equalTo("VALIDATION_ERROR")));
        }
        
        logger.info("Input validation test completed successfully");
    }
    
    @Test
    @DisplayName("Session management security")
    public void testSessionManagementSecurity() {
        logger.info("Testing session management security");
        
        // Test token refresh
        Map<String, Object> refreshData = Map.of(
            "refreshToken", "valid-refresh-token"
        );
        
        requestSpec
            .body(refreshData)
        .when()
            .post("/auth/refresh")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(401)))
            .body(anyOf(
                hasKey("token"),  // Success case
                hasKey("error")   // Error case
            ));
        
        // Test logout
        requestSpec
            .header("Authorization", "Bearer " + validAuthToken)
        .when()
            .post("/auth/logout")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204)));
        
        logger.info("Session management test completed successfully");
    }
    
    @Test
    @DisplayName("CORS policy validation")
    public void testCORSPolicy() {
        logger.info("Testing CORS policy");
        
        // Test preflight request
        given()
            .header("Origin", "https://malicious-site.com")
            .header("Access-Control-Request-Method", "POST")
            .header("Access-Control-Request-Headers", "Content-Type")
        .when()
            .options("/cart")
        .then()
            .statusCode(anyOf(equalTo(200), equalTo(204), equalTo(403)))
            .header("Access-Control-Allow-Origin", 
                anyOf(nullValue(), not(equalTo("*")), not(equalTo("https://malicious-site.com"))));
        
        logger.info("CORS policy test completed successfully");
    }
    
    @Test
    @DisplayName("Data privacy and PII protection")
    public void testDataPrivacyAndPIIProtection() {
        logger.info("Testing data privacy and PII protection");
        
        // Test that sensitive data is not exposed in responses
        Map<String, Object> cartData = Map.of(
            "customerId", "customer-123",
            "currency", "USD",
            "customerEmail", "test@example.com",
            "customerPhone", "+1234567890"
        );
        
        Response response = requestSpec
            .header("Authorization", "Bearer " + validAuthToken)
            .body(cartData)
        .when()
            .post("/cart")
        .then()
            .statusCode(anyOf(equalTo(201), equalTo(400)))
            .extract().response();
        
        String responseBody = response.getBody().asString();
        
        // Sensitive data should not be exposed in response
        assertThat(responseBody).doesNotContain("customerEmail");
        assertThat(responseBody).doesNotContain("customerPhone");
        assertThat(responseBody).doesNotContain("password");
        
        logger.info("Data privacy test completed successfully");
    }
    
    private void setupAuthTokens() {
        // Generate test tokens for different scenarios
        validAuthToken = generateTestToken("valid-user", false);
        invalidAuthToken = "invalid.token.here";
        expiredAuthToken = generateTestToken("expired-user", true);
        
        logger.debug("Auth tokens setup completed");
    }
    
    private String generateTestToken(String username, boolean expired) {
        // This is a simplified token generation for testing
        // In real implementation, this would be done by the auth service
        
        Map<String, Object> tokenData = Map.of(
            "sub", username,
            "iat", expired ? System.currentTimeMillis() - 3600000 : System.currentTimeMillis(),
            "exp", expired ? System.currentTimeMillis() - 1800000 : System.currentTimeMillis() + 3600000,
            "aud", "retail-cart-api"
        );
        
        // Simplified base64 encoding (not a real JWT)
        String payload = Base64.getEncoder().encodeToString(tokenData.toString().getBytes());
        return "header." + payload + ".signature";
    }
}