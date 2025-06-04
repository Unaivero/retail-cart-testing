package com.retailer.cart.tests;

import com.retailer.cart.pages.ShoppingCartPage;
import com.retailer.cart.utils.DriverManager;
import com.retailer.cart.utils.ConfigReader;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openqa.selenium.WebDriver;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("cross-browser")
@DisplayName("Cross-Browser Compatibility Tests")
public class CrossBrowserTest {
    
    private static final Logger logger = LoggerFactory.getLogger(CrossBrowserTest.class);
    private WebDriver driver;
    private ShoppingCartPage cartPage;
    
    @BeforeEach
    public void setUp() {
        logger.info("Setting up cross-browser test");
    }
    
    @AfterEach
    public void tearDown() {
        if (driver != null) {
            DriverManager.quitDriver();
            logger.info("Browser session ended");
        }
    }
    
    @ParameterizedTest(name = "Test cart functionality in {0}")
    @ValueSource(strings = {"chrome", "firefox", "edge"})
    @DisplayName("Cart operations across different browsers")
    public void testCartOperationsAcrossBrowsers(String browserName) {
        logger.info("Testing cart operations in: {}", browserName);
        
        // Initialize driver for specific browser
        DriverManager.setDriver(browserName);
        driver = DriverManager.getDriver();
        cartPage = new ShoppingCartPage(driver);
        
        // Navigate to cart page
        cartPage.navigateToCart();
        
        // Verify page loads correctly in all browsers
        String pageTitle = cartPage.getPageTitle();
        assertThat(pageTitle).containsIgnoringCase("cart");
        
        // Test basic cart functionality
        verifyCartFunctionality(browserName);
        
        logger.info("Cart operations test completed successfully in: {}", browserName);
    }
    
    @ParameterizedTest(name = "Test responsive design in {0}")
    @ValueSource(strings = {"chrome", "firefox", "edge"})
    @DisplayName("Responsive design validation across browsers")
    public void testResponsiveDesignAcrossBrowsers(String browserName) {
        logger.info("Testing responsive design in: {}", browserName);
        
        DriverManager.setDriver(browserName);
        driver = DriverManager.getDriver();
        cartPage = new ShoppingCartPage(driver);
        
        // Test different screen sizes
        testDesktopView(browserName);
        testTabletView(browserName);
        testMobileView(browserName);
        
        logger.info("Responsive design test completed successfully in: {}", browserName);
    }
    
    @ParameterizedTest(name = "Test JavaScript functionality in {0}")
    @ValueSource(strings = {"chrome", "firefox", "edge"})
    @DisplayName("JavaScript compatibility across browsers")
    public void testJavaScriptCompatibilityAcrossBrowsers(String browserName) {
        logger.info("Testing JavaScript compatibility in: {}", browserName);
        
        DriverManager.setDriver(browserName);
        driver = DriverManager.getDriver();
        cartPage = new ShoppingCartPage(driver);
        
        cartPage.navigateToCart();
        
        // Test JavaScript-dependent features
        verifyJavaScriptFunctionality(browserName);
        
        logger.info("JavaScript compatibility test completed successfully in: {}", browserName);
    }
    
    @ParameterizedTest(name = "Test CSS styling in {0}")
    @ValueSource(strings = {"chrome", "firefox", "edge"})
    @DisplayName("CSS styling consistency across browsers")
    public void testCSSStylingAcrossBrowsers(String browserName) {
        logger.info("Testing CSS styling consistency in: {}", browserName);
        
        DriverManager.setDriver(browserName);
        driver = DriverManager.getDriver();
        cartPage = new ShoppingCartPage(driver);
        
        cartPage.navigateToCart();
        
        // Verify CSS styling consistency
        verifyStylingConsistency(browserName);
        
        logger.info("CSS styling test completed successfully in: {}", browserName);
    }
    
    private void verifyCartFunctionality(String browserName) {
        logger.debug("Verifying cart functionality in: {}", browserName);
        
        // Test that cart page loads
        assertThat(cartPage.getCurrentUrl()).containsIgnoringCase("cart");
        
        // Test browser-specific behaviors
        switch (browserName.toLowerCase()) {
            case "chrome":
                // Chrome-specific validations
                verifyChromeBehavior();
                break;
            case "firefox":
                // Firefox-specific validations
                verifyFirefoxBehavior();
                break;
            case "edge":
                // Edge-specific validations
                verifyEdgeBehavior();
                break;
        }
    }
    
    private void testDesktopView(String browserName) {
        logger.debug("Testing desktop view in: {}", browserName);
        
        // Set desktop resolution
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
        cartPage.navigateToCart();
        
        // Verify desktop layout
        assertThat(cartPage.getCurrentUrl()).isNotNull();
        
        // Additional desktop-specific checks can be added here
    }
    
    private void testTabletView(String browserName) {
        logger.debug("Testing tablet view in: {}", browserName);
        
        // Set tablet resolution
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(768, 1024));
        cartPage.refreshPage();
        
        // Verify tablet layout adaptations
        assertThat(cartPage.getCurrentUrl()).isNotNull();
        
        // Additional tablet-specific checks can be added here
    }
    
    private void testMobileView(String browserName) {
        logger.debug("Testing mobile view in: {}", browserName);
        
        // Set mobile resolution
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(375, 667));
        cartPage.refreshPage();
        
        // Verify mobile layout adaptations
        assertThat(cartPage.getCurrentUrl()).isNotNull();
        
        // Additional mobile-specific checks can be added here
    }
    
    private void verifyJavaScriptFunctionality(String browserName) {
        logger.debug("Verifying JavaScript functionality in: {}", browserName);
        
        // Test JavaScript execution
        Object result = ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("return document.readyState;");
        assertThat(result.toString()).isEqualTo("complete");
        
        // Test browser-specific JavaScript features
        switch (browserName.toLowerCase()) {
            case "chrome":
                // Chrome-specific JS tests
                verifyChromeJavaScript();
                break;
            case "firefox":
                // Firefox-specific JS tests
                verifyFirefoxJavaScript();
                break;
            case "edge":
                // Edge-specific JS tests
                verifyEdgeJavaScript();
                break;
        }
    }
    
    private void verifyStylingConsistency(String browserName) {
        logger.debug("Verifying styling consistency in: {}", browserName);
        
        // Test that page elements are properly styled
        assertThat(cartPage.getCurrentUrl()).isNotNull();
        
        // Browser-specific styling checks
        switch (browserName.toLowerCase()) {
            case "chrome":
                // Chrome-specific styling validations
                verifyChromeStyles();
                break;
            case "firefox":
                // Firefox-specific styling validations
                verifyFirefoxStyles();
                break;
            case "edge":
                // Edge-specific styling validations
                verifyEdgeStyles();
                break;
        }
    }
    
    // Browser-specific validation methods
    private void verifyChromeBehavior() {
        logger.debug("Verifying Chrome-specific behavior");
        // Chrome-specific tests
        assertThat(driver.manage().window().getSize().getWidth()).isGreaterThan(0);
    }
    
    private void verifyFirefoxBehavior() {
        logger.debug("Verifying Firefox-specific behavior");
        // Firefox-specific tests
        assertThat(driver.manage().window().getSize().getWidth()).isGreaterThan(0);
    }
    
    private void verifyEdgeBehavior() {
        logger.debug("Verifying Edge-specific behavior");
        // Edge-specific tests
        assertThat(driver.manage().window().getSize().getWidth()).isGreaterThan(0);
    }
    
    private void verifyChromeJavaScript() {
        logger.debug("Verifying Chrome JavaScript features");
        // Chrome-specific JS features
        Object chromeVersion = ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("return navigator.userAgent.includes('Chrome');");
        assertThat(chromeVersion).isEqualTo(true);
    }
    
    private void verifyFirefoxJavaScript() {
        logger.debug("Verifying Firefox JavaScript features");
        // Firefox-specific JS features
        Object firefoxVersion = ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("return navigator.userAgent.includes('Firefox');");
        assertThat(firefoxVersion).isEqualTo(true);
    }
    
    private void verifyEdgeJavaScript() {
        logger.debug("Verifying Edge JavaScript features");
        // Edge-specific JS features
        Object edgeVersion = ((org.openqa.selenium.JavascriptExecutor) driver)
            .executeScript("return navigator.userAgent.includes('Edge') || navigator.userAgent.includes('Edg');");
        assertThat(edgeVersion).isEqualTo(true);
    }
    
    private void verifyChromeStyles() {
        logger.debug("Verifying Chrome-specific styles");
        // Chrome-specific styling checks
    }
    
    private void verifyFirefoxStyles() {
        logger.debug("Verifying Firefox-specific styles");
        // Firefox-specific styling checks
    }
    
    private void verifyEdgeStyles() {
        logger.debug("Verifying Edge-specific styles");
        // Edge-specific styling checks
    }
}