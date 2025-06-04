package com.retailer.cart.steps;

import com.retailer.cart.utils.AccessibilityTestingUtil;
import com.retailer.cart.utils.AccessibilityTestingUtil.AccessibilityResults;
import com.retailer.cart.utils.AccessibilityTestingUtil.AccessibilityViolation;
import com.retailer.cart.utils.DriverManager;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

public class AccessibilityTestingSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessibilityTestingSteps.class);
    
    private WebDriver driver;
    private AccessibilityTestingUtil accessibilityUtil;
    private AccessibilityResults lastResults;
    private String currentElementSelector;
    
    @Before("@accessibility")
    public void setupAccessibilityTesting() {
        driver = DriverManager.getDriver();
        accessibilityUtil = new AccessibilityTestingUtil(driver);
        logger.info("Accessibility testing setup completed");
    }
    
    @After("@accessibility")
    public void tearDownAccessibilityTesting() {
        if (lastResults != null && lastResults.hasViolations()) {
            logger.warn("Accessibility violations found during testing:");
            for (AccessibilityViolation violation : lastResults.getViolations()) {
                logger.warn("  - {}", violation.toString());
            }
        }
        logger.info("Accessibility testing completed");
    }
    
    @Given("I am testing accessibility for element {string}")
    public void iAmTestingAccessibilityForElement(String elementSelector) {
        this.currentElementSelector = elementSelector;
        logger.info("Set target element for accessibility testing: {}", elementSelector);
    }
    
    @When("I perform a full accessibility audit")
    public void iPerformAFullAccessibilityAudit() {
        logger.info("Performing full accessibility audit for: {}", driver.getCurrentUrl());
        lastResults = accessibilityUtil.performFullAccessibilityAudit();
        
        logger.info("Full accessibility audit completed. Violations: {}, Incomplete: {}, Passes: {}", 
                   lastResults.getViolations().size(), 
                   lastResults.getIncomplete().size(), 
                   lastResults.getPasses().size());
    }
    
    @When("I perform a WCAG 2.1 AA compliance audit")
    public void iPerformAWCAG21AAComplianceAudit() {
        logger.info("Performing WCAG 2.1 AA compliance audit");
        lastResults = accessibilityUtil.testWCAG21AACompliance();
        
        logger.info("WCAG 2.1 AA audit completed. Violations: {}", lastResults.getViolations().size());
    }
    
    @When("I test keyboard navigation accessibility")
    public void iTestKeyboardNavigationAccessibility() {
        logger.info("Testing keyboard navigation accessibility");
        lastResults = accessibilityUtil.testKeyboardNavigation();
        
        logger.info("Keyboard navigation test completed. Violations: {}", lastResults.getViolations().size());
    }
    
    @When("I test color contrast accessibility")
    public void iTestColorContrastAccessibility() {
        logger.info("Testing color contrast accessibility");
        lastResults = accessibilityUtil.testColorContrast();
        
        logger.info("Color contrast test completed. Violations: {}", lastResults.getViolations().size());
    }
    
    @When("I test ARIA implementation")
    public void iTestARIAImplementation() {
        logger.info("Testing ARIA implementation");
        lastResults = accessibilityUtil.testARIAImplementation();
        
        logger.info("ARIA implementation test completed. Violations: {}", lastResults.getViolations().size());
    }
    
    @When("I test form accessibility")
    public void iTestFormAccessibility() {
        logger.info("Testing form accessibility");
        lastResults = accessibilityUtil.testFormAccessibility();
        
        logger.info("Form accessibility test completed. Violations: {}", lastResults.getViolations().size());
    }
    
    @When("I test image accessibility")
    public void iTestImageAccessibility() {
        logger.info("Testing image accessibility");
        lastResults = accessibilityUtil.testImageAccessibility();
        
        logger.info("Image accessibility test completed. Violations: {}", lastResults.getViolations().size());
    }
    
    @When("I test link accessibility")
    public void iTestLinkAccessibility() {
        logger.info("Testing link accessibility");
        lastResults = accessibilityUtil.testLinkAccessibility();
        
        logger.info("Link accessibility test completed. Violations: {}", lastResults.getViolations().size());
    }
    
    @When("I test heading structure accessibility")
    public void iTestHeadingStructureAccessibility() {
        logger.info("Testing heading structure accessibility");
        lastResults = accessibilityUtil.testHeadingStructure();
        
        logger.info("Heading structure test completed. Violations: {}", lastResults.getViolations().size());
    }
    
    @When("I test the current element for accessibility")
    public void iTestTheCurrentElementForAccessibility() {
        assertThat(currentElementSelector).as("Element selector must be set").isNotNull();
        
        logger.info("Testing element accessibility for: {}", currentElementSelector);
        lastResults = accessibilityUtil.testElementAccessibility(currentElementSelector);
        
        logger.info("Element accessibility test completed for: {}. Violations: {}", 
                   currentElementSelector, lastResults.getViolations().size());
    }
    
    @When("I test the cart items container for accessibility")
    public void iTestTheCartItemsContainerForAccessibility() {
        this.currentElementSelector = "#cart-items";
        logger.info("Testing cart items container accessibility");
        lastResults = accessibilityUtil.testElementAccessibility(currentElementSelector);
        
        logger.info("Cart items container accessibility test completed. Violations: {}", 
                   lastResults.getViolations().size());
    }
    
    @When("I test the promo code input for accessibility")
    public void iTestThePromoCodeInputForAccessibility() {
        this.currentElementSelector = "#promo-code-input";
        logger.info("Testing promo code input accessibility");
        lastResults = accessibilityUtil.testElementAccessibility(currentElementSelector);
        
        logger.info("Promo code input accessibility test completed. Violations: {}", 
                   lastResults.getViolations().size());
    }
    
    @When("I test quantity input fields for accessibility")
    public void iTestQuantityInputFieldsForAccessibility() {
        this.currentElementSelector = ".product-quantity";
        logger.info("Testing quantity input fields accessibility");
        lastResults = accessibilityUtil.testElementAccessibility(currentElementSelector);
        
        logger.info("Quantity input fields accessibility test completed. Violations: {}", 
                   lastResults.getViolations().size());
    }
    
    @When("I test the checkout button for accessibility")
    public void iTestTheCheckoutButtonForAccessibility() {
        this.currentElementSelector = "#checkout-button";
        logger.info("Testing checkout button accessibility");
        lastResults = accessibilityUtil.testElementAccessibility(currentElementSelector);
        
        logger.info("Checkout button accessibility test completed. Violations: {}", 
                   lastResults.getViolations().size());
    }
    
    @Then("there should be no accessibility violations")
    public void thereShouldBeNoAccessibilityViolations() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        assertThat(lastResults.getViolations())
                .as("No accessibility violations should be found, but found: %s", 
                    lastResults.getViolations().size())
                .isEmpty();
        
        logger.info("Accessibility check passed - no violations found");
    }
    
    @Then("there should be no critical accessibility violations")
    public void thereShouldBeNoCriticalAccessibilityViolations() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        long criticalViolations = lastResults.getCriticalViolations();
        assertThat(criticalViolations)
                .as("No critical accessibility violations should be found, but found: %d", criticalViolations)
                .isEqualTo(0);
        
        logger.info("Critical accessibility check passed - no critical violations found");
    }
    
    @Then("there should be no serious accessibility violations")
    public void thereShouldBeNoSeriousAccessibilityViolations() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        long seriousViolations = lastResults.getSeriousViolations();
        assertThat(seriousViolations)
                .as("No serious accessibility violations should be found, but found: %d", seriousViolations)
                .isEqualTo(0);
        
        logger.info("Serious accessibility check passed - no serious violations found");
    }
    
    @Then("there should be fewer than {int} accessibility violations")
    public void thereShouldBeFewerThanAccessibilityViolations(int maxViolations) {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        assertThat(lastResults.getViolations().size())
                .as("Should have fewer than %d accessibility violations, but found: %d", 
                    maxViolations, lastResults.getViolations().size())
                .isLessThan(maxViolations);
        
        logger.info("Accessibility violation threshold check passed: {} < {}", 
                   lastResults.getViolations().size(), maxViolations);
    }
    
    @Then("the page should be WCAG 2.1 AA compliant")
    public void thePageShouldBeWCAG21AACompliant() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // WCAG 2.1 AA compliance means no critical or serious violations for WCAG AA rules
        long wcagViolations = lastResults.getViolations().stream()
                .filter(v -> v.getTags().contains("wcag2aa") || v.getTags().contains("wcag21aa"))
                .filter(v -> "critical".equals(v.getImpact()) || "serious".equals(v.getImpact()))
                .count();
        
        assertThat(wcagViolations)
                .as("Page should be WCAG 2.1 AA compliant with no critical/serious violations, but found: %d", wcagViolations)
                .isEqualTo(0);
        
        logger.info("WCAG 2.1 AA compliance check passed");
    }
    
    @Then("form elements should have proper labels")
    public void formElementsShouldHaveProperLabels() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // Check for label-related violations
        long labelViolations = lastResults.getViolations().stream()
                .filter(v -> v.getId().contains("label") || 
                           v.getDescription().toLowerCase().contains("label") ||
                           v.getId().equals("form-field-multiple-labels"))
                .count();
        
        assertThat(labelViolations)
                .as("All form elements should have proper labels, but found %d label violations", labelViolations)
                .isEqualTo(0);
        
        logger.info("Form label check passed - all form elements have proper labels");
    }
    
    @Then("images should have alternative text")
    public void imagesShouldHaveAlternativeText() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // Check for image alt text violations
        long imageViolations = lastResults.getViolations().stream()
                .filter(v -> v.getId().contains("image-alt") || 
                           v.getId().contains("alt-text") ||
                           v.getId().equals("missing-alt-text"))
                .count();
        
        assertThat(imageViolations)
                .as("All images should have alternative text, but found %d image violations", imageViolations)
                .isEqualTo(0);
        
        logger.info("Image alt text check passed - all images have alternative text");
    }
    
    @Then("links should have accessible text")
    public void linksShouldHaveAccessibleText() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // Check for link text violations
        long linkViolations = lastResults.getViolations().stream()
                .filter(v -> v.getId().contains("link") || 
                           v.getId().equals("empty-link-text") ||
                           v.getId().equals("generic-link-text"))
                .count();
        
        assertThat(linkViolations)
                .as("All links should have accessible text, but found %d link violations", linkViolations)
                .isEqualTo(0);
        
        logger.info("Link accessibility check passed - all links have accessible text");
    }
    
    @Then("the heading structure should be logical")
    public void theHeadingStructureShouldBeLogical() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // Check for heading structure violations
        long headingViolations = lastResults.getViolations().stream()
                .filter(v -> v.getId().contains("heading") || 
                           v.getId().contains("h1") ||
                           v.getId().equals("skipped-heading-level") ||
                           v.getId().equals("first-heading-not-h1"))
                .count();
        
        assertThat(headingViolations)
                .as("Heading structure should be logical, but found %d heading violations", headingViolations)
                .isEqualTo(0);
        
        logger.info("Heading structure check passed - heading structure is logical");
    }
    
    @Then("color contrast should meet WCAG standards")
    public void colorContrastShouldMeetWCAGStandards() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // Check for color contrast violations
        long contrastViolations = lastResults.getViolations().stream()
                .filter(v -> v.getId().contains("color-contrast") || 
                           v.getTags().contains("color-contrast"))
                .count();
        
        assertThat(contrastViolations)
                .as("Color contrast should meet WCAG standards, but found %d contrast violations", contrastViolations)
                .isEqualTo(0);
        
        logger.info("Color contrast check passed - contrast meets WCAG standards");
    }
    
    @Then("ARIA attributes should be implemented correctly")
    public void ariaAttributesShouldBeImplementedCorrectly() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // Check for ARIA-related violations
        long ariaViolations = lastResults.getViolations().stream()
                .filter(v -> v.getId().contains("aria") || 
                           v.getTags().contains("aria") ||
                           v.getDescription().toLowerCase().contains("aria"))
                .count();
        
        assertThat(ariaViolations)
                .as("ARIA attributes should be implemented correctly, but found %d ARIA violations", ariaViolations)
                .isEqualTo(0);
        
        logger.info("ARIA implementation check passed - ARIA attributes are correctly implemented");
    }
    
    @Then("keyboard navigation should be accessible")
    public void keyboardNavigationShouldBeAccessible() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // Check for keyboard navigation violations
        long keyboardViolations = lastResults.getViolations().stream()
                .filter(v -> v.getId().contains("keyboard") || 
                           v.getId().contains("focus") ||
                           v.getId().contains("tabindex") ||
                           v.getTags().contains("keyboard"))
                .count();
        
        assertThat(keyboardViolations)
                .as("Keyboard navigation should be accessible, but found %d keyboard violations", keyboardViolations)
                .isEqualTo(0);
        
        logger.info("Keyboard navigation check passed - keyboard navigation is accessible");
    }
    
    @Then("an accessibility report should be generated")
    public void anAccessibilityReportShouldBeGenerated() {
        assertThat(lastResults).as("Accessibility results should be available for report generation").isNotNull();
        
        // The report is automatically generated by the AccessibilityTestingUtil
        // This step validates that we have results to report
        boolean hasData = lastResults.hasViolations() || 
                         lastResults.hasIncomplete() || 
                         !lastResults.getPasses().isEmpty();
        
        assertThat(hasData)
                .as("Accessibility report should contain data")
                .isTrue();
        
        logger.info("Accessibility report validation passed - report contains data");
    }
    
    @Then("the overall accessibility score should be acceptable")
    public void theOverallAccessibilityScoreShouldBeAcceptable() {
        assertThat(lastResults).as("Accessibility results should be available").isNotNull();
        
        // Define acceptable thresholds
        long criticalViolations = lastResults.getCriticalViolations();
        long seriousViolations = lastResults.getSeriousViolations();
        long totalViolations = lastResults.getViolations().size();
        
        // Acceptable criteria: no critical, max 2 serious, max 10 total
        assertThat(criticalViolations)
                .as("Should have no critical violations for acceptable score, but found: %d", criticalViolations)
                .isEqualTo(0);
        
        assertThat(seriousViolations)
                .as("Should have max 2 serious violations for acceptable score, but found: %d", seriousViolations)
                .isLessThanOrEqualTo(2);
        
        assertThat(totalViolations)
                .as("Should have max 10 total violations for acceptable score, but found: %d", totalViolations)
                .isLessThanOrEqualTo(10);
        
        logger.info("Overall accessibility score is acceptable: {} critical, {} serious, {} total violations", 
                   criticalViolations, seriousViolations, totalViolations);
    }
}