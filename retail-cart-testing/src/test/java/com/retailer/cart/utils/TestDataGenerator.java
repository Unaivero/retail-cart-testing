package com.retailer.cart.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.retailer.cart.models.Product;
import com.retailer.cart.models.Promotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class TestDataGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TestDataGenerator.class);
    private static final Random random = new Random();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Product categories and names for realistic data
    private static final Map<String, List<String>> PRODUCT_CATEGORIES = Map.of(
        "Clothing", Arrays.asList(
            "Slim Fit Jeans", "Cotton T-Shirt", "Leather Jacket", "Wool Sweater", 
            "Denim Jacket", "Cargo Pants", "Polo Shirt", "Hoodie", "Blazer", "Dress Shirt"
        ),
        "Footwear", Arrays.asList(
            "Running Shoes", "Leather Boots", "Sneakers", "Sandals", "High Heels", 
            "Loafers", "Hiking Boots", "Ballet Flats", "Dress Shoes", "Canvas Shoes"
        ),
        "Accessories", Arrays.asList(
            "Leather Belt", "Silk Scarf", "Wrist Watch", "Sunglasses", "Handbag", 
            "Wallet", "Baseball Cap", "Jewelry Set", "Backpack", "Tie"
        ),
        "Electronics", Arrays.asList(
            "Wireless Headphones", "Smartphone Case", "Portable Charger", "Bluetooth Speaker", 
            "Smart Watch", "Tablet Stand", "USB Cable", "Screen Protector", "Phone Holder", "Power Bank"
        )
    );
    
    private static final List<String> PROMO_CODE_PREFIXES = Arrays.asList(
        "SAVE", "DISCOUNT", "SPECIAL", "OFFER", "DEAL", "SUMMER", "WINTER", "SPRING", "AUTUMN",
        "WELCOME", "NEWUSER", "LOYALTY", "BUNDLE", "FLASH", "MEGA", "SUPER", "ULTRA"
    );
    
    private static final List<String> CUSTOMER_FIRST_NAMES = Arrays.asList(
        "John", "Jane", "Michael", "Sarah", "David", "Emma", "Chris", "Lisa", "Alex", "Maria",
        "Robert", "Jennifer", "William", "Jessica", "James", "Ashley", "Kevin", "Amanda", "Mark", "Nicole"
    );
    
    private static final List<String> CUSTOMER_LAST_NAMES = Arrays.asList(
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis", "Rodriguez", "Martinez",
        "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore", "Jackson", "Martin"
    );

    /**
     * Generate a random product with realistic data
     */
    public static Product generateRandomProduct() {
        String category = getRandomKey(PRODUCT_CATEGORIES);
        List<String> productNames = PRODUCT_CATEGORIES.get(category);
        String name = getRandomElement(productNames);
        
        String productId = generateProductId();
        double price = generateRandomPrice(category);
        int quantity = ThreadLocalRandom.current().nextInt(1, 6); // 1-5 quantity
        
        return new Product(productId, name, price, quantity);
    }
    
    /**
     * Generate a product with specific constraints
     */
    public static Product generateProduct(String category, double minPrice, double maxPrice) {
        List<String> productNames = PRODUCT_CATEGORIES.getOrDefault(category, 
            PRODUCT_CATEGORIES.get("Clothing"));
        String name = getRandomElement(productNames);
        
        String productId = generateProductId();
        double price = ThreadLocalRandom.current().nextDouble(minPrice, maxPrice);
        price = BigDecimal.valueOf(price).setScale(2, RoundingMode.HALF_UP).doubleValue();
        int quantity = ThreadLocalRandom.current().nextInt(1, 4);
        
        return new Product(productId, name, price, quantity);
    }
    
    /**
     * Generate multiple products for testing
     */
    public static List<Product> generateProductList(int count) {
        List<Product> products = new ArrayList<>();
        Set<String> usedIds = new HashSet<>();
        
        for (int i = 0; i < count; i++) {
            Product product;
            do {
                product = generateRandomProduct();
            } while (usedIds.contains(product.getProductId()));
            
            usedIds.add(product.getProductId());
            products.add(product);
        }
        
        return products;
    }
    
    /**
     * Generate a random promotion code
     */
    public static Promotion generateRandomPromotion() {
        String prefix = getRandomElement(PROMO_CODE_PREFIXES);
        String suffix = String.valueOf(ThreadLocalRandom.current().nextInt(5, 50));
        String code = prefix + suffix;
        
        double discountValue;
        Promotion.DiscountType discountType;
        
        if (random.nextBoolean()) {
            // Percentage discount (5% to 40%)
            discountType = Promotion.DiscountType.PERCENTAGE;
            discountValue = ThreadLocalRandom.current().nextDouble(5.0, 40.0);
            discountValue = BigDecimal.valueOf(discountValue).setScale(1, RoundingMode.HALF_UP).doubleValue();
        } else {
            // Fixed amount discount ($5 to $50)
            discountType = Promotion.DiscountType.FIXED_AMOUNT;
            discountValue = ThreadLocalRandom.current().nextDouble(5.0, 50.0);
            discountValue = BigDecimal.valueOf(discountValue).setScale(2, RoundingMode.HALF_UP).doubleValue();
        }
        
        LocalDate startDate = LocalDate.now().minusDays(ThreadLocalRandom.current().nextInt(0, 30));
        LocalDate endDate = LocalDate.now().plusDays(ThreadLocalRandom.current().nextInt(1, 90));
        
        double minSpend = ThreadLocalRandom.current().nextDouble(0, 100);
        minSpend = BigDecimal.valueOf(minSpend).setScale(2, RoundingMode.HALF_UP).doubleValue();
        
        boolean combinable = random.nextBoolean();
        
        return new Promotion(code, discountType, discountValue, startDate, endDate, minSpend, combinable);
    }
    
    /**
     * Generate promotion with specific parameters
     */
    public static Promotion generatePromotion(String code, Promotion.DiscountType type, 
                                            double value, boolean isActive, boolean combinable) {
        LocalDate startDate = isActive ? 
            LocalDate.now().minusDays(5) : 
            LocalDate.now().plusDays(5);
        LocalDate endDate = isActive ? 
            LocalDate.now().plusDays(30) : 
            LocalDate.now().plusDays(60);
            
        double minSpend = ThreadLocalRandom.current().nextDouble(0, 50);
        minSpend = BigDecimal.valueOf(minSpend).setScale(2, RoundingMode.HALF_UP).doubleValue();
        
        return new Promotion(code, type, value, startDate, endDate, minSpend, combinable);
    }
    
    /**
     * Generate test customer data
     */
    public static Map<String, Object> generateCustomerData() {
        Map<String, Object> customer = new HashMap<>();
        
        String firstName = getRandomElement(CUSTOMER_FIRST_NAMES);
        String lastName = getRandomElement(CUSTOMER_LAST_NAMES);
        String email = firstName.toLowerCase() + "." + lastName.toLowerCase() + 
                      ThreadLocalRandom.current().nextInt(100, 999) + "@example.com";
        
        customer.put("id", UUID.randomUUID().toString());
        customer.put("firstName", firstName);
        customer.put("lastName", lastName);
        customer.put("email", email);
        customer.put("phone", generatePhoneNumber());
        customer.put("address", generateAddress());
        customer.put("dateOfBirth", generateDateOfBirth());
        customer.put("membershipLevel", getRandomElement(Arrays.asList("Bronze", "Silver", "Gold", "Platinum")));
        
        return customer;
    }
    
    /**
     * Generate test cart data with multiple items
     */
    public static Map<String, Object> generateCartData(int itemCount) {
        Map<String, Object> cart = new HashMap<>();
        List<Product> items = generateProductList(itemCount);
        
        cart.put("id", UUID.randomUUID().toString());
        cart.put("customerId", UUID.randomUUID().toString());
        cart.put("currency", "USD");
        cart.put("items", items);
        cart.put("createdAt", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
        cart.put("status", "ACTIVE");
        
        return cart;
    }
    
    /**
     * Generate test data based on scenario requirements
     */
    public static Map<String, Object> generateTestDataForScenario(String scenarioName) {
        Map<String, Object> testData = new HashMap<>();
        
        switch (scenarioName.toLowerCase()) {
            case "empty_cart":
                testData.put("products", Collections.emptyList());
                testData.put("customer", generateCustomerData());
                break;
                
            case "single_item_cart":
                testData.put("products", generateProductList(1));
                testData.put("customer", generateCustomerData());
                testData.put("promotion", generateRandomPromotion());
                break;
                
            case "multiple_items_cart":
                testData.put("products", generateProductList(ThreadLocalRandom.current().nextInt(3, 8)));
                testData.put("customer", generateCustomerData());
                testData.put("promotions", Arrays.asList(
                    generateRandomPromotion(),
                    generateRandomPromotion()
                ));
                break;
                
            case "high_value_cart":
                List<Product> expensiveProducts = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    expensiveProducts.add(generateProduct("Electronics", 200.0, 500.0));
                }
                testData.put("products", expensiveProducts);
                testData.put("customer", generateCustomerData());
                testData.put("promotion", generatePromotion("BIGSPENDER25", 
                    Promotion.DiscountType.PERCENTAGE, 25.0, true, false));
                break;
                
            case "promotion_testing":
                testData.put("products", generateProductList(2));
                testData.put("customer", generateCustomerData());
                testData.put("validPromotion", generatePromotion("VALID20", 
                    Promotion.DiscountType.PERCENTAGE, 20.0, true, true));
                testData.put("expiredPromotion", generatePromotion("EXPIRED10", 
                    Promotion.DiscountType.PERCENTAGE, 10.0, false, true));
                testData.put("invalidPromotion", generatePromotion("INVALID123", 
                    Promotion.DiscountType.FIXED_AMOUNT, 15.0, false, true));
                break;
                
            default:
                testData.put("products", generateProductList(2));
                testData.put("customer", generateCustomerData());
                testData.put("promotion", generateRandomPromotion());
        }
        
        return testData;
    }
    
    /**
     * Load and merge static test data with generated data
     */
    public static Map<String, Object> loadAndEnhanceTestData(String fileName) {
        Map<String, Object> staticData = TestDataReader.loadTestData(fileName);
        Map<String, Object> enhancedData = new HashMap<>(staticData);
        
        // Add generated elements to enhance static data
        enhancedData.put("generatedCustomer", generateCustomerData());
        enhancedData.put("generatedPromotion", generateRandomPromotion());
        enhancedData.put("timestamp", System.currentTimeMillis());
        enhancedData.put("testRunId", UUID.randomUUID().toString());
        
        return enhancedData;
    }
    
    /**
     * Generate test data for API testing
     */
    public static Map<String, Object> generateApiTestData() {
        Map<String, Object> apiData = new HashMap<>();
        
        // Generate request payloads
        Map<String, Object> createCartRequest = new HashMap<>();
        createCartRequest.put("customerId", UUID.randomUUID().toString());
        createCartRequest.put("currency", "USD");
        
        Map<String, Object> addItemRequest = new HashMap<>();
        Product product = generateRandomProduct();
        addItemRequest.put("productId", product.getProductId());
        addItemRequest.put("quantity", product.getQuantity());
        addItemRequest.put("price", product.getPrice());
        
        Map<String, Object> promoRequest = new HashMap<>();
        promoRequest.put("promoCode", generateRandomPromotion().getCode());
        
        apiData.put("createCartRequest", createCartRequest);
        apiData.put("addItemRequest", addItemRequest);
        apiData.put("promoRequest", promoRequest);
        apiData.put("testProduct", product);
        
        return apiData;
    }
    
    /**
     * Generate boundary test data
     */
    public static Map<String, Object> generateBoundaryTestData() {
        Map<String, Object> boundaryData = new HashMap<>();
        
        // Edge case products
        Product minPriceProduct = new Product("MIN001", "Minimal Item", 0.01, 1);
        Product maxPriceProduct = new Product("MAX001", "Luxury Item", 9999.99, 1);
        Product zeroQuantityProduct = new Product("ZERO001", "Zero Quantity Item", 25.99, 0);
        Product maxQuantityProduct = new Product("BULK001", "Bulk Item", 5.99, 999);
        
        boundaryData.put("minPriceProduct", minPriceProduct);
        boundaryData.put("maxPriceProduct", maxPriceProduct);
        boundaryData.put("zeroQuantityProduct", zeroQuantityProduct);
        boundaryData.put("maxQuantityProduct", maxQuantityProduct);
        
        // Edge case promotions
        Promotion minDiscount = generatePromotion("MIN1", Promotion.DiscountType.PERCENTAGE, 0.1, true, true);
        Promotion maxDiscount = generatePromotion("MAX99", Promotion.DiscountType.PERCENTAGE, 99.9, true, false);
        
        boundaryData.put("minDiscountPromo", minDiscount);
        boundaryData.put("maxDiscountPromo", maxDiscount);
        
        return boundaryData;
    }
    
    // Helper methods
    private static String generateProductId() {
        return "P" + String.format("%03d", ThreadLocalRandom.current().nextInt(1, 1000));
    }
    
    private static double generateRandomPrice(String category) {
        double basePrice;
        switch (category) {
            case "Electronics":
                basePrice = ThreadLocalRandom.current().nextDouble(50.0, 300.0);
                break;
            case "Footwear":
                basePrice = ThreadLocalRandom.current().nextDouble(30.0, 200.0);
                break;
            case "Clothing":
                basePrice = ThreadLocalRandom.current().nextDouble(15.0, 150.0);
                break;
            case "Accessories":
                basePrice = ThreadLocalRandom.current().nextDouble(10.0, 100.0);
                break;
            default:
                basePrice = ThreadLocalRandom.current().nextDouble(10.0, 100.0);
        }
        
        return BigDecimal.valueOf(basePrice).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
    
    private static String getRandomKey(Map<String, ?> map) {
        List<String> keys = new ArrayList<>(map.keySet());
        return keys.get(random.nextInt(keys.size()));
    }
    
    private static <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }
    
    private static String generatePhoneNumber() {
        return String.format("(%03d) %03d-%04d",
            ThreadLocalRandom.current().nextInt(200, 999),
            ThreadLocalRandom.current().nextInt(200, 999),
            ThreadLocalRandom.current().nextInt(1000, 9999));
    }
    
    private static Map<String, String> generateAddress() {
        Map<String, String> address = new HashMap<>();
        address.put("street", ThreadLocalRandom.current().nextInt(1, 9999) + " " + 
                   getRandomElement(Arrays.asList("Main St", "Oak Ave", "Park Rd", "First St", "Second Ave")));
        address.put("city", getRandomElement(Arrays.asList("New York", "Los Angeles", "Chicago", "Houston", "Phoenix")));
        address.put("state", getRandomElement(Arrays.asList("NY", "CA", "IL", "TX", "AZ")));
        address.put("zipCode", String.valueOf(ThreadLocalRandom.current().nextInt(10000, 99999)));
        address.put("country", "USA");
        return address;
    }
    
    private static String generateDateOfBirth() {
        LocalDate birthDate = LocalDate.now()
            .minusYears(ThreadLocalRandom.current().nextInt(18, 80))
            .minusDays(ThreadLocalRandom.current().nextInt(0, 365));
        return birthDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
    }
}