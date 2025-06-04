package com.retailer.cart.tests;

import com.retailer.cart.pages.ShoppingCartPage;
import com.retailer.cart.utils.DriverManager;
import com.retailer.cart.utils.ConfigReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("mobile")
@Tag("responsive")
@DisplayName("Mobile and Responsive Design Tests")
public class MobileResponsiveTest {
    
    private static final Logger logger = LoggerFactory.getLogger(MobileResponsiveTest.class);
    private WebDriver driver;
    private ShoppingCartPage cartPage;
    private JavascriptExecutor jsExecutor;
    
    // Common mobile device dimensions
    private static final Map<String, Dimension> DEVICE_DIMENSIONS = Map.of(
        "iPhone SE", new Dimension(375, 667),
        "iPhone 12", new Dimension(390, 844),
        "iPhone 12 Pro Max", new Dimension(428, 926),
        "Samsung Galaxy S21", new Dimension(360, 800),
        "iPad", new Dimension(768, 1024),
        "iPad Pro", new Dimension(1024, 1366),
        "Desktop", new Dimension(1920, 1080),
        "Laptop", new Dimension(1366, 768)
    );
    
    @BeforeEach
    public void setUp() {
        logger.info("Setting up mobile/responsive test");
    }
    
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            DriverManager.quitDriver();
            logger.info("Mobile test session ended");
        }
    }
    
    @ParameterizedTest(name = "Test responsive layout on {0}")
    @ValueSource(strings = {"iPhone SE", "iPhone 12", "Samsung Galaxy S21", "iPad", "Desktop"})
    @DisplayName("Responsive layout validation across devices")
    public void testResponsiveLayoutAcrossDevices(String deviceName) {
        logger.info("Testing responsive layout on: {}", deviceName);
        
        setupDriverForDevice(deviceName);
        cartPage = new ShoppingCartPage(driver);
        jsExecutor = (JavascriptExecutor) driver;
        
        // Navigate to cart page
        cartPage.navigateToCart();
        
        // Test responsive behavior
        validateResponsiveLayout(deviceName);
        validateTouchInteractions(deviceName);
        validateViewportAdaptation(deviceName);
        
        logger.info("Responsive layout test completed for: {}", deviceName);
    }
    
    @Test
    @DisplayName("Mobile device emulation")
    public void testMobileDeviceEmulation() {
        logger.info("Testing mobile device emulation");
        
        // Test Chrome mobile emulation
        setupChromeWithMobileEmulation("iPhone 12");
        cartPage = new ShoppingCartPage(driver);
        jsExecutor = (JavascriptExecutor) driver;
        
        cartPage.navigateToCart();
        
        // Verify mobile user agent
        String userAgent = (String) jsExecutor.executeScript("return navigator.userAgent;");
        assertThat(userAgent).containsIgnoringCase("mobile");
        
        // Test mobile-specific features
        validateMobileSpecificFeatures();
        
        logger.info("Mobile device emulation test completed");
    }
    
    @ParameterizedTest(name = "Test orientation on {0} in {1} mode")
    @CsvSource({
        "iPhone 12, portrait",
        "iPhone 12, landscape",
        "iPad, portrait", 
        "iPad, landscape"
    })
    @DisplayName("Device orientation testing")
    public void testDeviceOrientation(String deviceName, String orientation) {
        logger.info("Testing {} orientation on: {}", orientation, deviceName);
        
        setupDriverForDevice(deviceName);
        cartPage = new ShoppingCartPage(driver);
        jsExecutor = (JavascriptExecutor) driver;
        
        // Set orientation
        setDeviceOrientation(deviceName, orientation);
        
        cartPage.navigateToCart();
        
        // Validate orientation-specific layout
        validateOrientationLayout(deviceName, orientation);
        
        logger.info("Orientation test completed for: {} - {}", deviceName, orientation);
    }
    
    @Test
    @DisplayName("Touch gestures and interactions")
    public void testTouchGesturesAndInteractions() {
        logger.info("Testing touch gestures and interactions");
        
        setupDriverForDevice("iPhone 12");
        cartPage = new ShoppingCartPage(driver);
        jsExecutor = (JavascriptExecutor) driver;
        
        cartPage.navigateToCart();
        
        // Test touch interactions
        validateTouchGestures();
        validateSwipeActions();
        validatePinchZoom();
        
        logger.info("Touch gestures test completed");
    }
    
    @Test
    @DisplayName("Mobile performance optimization")
    public void testMobilePerformanceOptimization() {
        logger.info("Testing mobile performance optimization");
        
        setupDriverForDevice("iPhone SE"); // Lower-end device simulation
        cartPage = new ShoppingCartPage(driver);
        jsExecutor = (JavascriptExecutor) driver;
        
        // Measure page load performance
        long startTime = System.currentTimeMillis();
        cartPage.navigateToCart();
        long loadTime = System.currentTimeMillis() - startTime;
        
        // Validate mobile performance metrics
        validateMobilePerformance(loadTime);
        validateResourceOptimization();
        
        logger.info("Mobile performance test completed");
    }
    
    @Test
    @DisplayName("Progressive Web App features")
    public void testProgressiveWebAppFeatures() {
        logger.info("Testing Progressive Web App features");
        
        setupDriverForDevice("iPhone 12");
        cartPage = new ShoppingCartPage(driver);
        jsExecutor = (JavascriptExecutor) driver;
        
        cartPage.navigateToCart();
        
        // Test PWA features
        validateServiceWorker();
        validateManifest();
        validateOfflineCapability();
        
        logger.info("PWA features test completed");
    }
    
    private void setupDriverForDevice(String deviceName) {
        Dimension dimension = DEVICE_DIMENSIONS.get(deviceName);
        if (dimension == null) {
            dimension = DEVICE_DIMENSIONS.get("iPhone 12"); // Default
        }
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("deviceName", deviceName);
        
        DriverManager.setDriver("chrome", capabilities);
        driver = DriverManager.getDriver();
        
        // Set window size to match device
        driver.manage().window().setSize(dimension);
        
        logger.debug("Driver setup completed for device: {} ({}x{})", 
                    deviceName, dimension.width, dimension.height);
    }
    
    private void setupChromeWithMobileEmulation(String deviceName) {
        Map<String, Object> mobileEmulation = new HashMap<>();
        
        switch (deviceName) {
            case "iPhone 12":
                mobileEmulation.put("deviceName", "iPhone 12");
                break;
            case "iPad":
                mobileEmulation.put("deviceName", "iPad");
                break;
            case "Samsung Galaxy S21":
                mobileEmulation.put("deviceName", "Galaxy S5");
                break;
            default:
                Map<String, Object> deviceMetrics = new HashMap<>();
                deviceMetrics.put("width", 375);
                deviceMetrics.put("height", 667);
                deviceMetrics.put("pixelRatio", 2.0);
                mobileEmulation.put("deviceMetrics", deviceMetrics);
                mobileEmulation.put("userAgent", "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)");
        }
        
        Map<String, Object> capabilities = new HashMap<>();
        capabilities.put("mobileEmulation", mobileEmulation);
        
        DriverManager.setDriver("chrome", capabilities);
        driver = DriverManager.getDriver();
        
        logger.debug("Chrome mobile emulation setup completed for: {}", deviceName);
    }
    
    private void setDeviceOrientation(String deviceName, String orientation) {
        Dimension currentSize = driver.manage().window().getSize();
        Dimension newSize;
        
        if ("landscape".equals(orientation)) {
            // Swap width and height for landscape
            newSize = new Dimension(Math.max(currentSize.width, currentSize.height), 
                                  Math.min(currentSize.width, currentSize.height));
        } else {
            // Portrait (or ensure portrait)
            newSize = new Dimension(Math.min(currentSize.width, currentSize.height), 
                                  Math.max(currentSize.width, currentSize.height));
        }
        
        driver.manage().window().setSize(newSize);
        
        logger.debug("Device orientation set to {} for {}: {}x{}", 
                    orientation, deviceName, newSize.width, newSize.height);
    }
    
    private void validateResponsiveLayout(String deviceName) {
        logger.debug("Validating responsive layout for: {}", deviceName);
        
        // Get viewport dimensions
        Long viewportWidth = (Long) jsExecutor.executeScript("return window.innerWidth;");
        Long viewportHeight = (Long) jsExecutor.executeScript("return window.innerHeight;");
        
        // Validate layout adaptation
        assertThat(viewportWidth).isGreaterThan(0);
        assertThat(viewportHeight).isGreaterThan(0);
        
        // Test responsive breakpoints
        if (viewportWidth <= 480) {
            validateMobileLayout();
        } else if (viewportWidth <= 768) {
            validateTabletLayout();
        } else {
            validateDesktopLayout();
        }
    }
    
    private void validateTouchInteractions(String deviceName) {
        logger.debug("Validating touch interactions for: {}", deviceName);
        
        if (isMobileDevice(deviceName)) {
            // Simulate touch events
            Boolean touchSupported = (Boolean) jsExecutor.executeScript(
                "return 'ontouchstart' in window || navigator.maxTouchPoints > 0;");
            
            if (touchSupported) {
                logger.debug("Touch support detected for: {}", deviceName);
                // Additional touch-specific validations can be added here
            }
        }
    }
    
    private void validateViewportAdaptation(String deviceName) {
        logger.debug("Validating viewport adaptation for: {}", deviceName);
        
        // Check viewport meta tag
        String viewportMeta = (String) jsExecutor.executeScript(
            "var meta = document.querySelector('meta[name=\"viewport\"]');" +
            "return meta ? meta.content : null;");
        
        if (isMobileDevice(deviceName)) {
            assertThat(viewportMeta).isNotNull();
            assertThat(viewportMeta).containsIgnoringCase("width=device-width");
        }
    }
    
    private void validateMobileSpecificFeatures() {
        logger.debug("Validating mobile-specific features");
        
        // Test touch-friendly button sizes
        validateTouchTargetSizes();
        
        // Test mobile navigation patterns
        validateMobileNavigation();
        
        // Test mobile form interactions
        validateMobileFormInputs();
    }
    
    private void validateOrientationLayout(String deviceName, String orientation) {
        logger.debug("Validating orientation layout: {} - {}", deviceName, orientation);
        
        Long viewportWidth = (Long) jsExecutor.executeScript("return window.innerWidth;");
        Long viewportHeight = (Long) jsExecutor.executeScript("return window.innerHeight;");
        
        if ("landscape".equals(orientation)) {
            assertThat(viewportWidth).isGreaterThan(viewportHeight);
        } else {
            assertThat(viewportHeight).isGreaterThan(viewportWidth);
        }
    }
    
    private void validateTouchGestures() {
        logger.debug("Validating touch gestures");
        
        // Test touch events are properly handled
        Boolean touchEventsSupported = (Boolean) jsExecutor.executeScript(
            "return typeof Touch !== 'undefined';");
        
        if (touchEventsSupported) {
            logger.debug("Touch events are supported");
        }
    }
    
    private void validateSwipeActions() {
        logger.debug("Validating swipe actions");
        
        // Test swipe gesture recognition
        // This would typically involve simulating touch events
        // For now, we'll check if swipe handlers are present
        Boolean swipeHandlersPresent = (Boolean) jsExecutor.executeScript(
            "return document.querySelector('[data-swipe]') !== null;");
        
        logger.debug("Swipe handlers present: {}", swipeHandlersPresent);
    }
    
    private void validatePinchZoom() {
        logger.debug("Validating pinch zoom");
        
        // Check if pinch zoom is appropriately enabled/disabled
        String viewportMeta = (String) jsExecutor.executeScript(
            "var meta = document.querySelector('meta[name=\"viewport\"]');" +
            "return meta ? meta.content : null;");
        
        if (viewportMeta != null) {
            boolean zoomDisabled = viewportMeta.contains("user-scalable=no") || 
                                 viewportMeta.contains("maximum-scale=1");
            logger.debug("Zoom disabled: {}", zoomDisabled);
        }
    }
    
    private void validateMobilePerformance(long loadTime) {
        logger.debug("Validating mobile performance - Load time: {}ms", loadTime);
        
        // Mobile performance should be under 3 seconds for good UX
        assertThat(loadTime).isLessThan(3000);
        
        // Check for performance optimization indicators
        validateImageOptimization();
        validateCSSOptimization();
        validateJavaScriptOptimization();
    }
    
    private void validateResourceOptimization() {
        logger.debug("Validating resource optimization");
        
        // Check for lazy loading
        Boolean lazyLoadingPresent = (Boolean) jsExecutor.executeScript(
            "return document.querySelector('[loading=\"lazy\"]') !== null;");
        
        logger.debug("Lazy loading present: {}", lazyLoadingPresent);
    }
    
    private void validateServiceWorker() {
        logger.debug("Validating service worker");
        
        Boolean serviceWorkerSupported = (Boolean) jsExecutor.executeScript(
            "return 'serviceWorker' in navigator;");
        
        if (serviceWorkerSupported) {
            Boolean serviceWorkerRegistered = (Boolean) jsExecutor.executeScript(
                "return navigator.serviceWorker.controller !== null;");
            logger.debug("Service worker registered: {}", serviceWorkerRegistered);
        }
    }
    
    private void validateManifest() {
        logger.debug("Validating web app manifest");
        
        String manifestLink = (String) jsExecutor.executeScript(
            "var link = document.querySelector('link[rel=\"manifest\"]');" +
            "return link ? link.href : null;");
        
        logger.debug("Manifest link present: {}", manifestLink != null);
    }
    
    private void validateOfflineCapability() {
        logger.debug("Validating offline capability");
        
        // Test offline detection
        Boolean onlineStatus = (Boolean) jsExecutor.executeScript("return navigator.onLine;");
        logger.debug("Online status: {}", onlineStatus);
    }
    
    private void validateMobileLayout() {
        logger.debug("Validating mobile layout");
        
        // Check for mobile-specific layout elements
        Boolean mobileMenuPresent = (Boolean) jsExecutor.executeScript(
            "return document.querySelector('.mobile-menu, .hamburger-menu') !== null;");
        
        logger.debug("Mobile menu present: {}", mobileMenuPresent);
    }
    
    private void validateTabletLayout() {
        logger.debug("Validating tablet layout");
        
        // Tablet-specific layout validations
        Long viewportWidth = (Long) jsExecutor.executeScript("return window.innerWidth;");
        assertThat(viewportWidth).isBetween(481L, 768L);
    }
    
    private void validateDesktopLayout() {
        logger.debug("Validating desktop layout");
        
        // Desktop-specific layout validations
        Long viewportWidth = (Long) jsExecutor.executeScript("return window.innerWidth;");
        assertThat(viewportWidth).isGreaterThan(768L);
    }
    
    private void validateTouchTargetSizes() {
        logger.debug("Validating touch target sizes");
        
        // Check button and link sizes are touch-friendly (at least 44px)
        // This would require more specific element selection
    }
    
    private void validateMobileNavigation() {
        logger.debug("Validating mobile navigation");
        
        // Check for mobile navigation patterns
        Boolean hamburgerMenuPresent = (Boolean) jsExecutor.executeScript(
            "return document.querySelector('.hamburger, .mobile-toggle') !== null;");
        
        logger.debug("Hamburger menu present: {}", hamburgerMenuPresent);
    }
    
    private void validateMobileFormInputs() {
        logger.debug("Validating mobile form inputs");
        
        // Check for mobile-optimized input types
        Boolean mobileInputTypesUsed = (Boolean) jsExecutor.executeScript(
            "var inputs = document.querySelectorAll('input[type=\"tel\"], input[type=\"email\"]');" +
            "return inputs.length > 0;");
        
        logger.debug("Mobile input types used: {}", mobileInputTypesUsed);
    }
    
    private void validateImageOptimization() {
        logger.debug("Validating image optimization");
        
        // Check for responsive images
        Boolean responsiveImagesUsed = (Boolean) jsExecutor.executeScript(
            "return document.querySelector('img[srcset], picture') !== null;");
        
        logger.debug("Responsive images used: {}", responsiveImagesUsed);
    }
    
    private void validateCSSOptimization() {
        logger.debug("Validating CSS optimization");
        
        // Check for critical CSS inlining
        Boolean inlineCSSPresent = (Boolean) jsExecutor.executeScript(
            "return document.querySelector('style') !== null;");
        
        logger.debug("Inline CSS present: {}", inlineCSSPresent);
    }
    
    private void validateJavaScriptOptimization() {
        logger.debug("Validating JavaScript optimization");
        
        // Check for async/defer script loading
        Boolean asyncScriptsPresent = (Boolean) jsExecutor.executeScript(
            "return document.querySelector('script[async], script[defer]') !== null;");
        
        logger.debug("Async scripts present: {}", asyncScriptsPresent);
    }
    
    private boolean isMobileDevice(String deviceName) {
        return deviceName.contains("iPhone") || deviceName.contains("Samsung Galaxy") || 
               deviceName.contains("Android");
    }
}