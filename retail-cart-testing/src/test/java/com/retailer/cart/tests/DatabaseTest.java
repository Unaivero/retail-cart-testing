package com.retailer.cart.tests;

import com.retailer.cart.utils.ConfigReader;
import com.retailer.cart.utils.exceptions.TestDataException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Tag("database")
@Tag("persistence")
@DisplayName("Database Persistence and Data Validation Tests")
public class DatabaseTest {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseTest.class);
    private Connection connection;
    private String testCartId;
    private String testCustomerId;
    
    @BeforeEach
    public void setUp() {
        logger.info("Setting up database test");
        establishDatabaseConnection();
        setupTestData();
    }
    
    @AfterEach
    public void tearDown() {
        cleanupTestData();
        closeDatabaseConnection();
        logger.info("Database test cleanup completed");
    }
    
    @Test
    @DisplayName("Database connection and basic operations")
    public void testDatabaseConnection() {
        logger.info("Testing database connection and basic operations");
        
        assertThat(connection).isNotNull();
        
        // Test basic query execution
        assertDoesNotThrow(() -> {
            try (Statement stmt = connection.createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT 1 as test_column");
                assertThat(rs.next()).isTrue();
                assertThat(rs.getInt("test_column")).isEqualTo(1);
            }
        });
        
        logger.info("Database connection test completed successfully");
    }
    
    @Test
    @DisplayName("Cart data persistence")
    public void testCartDataPersistence() {
        logger.info("Testing cart data persistence");
        
        // Create a cart via API and verify database storage
        Map<String, Object> cartData = createCartInDatabase();
        
        // Verify cart exists in database
        String cartId = (String) cartData.get("id");
        Map<String, Object> storedCart = getCartFromDatabase(cartId);
        
        assertThat(storedCart).isNotEmpty();
        assertThat(storedCart.get("customer_id")).isEqualTo(cartData.get("customer_id"));
        assertThat(storedCart.get("currency")).isEqualTo(cartData.get("currency"));
        assertThat(storedCart.get("status")).isEqualTo("ACTIVE");
        
        logger.info("Cart data persistence test completed successfully");
    }
    
    @Test
    @DisplayName("Cart items persistence")
    public void testCartItemsPersistence() {
        logger.info("Testing cart items persistence");
        
        // Add items to cart and verify database storage
        Map<String, Object> itemData = Map.of(
            "product_id", "P001",
            "quantity", 2,
            "price", 49.99,
            "line_total", 99.98
        );
        
        addItemToCartInDatabase(testCartId, itemData);
        
        // Verify item exists in database
        List<Map<String, Object>> cartItems = getCartItemsFromDatabase(testCartId);
        
        assertThat(cartItems).hasSize(1);
        Map<String, Object> storedItem = cartItems.get(0);
        assertThat(storedItem.get("product_id")).isEqualTo("P001");
        assertThat(((Number) storedItem.get("quantity")).intValue()).isEqualTo(2);
        assertThat(((Number) storedItem.get("price")).doubleValue()).isEqualTo(49.99);
        
        logger.info("Cart items persistence test completed successfully");
    }
    
    @Test
    @DisplayName("Promotion data persistence")
    public void testPromotionDataPersistence() {
        logger.info("Testing promotion data persistence");
        
        // Apply promotion to cart and verify database storage
        Map<String, Object> promotionData = Map.of(
            "code", "SAVE20",
            "discount_type", "PERCENTAGE",
            "discount_value", 20.0,
            "discount_amount", 19.996
        );
        
        applyPromotionToCartInDatabase(testCartId, promotionData);
        
        // Verify promotion exists in database
        List<Map<String, Object>> appliedPromotions = getCartPromotionsFromDatabase(testCartId);
        
        assertThat(appliedPromotions).hasSize(1);
        Map<String, Object> storedPromotion = appliedPromotions.get(0);
        assertThat(storedPromotion.get("code")).isEqualTo("SAVE20");
        assertThat(storedPromotion.get("discount_type")).isEqualTo("PERCENTAGE");
        
        logger.info("Promotion data persistence test completed successfully");
    }
    
    @ParameterizedTest(name = "Test data integrity for {0} operations")
    @ValueSource(strings = {"INSERT", "UPDATE", "DELETE"})
    @DisplayName("Data integrity validation")
    public void testDataIntegrity(String operation) {
        logger.info("Testing data integrity for {} operations", operation);
        
        switch (operation) {
            case "INSERT":
                testInsertDataIntegrity();
                break;
            case "UPDATE":
                testUpdateDataIntegrity();
                break;
            case "DELETE":
                testDeleteDataIntegrity();
                break;
        }
        
        logger.info("Data integrity test completed for: {}", operation);
    }
    
    @Test
    @DisplayName("Transaction rollback on failure")
    public void testTransactionRollback() {
        logger.info("Testing transaction rollback on failure");
        
        try {
            connection.setAutoCommit(false);
            
            // Simulate a transaction that should fail
            try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT INTO cart_items (cart_id, product_id, quantity, price) VALUES (?, ?, ?, ?)")) {
                
                // Insert valid item
                stmt.setString(1, testCartId);
                stmt.setString(2, "P001");
                stmt.setInt(3, 1);
                stmt.setDouble(4, 25.99);
                stmt.executeUpdate();
                
                // Insert invalid item (should cause constraint violation)
                stmt.setString(1, "invalid-cart-id");
                stmt.setString(2, "P002");
                stmt.setInt(3, -1); // Invalid quantity
                stmt.setDouble(4, -10.00); // Invalid price
                stmt.executeUpdate();
                
                connection.commit();
                
            } catch (SQLException e) {
                logger.info("Expected exception caught: {}", e.getMessage());
                connection.rollback();
            }
            
            // Verify rollback occurred - no items should be in the cart
            List<Map<String, Object>> cartItems = getCartItemsFromDatabase(testCartId);
            assertThat(cartItems).isEmpty();
            
        } catch (SQLException e) {
            throw new TestDataException("database", "transaction", "Transaction test failed", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.warn("Failed to reset auto-commit", e);
            }
        }
        
        logger.info("Transaction rollback test completed successfully");
    }
    
    @Test
    @DisplayName("Concurrent access and locking")
    public void testConcurrentAccessAndLocking() {
        logger.info("Testing concurrent access and locking");
        
        // This test would typically involve multiple threads
        // For simplicity, we'll test basic locking behavior
        
        try {
            // Start a transaction but don't commit
            connection.setAutoCommit(false);
            
            try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE carts SET updated_at = ? WHERE id = ?")) {
                stmt.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
                stmt.setString(2, testCartId);
                int updated = stmt.executeUpdate();
                assertThat(updated).isEqualTo(1);
            }
            
            // Simulate concurrent access (in real scenario, this would be another connection)
            // For now, we'll just verify the transaction is active
            assertThat(connection.getAutoCommit()).isFalse();
            
            connection.commit();
            
        } catch (SQLException e) {
            throw new TestDataException("database", "concurrency", "Concurrency test failed", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                logger.warn("Failed to reset auto-commit", e);
            }
        }
        
        logger.info("Concurrent access test completed successfully");
    }
    
    @Test
    @DisplayName("Database performance validation")
    public void testDatabasePerformance() {
        logger.info("Testing database performance");
        
        // Test query performance
        long startTime = System.currentTimeMillis();
        
        for (int i = 0; i < 100; i++) {
            getCartFromDatabase(testCartId);
        }
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        double avgTime = totalTime / 100.0;
        
        logger.info("Average query time: {}ms", avgTime);
        
        // Performance should be reasonable (less than 100ms per query on average)
        assertThat(avgTime).isLessThan(100.0);
        
        logger.info("Database performance test completed successfully");
    }
    
    private void establishDatabaseConnection() {
        try {
            String dbUrl = ConfigReader.getProperty("db.url", "jdbc:h2:mem:testdb");
            String dbUsername = ConfigReader.getProperty("db.username", "sa");
            String dbPassword = ConfigReader.getProperty("db.password", "");
            
            connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
            
            // Create test tables if they don't exist (for H2 in-memory database)
            createTestTables();
            
            logger.info("Database connection established: {}", dbUrl);
            
        } catch (SQLException e) {
            throw new TestDataException("database", "connection", "Failed to establish database connection", e);
        }
    }
    
    private void createTestTables() throws SQLException {
        String[] createTableQueries = {
            """
            CREATE TABLE IF NOT EXISTS carts (
                id VARCHAR(255) PRIMARY KEY,
                customer_id VARCHAR(255) NOT NULL,
                currency VARCHAR(3) DEFAULT 'USD',
                subtotal DECIMAL(10,2) DEFAULT 0.00,
                discount_amount DECIMAL(10,2) DEFAULT 0.00,
                total DECIMAL(10,2) DEFAULT 0.00,
                status VARCHAR(50) DEFAULT 'ACTIVE',
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS cart_items (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                cart_id VARCHAR(255) NOT NULL,
                product_id VARCHAR(255) NOT NULL,
                quantity INT NOT NULL CHECK (quantity > 0),
                price DECIMAL(10,2) NOT NULL CHECK (price >= 0),
                line_total DECIMAL(10,2) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS cart_promotions (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                cart_id VARCHAR(255) NOT NULL,
                code VARCHAR(255) NOT NULL,
                discount_type VARCHAR(50) NOT NULL,
                discount_value DECIMAL(10,2) NOT NULL,
                discount_amount DECIMAL(10,2) NOT NULL,
                applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE
            )
            """
        };
        
        try (Statement stmt = connection.createStatement()) {
            for (String query : createTableQueries) {
                stmt.execute(query);
            }
        }
        
        logger.debug("Test tables created successfully");
    }
    
    private void setupTestData() {
        testCartId = "test-cart-" + UUID.randomUUID().toString();
        testCustomerId = "test-customer-" + UUID.randomUUID().toString();
        
        // Create test cart
        Map<String, Object> cartData = Map.of(
            "id", testCartId,
            "customer_id", testCustomerId,
            "currency", "USD"
        );
        
        createCartInDatabase(cartData);
        logger.debug("Test data setup completed");
    }
    
    private void cleanupTestData() {
        if (connection != null) {
            try {
                // Clean up test data
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM carts WHERE id = ?")) {
                    stmt.setString(1, testCartId);
                    stmt.executeUpdate();
                }
                logger.debug("Test data cleanup completed");
            } catch (SQLException e) {
                logger.warn("Failed to cleanup test data", e);
            }
        }
    }
    
    private void closeDatabaseConnection() {
        if (connection != null) {
            try {
                connection.close();
                logger.debug("Database connection closed");
            } catch (SQLException e) {
                logger.warn("Failed to close database connection", e);
            }
        }
    }
    
    private Map<String, Object> createCartInDatabase() {
        return createCartInDatabase(Map.of(
            "id", "cart-" + UUID.randomUUID().toString(),
            "customer_id", "customer-" + UUID.randomUUID().toString(),
            "currency", "USD"
        ));
    }
    
    private Map<String, Object> createCartInDatabase(Map<String, Object> cartData) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO carts (id, customer_id, currency) VALUES (?, ?, ?)")) {
            
            stmt.setString(1, (String) cartData.get("id"));
            stmt.setString(2, (String) cartData.get("customer_id"));
            stmt.setString(3, (String) cartData.get("currency"));
            stmt.executeUpdate();
            
            return cartData;
            
        } catch (SQLException e) {
            throw new TestDataException("database", "cart creation", "Failed to create cart", e);
        }
    }
    
    private Map<String, Object> getCartFromDatabase(String cartId) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT * FROM carts WHERE id = ?")) {
            
            stmt.setString(1, cartId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Map<String, Object> cart = new HashMap<>();
                cart.put("id", rs.getString("id"));
                cart.put("customer_id", rs.getString("customer_id"));
                cart.put("currency", rs.getString("currency"));
                cart.put("subtotal", rs.getDouble("subtotal"));
                cart.put("discount_amount", rs.getDouble("discount_amount"));
                cart.put("total", rs.getDouble("total"));
                cart.put("status", rs.getString("status"));
                cart.put("created_at", rs.getTimestamp("created_at"));
                cart.put("updated_at", rs.getTimestamp("updated_at"));
                return cart;
            }
            
            return Collections.emptyMap();
            
        } catch (SQLException e) {
            throw new TestDataException("database", "cart retrieval", "Failed to get cart", e);
        }
    }
    
    private void addItemToCartInDatabase(String cartId, Map<String, Object> itemData) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO cart_items (cart_id, product_id, quantity, price, line_total) VALUES (?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, cartId);
            stmt.setString(2, (String) itemData.get("product_id"));
            stmt.setInt(3, (Integer) itemData.get("quantity"));
            stmt.setDouble(4, (Double) itemData.get("price"));
            stmt.setDouble(5, (Double) itemData.get("line_total"));
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new TestDataException("database", "item addition", "Failed to add item to cart", e);
        }
    }
    
    private List<Map<String, Object>> getCartItemsFromDatabase(String cartId) {
        List<Map<String, Object>> items = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT * FROM cart_items WHERE cart_id = ?")) {
            
            stmt.setString(1, cartId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", rs.getLong("id"));
                item.put("cart_id", rs.getString("cart_id"));
                item.put("product_id", rs.getString("product_id"));
                item.put("quantity", rs.getInt("quantity"));
                item.put("price", rs.getDouble("price"));
                item.put("line_total", rs.getDouble("line_total"));
                item.put("created_at", rs.getTimestamp("created_at"));
                items.add(item);
            }
            
        } catch (SQLException e) {
            throw new TestDataException("database", "items retrieval", "Failed to get cart items", e);
        }
        
        return items;
    }
    
    private void applyPromotionToCartInDatabase(String cartId, Map<String, Object> promotionData) {
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO cart_promotions (cart_id, code, discount_type, discount_value, discount_amount) VALUES (?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, cartId);
            stmt.setString(2, (String) promotionData.get("code"));
            stmt.setString(3, (String) promotionData.get("discount_type"));
            stmt.setDouble(4, (Double) promotionData.get("discount_value"));
            stmt.setDouble(5, (Double) promotionData.get("discount_amount"));
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            throw new TestDataException("database", "promotion application", "Failed to apply promotion", e);
        }
    }
    
    private List<Map<String, Object>> getCartPromotionsFromDatabase(String cartId) {
        List<Map<String, Object>> promotions = new ArrayList<>();
        
        try (PreparedStatement stmt = connection.prepareStatement(
            "SELECT * FROM cart_promotions WHERE cart_id = ?")) {
            
            stmt.setString(1, cartId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Map<String, Object> promotion = new HashMap<>();
                promotion.put("id", rs.getLong("id"));
                promotion.put("cart_id", rs.getString("cart_id"));
                promotion.put("code", rs.getString("code"));
                promotion.put("discount_type", rs.getString("discount_type"));
                promotion.put("discount_value", rs.getDouble("discount_value"));
                promotion.put("discount_amount", rs.getDouble("discount_amount"));
                promotion.put("applied_at", rs.getTimestamp("applied_at"));
                promotions.add(promotion);
            }
            
        } catch (SQLException e) {
            throw new TestDataException("database", "promotions retrieval", "Failed to get cart promotions", e);
        }
        
        return promotions;
    }
    
    private void testInsertDataIntegrity() {
        // Test data constraints and validations on insert
        try (PreparedStatement stmt = connection.prepareStatement(
            "INSERT INTO cart_items (cart_id, product_id, quantity, price, line_total) VALUES (?, ?, ?, ?, ?)")) {
            
            stmt.setString(1, testCartId);
            stmt.setString(2, "P002");
            stmt.setInt(3, 1);
            stmt.setDouble(4, 15.99);
            stmt.setDouble(5, 15.99);
            int inserted = stmt.executeUpdate();
            
            assertThat(inserted).isEqualTo(1);
            
        } catch (SQLException e) {
            throw new TestDataException("database", "insert integrity", "Insert integrity test failed", e);
        }
    }
    
    private void testUpdateDataIntegrity() {
        // Test data integrity on updates
        try (PreparedStatement stmt = connection.prepareStatement(
            "UPDATE carts SET total = ? WHERE id = ?")) {
            
            stmt.setDouble(1, 99.99);
            stmt.setString(2, testCartId);
            int updated = stmt.executeUpdate();
            
            assertThat(updated).isEqualTo(1);
            
            // Verify the update
            Map<String, Object> cart = getCartFromDatabase(testCartId);
            assertThat(((Number) cart.get("total")).doubleValue()).isEqualTo(99.99);
            
        } catch (SQLException e) {
            throw new TestDataException("database", "update integrity", "Update integrity test failed", e);
        }
    }
    
    private void testDeleteDataIntegrity() {
        // Test cascading deletes and referential integrity
        
        // First add an item to test cascade delete
        addItemToCartInDatabase(testCartId, Map.of(
            "product_id", "P003",
            "quantity", 1,
            "price", 25.99,
            "line_total", 25.99
        ));
        
        // Verify item exists
        List<Map<String, Object>> itemsBefore = getCartItemsFromDatabase(testCartId);
        assertThat(itemsBefore).hasSize(1);
        
        // Delete the cart (should cascade to items)
        try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM carts WHERE id = ?")) {
            stmt.setString(1, testCartId);
            int deleted = stmt.executeUpdate();
            assertThat(deleted).isEqualTo(1);
        } catch (SQLException e) {
            throw new TestDataException("database", "delete integrity", "Delete integrity test failed", e);
        }
        
        // Verify cascade delete worked
        List<Map<String, Object>> itemsAfter = getCartItemsFromDatabase(testCartId);
        assertThat(itemsAfter).isEmpty();
        
        // Recreate test cart for cleanup
        setupTestData();
    }
}