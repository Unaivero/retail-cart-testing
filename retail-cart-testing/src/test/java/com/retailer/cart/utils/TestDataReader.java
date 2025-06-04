package com.retailer.cart.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.retailer.cart.models.Product;
import com.retailer.cart.models.Promotion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class TestDataReader {
    private static final Logger logger = LoggerFactory.getLogger(TestDataReader.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final CsvMapper csvMapper = new CsvMapper();
    
    private static final Map<String, Object> dataCache = new HashMap<>();
    private static final String CACHE_PREFIX = "cached_";
    
    /**
     * Load test data from JSON file
     */
    public static Map<String, Object> loadTestData(String fileName) {
        String cacheKey = CACHE_PREFIX + fileName;
        
        if (dataCache.containsKey(cacheKey)) {
            logger.debug("Returning cached data for: {}", fileName);
            return new HashMap<>((Map<String, Object>) dataCache.get(cacheKey));
        }
        
        try {
            String resourcePath = getResourcePath(fileName);
            InputStream inputStream = TestDataReader.class.getClassLoader().getResourceAsStream(resourcePath);
            
            if (inputStream == null) {
                logger.error("Test data file not found: {}", resourcePath);
                throw new RuntimeException("Test data file not found: " + resourcePath);
            }
            
            JsonNode jsonNode = objectMapper.readTree(inputStream);
            Map<String, Object> data = objectMapper.convertValue(jsonNode, Map.class);
            
            // Cache the data for future use
            dataCache.put(cacheKey, new HashMap<>(data));
            logger.info("Loaded test data from: {}", fileName);
            
            return data;
            
        } catch (IOException e) {
            logger.error("Failed to load test data from: {}", fileName, e);
            throw new RuntimeException("Failed to load test data from: " + fileName, e);
        }
    }
    
    /**
     * Load products from CSV file
     */
    public static List<Product> loadProductsFromCsv(String fileName) {
        List<Product> products = new ArrayList<>();
        
        try {
            String resourcePath = getResourcePath(fileName);
            InputStream inputStream = TestDataReader.class.getClassLoader().getResourceAsStream(resourcePath);
            
            if (inputStream == null) {
                logger.error("CSV file not found: {}", resourcePath);
                throw new RuntimeException("CSV file not found: " + resourcePath);
            }
            
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            List<Map<String, String>> csvData = csvMapper.readerFor(Map.class)
                .with(schema)
                .readValues(inputStream)
                .readAll();
            
            for (Map<String, String> row : csvData) {
                try {
                    Product product = createProductFromCsvRow(row);
                    products.add(product);
                } catch (Exception e) {
                    logger.warn("Failed to create product from CSV row: {}", row, e);
                }
            }
            
            logger.info("Loaded {} products from CSV: {}", products.size(), fileName);
            
        } catch (IOException e) {
            logger.error("Failed to load products from CSV: {}", fileName, e);
            throw new RuntimeException("Failed to load products from CSV: " + fileName, e);
        }
        
        return products;
    }
    
    /**
     * Load promotions from CSV file
     */
    public static List<Promotion> loadPromotionsFromCsv(String fileName) {
        List<Promotion> promotions = new ArrayList<>();
        
        try {
            String resourcePath = getResourcePath(fileName);
            InputStream inputStream = TestDataReader.class.getClassLoader().getResourceAsStream(resourcePath);
            
            if (inputStream == null) {
                logger.error("CSV file not found: {}", resourcePath);
                throw new RuntimeException("CSV file not found: " + resourcePath);
            }
            
            CsvSchema schema = CsvSchema.emptySchema().withHeader();
            List<Map<String, String>> csvData = csvMapper.readerFor(Map.class)
                .with(schema)
                .readValues(inputStream)
                .readAll();
            
            for (Map<String, String> row : csvData) {
                try {
                    Promotion promotion = createPromotionFromCsvRow(row);
                    promotions.add(promotion);
                } catch (Exception e) {
                    logger.warn("Failed to create promotion from CSV row: {}", row, e);
                }
            }
            
            logger.info("Loaded {} promotions from CSV: {}", promotions.size(), fileName);
            
        } catch (IOException e) {
            logger.error("Failed to load promotions from CSV: {}", fileName, e);
            throw new RuntimeException("Failed to load promotions from CSV: " + fileName, e);
        }
        
        return promotions;
    }
    
    /**
     * Load specific test data by key from JSON file
     */
    public static Object loadTestDataByKey(String fileName, String key) {
        Map<String, Object> data = loadTestData(fileName);
        Object value = data.get(key);
        
        if (value == null) {
            logger.warn("Key '{}' not found in test data file: {}", key, fileName);
        }
        
        return value;
    }
    
    /**
     * Load test data with environment-specific overrides
     */
    public static Map<String, Object> loadTestDataWithEnvironment(String fileName) {
        Map<String, Object> baseData = loadTestData(fileName);
        String environment = ConfigReader.getEnvironmentName();
        
        // Try to load environment-specific overrides
        String envFileName = fileName.replace(".json", "_" + environment + ".json");
        
        try {
            Map<String, Object> envData = loadTestData(envFileName);
            baseData.putAll(envData); // Override base data with environment-specific data
            logger.info("Applied environment-specific data from: {}", envFileName);
        } catch (RuntimeException e) {
            logger.debug("No environment-specific data found for: {}", envFileName);
        }
        
        return baseData;
    }
    
    /**
     * Get all available product data
     */
    public static List<Product> getAllProducts() {
        List<Product> allProducts = new ArrayList<>();
        
        // Load from JSON
        try {
            Map<String, Object> jsonData = loadTestData("products.json");
            if (jsonData.containsKey("products")) {
                List<Map<String, Object>> productMaps = (List<Map<String, Object>>) jsonData.get("products");
                for (Map<String, Object> productMap : productMaps) {
                    allProducts.add(createProductFromMap(productMap));
                }
            }
        } catch (RuntimeException e) {
            logger.debug("No JSON product data found");
        }
        
        // Load from CSV
        try {
            List<Product> csvProducts = loadProductsFromCsv("products.csv");
            allProducts.addAll(csvProducts);
        } catch (RuntimeException e) {
            logger.debug("No CSV product data found");
        }
        
        logger.info("Loaded total of {} products from all sources", allProducts.size());
        return allProducts;
    }
    
    /**
     * Get all available promotion data
     */
    public static List<Promotion> getAllPromotions() {
        List<Promotion> allPromotions = new ArrayList<>();
        
        // Load from JSON
        try {
            Map<String, Object> jsonData = loadTestData("promotions.json");
            if (jsonData.containsKey("promotions")) {
                List<Map<String, Object>> promoMaps = (List<Map<String, Object>>) jsonData.get("promotions");
                for (Map<String, Object> promoMap : promoMaps) {
                    allPromotions.add(createPromotionFromMap(promoMap));
                }
            }
        } catch (RuntimeException e) {
            logger.debug("No JSON promotion data found");
        }
        
        // Load from CSV
        try {
            List<Promotion> csvPromotions = loadPromotionsFromCsv("promotions.csv");
            allPromotions.addAll(csvPromotions);
        } catch (RuntimeException e) {
            logger.debug("No CSV promotion data found");
        }
        
        logger.info("Loaded total of {} promotions from all sources", allPromotions.size());
        return allPromotions;
    }
    
    /**
     * Find product by ID
     */
    public static Product findProductById(String productId) {
        List<Product> products = getAllProducts();
        return products.stream()
            .filter(product -> product.getProductId().equals(productId))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Find promotion by code
     */
    public static Promotion findPromotionByCode(String code) {
        List<Promotion> promotions = getAllPromotions();
        return promotions.stream()
            .filter(promotion -> promotion.getCode().equals(code))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Get products by category
     */
    public static List<Product> getProductsByCategory(String category) {
        List<Product> products = getAllProducts();
        return products.stream()
            .filter(product -> product.getName().toLowerCase().contains(category.toLowerCase()))
            .toList();
    }
    
    /**
     * Get active promotions
     */
    public static List<Promotion> getActivePromotions() {
        List<Promotion> promotions = getAllPromotions();
        LocalDate today = LocalDate.now();
        
        return promotions.stream()
            .filter(promotion -> !promotion.getStartDate().isAfter(today) && 
                               !promotion.getEndDate().isBefore(today))
            .toList();
    }
    
    /**
     * Clear data cache
     */
    public static void clearCache() {
        dataCache.clear();
        logger.info("Test data cache cleared");
    }
    
    /**
     * Get cache statistics
     */
    public static Map<String, Object> getCacheStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("cachedFiles", dataCache.size());
        stats.put("cacheKeys", new ArrayList<>(dataCache.keySet()));
        return stats;
    }
    
    // Helper methods
    private static String getResourcePath(String fileName) {
        String basePath = ConfigReader.getProperty("test.data.path", "testdata");
        return basePath + "/" + fileName;
    }
    
    private static Product createProductFromCsvRow(Map<String, String> row) {
        String productId = row.get("productId");
        String name = row.get("name");
        double price = Double.parseDouble(row.get("price"));
        int quantity = Integer.parseInt(row.getOrDefault("quantity", "1"));
        
        return new Product(productId, name, price, quantity);
    }
    
    private static Promotion createPromotionFromCsvRow(Map<String, String> row) {
        String code = row.get("code");
        Promotion.DiscountType type = Promotion.DiscountType.valueOf(row.get("discountType"));
        double value = Double.parseDouble(row.get("discountValue"));
        
        LocalDate startDate = LocalDate.parse(row.get("startDate"), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse(row.get("endDate"), DateTimeFormatter.ISO_LOCAL_DATE);
        
        double minSpend = Double.parseDouble(row.getOrDefault("minSpend", "0.0"));
        boolean combinable = Boolean.parseBoolean(row.getOrDefault("combinable", "true"));
        
        return new Promotion(code, type, value, startDate, endDate, minSpend, combinable);
    }
    
    private static Product createProductFromMap(Map<String, Object> productMap) {
        String productId = (String) productMap.get("productId");
        String name = (String) productMap.get("name");
        double price = ((Number) productMap.get("price")).doubleValue();
        int quantity = ((Number) productMap.getOrDefault("quantity", 1)).intValue();
        
        return new Product(productId, name, price, quantity);
    }
    
    private static Promotion createPromotionFromMap(Map<String, Object> promoMap) {
        String code = (String) promoMap.get("code");
        Promotion.DiscountType type = Promotion.DiscountType.valueOf((String) promoMap.get("discountType"));
        double value = ((Number) promoMap.get("discountValue")).doubleValue();
        
        LocalDate startDate = LocalDate.parse((String) promoMap.get("startDate"), DateTimeFormatter.ISO_LOCAL_DATE);
        LocalDate endDate = LocalDate.parse((String) promoMap.get("endDate"), DateTimeFormatter.ISO_LOCAL_DATE);
        
        double minSpend = ((Number) promoMap.getOrDefault("minSpend", 0.0)).doubleValue();
        boolean combinable = (Boolean) promoMap.getOrDefault("combinable", true);
        
        return new Promotion(code, type, value, startDate, endDate, minSpend, combinable);
    }
}