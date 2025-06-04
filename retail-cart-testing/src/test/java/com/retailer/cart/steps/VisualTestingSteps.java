package com.retailer.cart.steps;

import com.retailer.cart.utils.DriverManager;
import com.retailer.cart.utils.VisualTestingUtil;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class VisualTestingSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(VisualTestingSteps.class);
    
    private WebDriver driver;
    private VisualTestingUtil visualTestingUtil;
    private String currentTestName;
    private boolean lastComparisonResult;
    
    @Before("@visual")
    public void setupVisualTesting() {
        driver = DriverManager.getDriver();
        visualTestingUtil = new VisualTestingUtil(driver);
        logger.info("Visual testing setup completed");
    }
    
    @After("@visual")
    public void tearDownVisualTesting() {
        // Cleanup is handled by DriverManager
        logger.info("Visual testing teardown completed");
    }
    
    @When("I capture a full page screenshot for {string}")
    public void iCaptureAFullPageScreenshotFor(String testName) {
        this.currentTestName = testName;
        // Screenshot will be captured during comparison
        logger.info("Preparing to capture full page screenshot for: {}", testName);
    }
    
    @When("I capture a screenshot of element {string} for {string}")
    public void iCaptureAScreenshotOfElementFor(String elementSelector, String testName) {
        this.currentTestName = testName;
        // Screenshot will be captured during comparison
        logger.info("Preparing to capture element screenshot for: {} (selector: {})", testName, elementSelector);
    }
    
    @Then("the full page screenshot should match the baseline")
    public void theFullPageScreenshotShouldMatchTheBaseline() {
        assertThat(currentTestName).as("Test name must be set before comparison").isNotNull();
        
        lastComparisonResult = visualTestingUtil.compareFullPageScreenshot(currentTestName);
        assertThat(lastComparisonResult)
                .as("Full page screenshot for '%s' should match the baseline within acceptable threshold", currentTestName)
                .isTrue();
        
        logger.info("Full page visual comparison passed for: {}", currentTestName);
    }
    
    @Then("the full page screenshot should match the baseline with {double}% threshold")
    public void theFullPageScreenshotShouldMatchTheBaselineWithThreshold(double thresholdPercentage) {
        assertThat(currentTestName).as("Test name must be set before comparison").isNotNull();
        
        double threshold = thresholdPercentage / 100.0; // Convert percentage to decimal
        lastComparisonResult = visualTestingUtil.compareFullPageScreenshot(currentTestName, threshold);
        assertThat(lastComparisonResult)
                .as("Full page screenshot for '%s' should match the baseline within %s%% threshold", 
                    currentTestName, thresholdPercentage)
                .isTrue();
        
        logger.info("Full page visual comparison passed for: {} with {}% threshold", currentTestName, thresholdPercentage);
    }
    
    @Then("the element {string} screenshot should match the baseline")
    public void theElementScreenshotShouldMatchTheBaseline(String elementSelector) {
        assertThat(currentTestName).as("Test name must be set before comparison").isNotNull();
        
        lastComparisonResult = visualTestingUtil.compareElementScreenshot(elementSelector, currentTestName);
        assertThat(lastComparisonResult)
                .as("Element screenshot for '%s' (selector: %s) should match the baseline within acceptable threshold", 
                    currentTestName, elementSelector)
                .isTrue();
        
        logger.info("Element visual comparison passed for: {} (selector: {})", currentTestName, elementSelector);
    }
    
    @Then("the element {string} screenshot should match the baseline with {double}% threshold")
    public void theElementScreenshotShouldMatchTheBaselineWithThreshold(String elementSelector, double thresholdPercentage) {
        assertThat(currentTestName).as("Test name must be set before comparison").isNotNull();
        
        double threshold = thresholdPercentage / 100.0; // Convert percentage to decimal
        lastComparisonResult = visualTestingUtil.compareElementScreenshot(elementSelector, currentTestName, threshold);
        assertThat(lastComparisonResult)
                .as("Element screenshot for '%s' (selector: %s) should match the baseline within %s%% threshold", 
                    currentTestName, elementSelector, thresholdPercentage)
                .isTrue();
        
        logger.info("Element visual comparison passed for: {} (selector: {}) with {}% threshold", 
                   currentTestName, elementSelector, thresholdPercentage);
    }
    
    @When("I update the baseline for {string}")
    public void iUpdateTheBaselineFor(String testName) {
        visualTestingUtil.updateBaseline(testName);
        logger.info("Updated baseline for: {}", testName);
    }
    
    @Then("the visual comparison should fail")
    public void theVisualComparisonShouldFail() {
        assertThat(lastComparisonResult)
                .as("Visual comparison should have failed")
                .isFalse();
        
        logger.info("Visual comparison correctly failed as expected");
    }
    
    @Then("the visual comparison should pass")
    public void theVisualComparisonShouldPass() {
        assertThat(lastComparisonResult)
                .as("Visual comparison should have passed")
                .isTrue();
        
        logger.info("Visual comparison correctly passed as expected");
    }
}