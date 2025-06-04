package com.retailer.cart.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

/**
 * Enhanced logging utility for test framework with structured logging support
 */
public class TestLogger {
    
    private static final Logger logger = LoggerFactory.getLogger(TestLogger.class);
    
    // Markers for different log categories
    public static final Marker CHROME = MarkerFactory.getMarker("CHROME");
    public static final Marker FIREFOX = MarkerFactory.getMarker("FIREFOX");
    public static final Marker EDGE = MarkerFactory.getMarker("EDGE");
    public static final Marker SAFARI = MarkerFactory.getMarker("SAFARI");
    public static final Marker PERFORMANCE = MarkerFactory.getMarker("PERFORMANCE");
    public static final Marker SECURITY = MarkerFactory.getMarker("SECURITY");
    public static final Marker API = MarkerFactory.getMarker("API");
    public static final Marker UI = MarkerFactory.getMarker("UI");
    public static final Marker DATABASE = MarkerFactory.getMarker("DATABASE");
    
    // MDC Keys for structured logging
    public static final String MDC_TEST_CLASS = "testClass";
    public static final String MDC_TEST_METHOD = "testMethod";
    public static final String MDC_BROWSER = "browser";
    public static final String MDC_ENVIRONMENT = "environment";
    public static final String MDC_TEST_ID = "testId";
    public static final String MDC_SESSION_ID = "sessionId";
    public static final String MDC_USER_ID = "userId";
    public static final String MDC_EXECUTION_ID = "executionId";
    
    /**
     * Initialize test context for logging
     */
    public static void initTestContext(String testClass, String testMethod) {
        MDC.put(MDC_TEST_CLASS, testClass);
        MDC.put(MDC_TEST_METHOD, testMethod);
        MDC.put(MDC_ENVIRONMENT, ConfigReader.getEnvironmentName());
        MDC.put(MDC_EXECUTION_ID, java.util.UUID.randomUUID().toString());
        
        logger.info("Test context initialized: {}.{}", testClass, testMethod);
    }
    
    /**
     * Set browser context for cross-browser testing
     */
    public static void setBrowserContext(String browserName) {
        MDC.put(MDC_BROWSER, browserName);
        logger.debug("Browser context set: {}", browserName);
    }
    
    /**
     * Set user context for test execution
     */
    public static void setUserContext(String userId) {
        MDC.put(MDC_USER_ID, userId);
        logger.debug("User context set: {}", userId);
    }
    
    /**
     * Set session context
     */
    public static void setSessionContext(String sessionId) {
        MDC.put(MDC_SESSION_ID, sessionId);
        logger.debug("Session context set: {}", sessionId);
    }
    
    /**
     * Clear test context
     */
    public static void clearTestContext() {
        String testClass = MDC.get(MDC_TEST_CLASS);
        String testMethod = MDC.get(MDC_TEST_METHOD);
        
        MDC.clear();
        
        logger.info("Test context cleared: {}.{}", testClass, testMethod);
    }
    
    /**
     * Log test start
     */
    public static void logTestStart(String testName) {
        logger.info("🚀 TEST STARTED: {}", testName);
    }
    
    /**
     * Log test completion
     */
    public static void logTestComplete(String testName, boolean passed) {
        if (passed) {
            logger.info("✅ TEST PASSED: {}", testName);
        } else {
            logger.error("❌ TEST FAILED: {}", testName);
        }
    }
    
    /**
     * Log test step
     */
    public static void logTestStep(String stepDescription) {
        logger.info("📋 STEP: {}", stepDescription);
    }
    
    /**
     * Log browser-specific events
     */
    public static void logBrowserEvent(String browserName, String event) {
        Marker browserMarker = getBrowserMarker(browserName);
        logger.info(browserMarker, "🌐 {}: {}", browserName.toUpperCase(), event);
    }
    
    /**
     * Log performance metrics
     */
    public static void logPerformanceMetric(String metricName, long value, String unit) {
        logger.info(PERFORMANCE, "⏱️ PERFORMANCE: {} = {} {}", metricName, value, unit);
    }
    
    /**
     * Log API request/response
     */
    public static void logApiRequest(String method, String endpoint, int statusCode) {
        logger.info(API, "🔗 API: {} {} -> {}", method, endpoint, statusCode);
    }
    
    /**
     * Log API request with timing
     */
    public static void logApiRequest(String method, String endpoint, int statusCode, long responseTime) {
        logger.info(API, "🔗 API: {} {} -> {} ({}ms)", method, endpoint, statusCode, responseTime);
    }
    
    /**
     * Log UI interaction
     */
    public static void logUIInteraction(String action, String element) {
        logger.info(UI, "🖱️ UI: {} on {}", action, element);
    }
    
    /**
     * Log database operation
     */
    public static void logDatabaseOperation(String operation, String table, boolean success) {
        if (success) {
            logger.info(DATABASE, "🗄️ DB: {} on {} - SUCCESS", operation, table);
        } else {
            logger.error(DATABASE, "🗄️ DB: {} on {} - FAILED", operation, table);
        }
    }
    
    /**
     * Log security event
     */
    public static void logSecurityEvent(String eventType, String details) {
        logger.info(SECURITY, "🔒 SECURITY: {} - {}", eventType, details);
    }
    
    /**
     * Log error with context
     */
    public static void logError(String message, Throwable throwable) {
        logger.error("❌ ERROR: {} - Context: [Class: {}, Method: {}, Browser: {}]", 
                    message, 
                    MDC.get(MDC_TEST_CLASS), 
                    MDC.get(MDC_TEST_METHOD), 
                    MDC.get(MDC_BROWSER), 
                    throwable);
    }
    
    /**
     * Log warning with context
     */
    public static void logWarning(String message) {
        logger.warn("⚠️ WARNING: {} - Context: [Class: {}, Method: {}, Browser: {}]", 
                   message, 
                   MDC.get(MDC_TEST_CLASS), 
                   MDC.get(MDC_TEST_METHOD), 
                   MDC.get(MDC_BROWSER));
    }
    
    /**
     * Log debug information
     */
    public static void logDebug(String message) {
        logger.debug("🔍 DEBUG: {}", message);
    }
    
    /**
     * Log test data information
     */
    public static void logTestData(String dataType, String data) {
        logger.debug("📊 TEST DATA [{}]: {}", dataType, data);
    }
    
    /**
     * Log screenshot capture
     */
    public static void logScreenshot(String screenshotPath) {
        logger.info("📸 SCREENSHOT: {}", screenshotPath);
    }
    
    /**
     * Log test environment details
     */
    public static void logEnvironmentInfo() {
        String browser = MDC.get(MDC_BROWSER);
        String environment = MDC.get(MDC_ENVIRONMENT);
        String os = System.getProperty("os.name");
        String javaVersion = System.getProperty("java.version");
        
        logger.info("🌍 ENVIRONMENT INFO:");
        logger.info("  - Browser: {}", browser != null ? browser : "Not set");
        logger.info("  - Environment: {}", environment != null ? environment : "Not set");
        logger.info("  - OS: {}", os);
        logger.info("  - Java: {}", javaVersion);
    }
    
    /**
     * Create a test execution summary log
     */
    public static void logExecutionSummary(int totalTests, int passedTests, int failedTests, long duration) {
        logger.info("📊 EXECUTION SUMMARY:");
        logger.info("  - Total Tests: {}", totalTests);
        logger.info("  - Passed: {} ({}%)", passedTests, (passedTests * 100) / totalTests);
        logger.info("  - Failed: {} ({}%)", failedTests, (failedTests * 100) / totalTests);
        logger.info("  - Duration: {}ms", duration);
        logger.info("  - Success Rate: {}%", (passedTests * 100) / totalTests);
    }
    
    /**
     * Get browser-specific marker
     */
    private static Marker getBrowserMarker(String browserName) {
        switch (browserName.toLowerCase()) {
            case "chrome":
                return CHROME;
            case "firefox":
                return FIREFOX;
            case "edge":
                return EDGE;
            case "safari":
                return SAFARI;
            default:
                return null;
        }
    }
    
    /**
     * Create structured log entry for complex data
     */
    public static void logStructuredData(String eventType, java.util.Map<String, Object> data) {
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("📋 ").append(eventType.toUpperCase()).append(": ");
        
        data.forEach((key, value) -> 
            logMessage.append(key).append("=").append(value).append(", "));
        
        // Remove trailing comma and space
        if (logMessage.length() > 2) {
            logMessage.setLength(logMessage.length() - 2);
        }
        
        logger.info(logMessage.toString());
    }
    
    /**
     * Log with custom marker
     */
    public static void logWithMarker(Marker marker, String level, String message, Object... args) {
        switch (level.toUpperCase()) {
            case "DEBUG":
                logger.debug(marker, message, args);
                break;
            case "INFO":
                logger.info(marker, message, args);
                break;
            case "WARN":
                logger.warn(marker, message, args);
                break;
            case "ERROR":
                logger.error(marker, message, args);
                break;
            default:
                logger.info(marker, message, args);
        }
    }
}