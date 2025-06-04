package com.retailer.cart.steps;

import com.retailer.cart.utils.DriverManager;
import com.retailer.cart.utils.ErrorHandlingUtil;
import com.retailer.cart.utils.ErrorHandlingUtil.ErrorScenario;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ErrorHandlingSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(ErrorHandlingSteps.class);
    
    private WebDriver driver;
    private ErrorHandlingUtil errorHandlingUtil;
    private List<ErrorScenario> lastTestResults;
    private String currentErrorType;
    
    @Before("@error-handling")
    public void setupErrorHandling() {
        driver = DriverManager.getDriver();
        errorHandlingUtil = new ErrorHandlingUtil(driver);
        logger.info("Error handling testing setup completed");
    }
    
    @After("@error-handling")
    public void tearDownErrorHandling() {
        if (errorHandlingUtil != null) {
            errorHandlingUtil.generateErrorReport();
        }
        
        if (lastTestResults != null && !lastTestResults.isEmpty()) {
            logger.info("Error handling test completed. Scenarios tested: {}", lastTestResults.size());
            
            long failed = lastTestResults.stream().filter(e -> "FAIL".equals(e.getResult())).count();
            if (failed > 0) {
                logger.warn("Failed error handling scenarios: {}", failed);
            }
        }
        
        logger.info("Error handling testing completed");
    }
    
    @Given("the application is configured for error testing")
    public void theApplicationIsConfiguredForErrorTesting() {
        // Ensure we're on the cart page and ready for error testing
        assertThat(driver.getCurrentUrl()).as("Should be on a valid page").isNotEmpty();
        logger.info("Application ready for error testing");
    }
    
    @Given("I am testing {string} error scenarios")
    public void iAmTestingErrorScenarios(String errorType) {
        this.currentErrorType = errorType;
        logger.info("Set error testing focus to: {}", errorType);
    }
    
    @When("I test network error handling")
    public void iTestNetworkErrorHandling() {
        logger.info("Testing network error handling scenarios");
        lastTestResults = errorHandlingUtil.testNetworkErrors();
        logger.info("Network error testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I test API error handling")
    public void iTestAPIErrorHandling() {
        logger.info("Testing API error handling scenarios");
        lastTestResults = errorHandlingUtil.testAPIErrors();
        logger.info("API error testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I test UI error handling")
    public void iTestUIErrorHandling() {
        logger.info("Testing UI error handling scenarios");
        lastTestResults = errorHandlingUtil.testUIErrors();
        logger.info("UI error testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I test form validation error handling")
    public void iTestFormValidationErrorHandling() {
        logger.info("Testing form validation error handling scenarios");
        lastTestResults = errorHandlingUtil.testFormValidationErrors();
        logger.info("Form validation error testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I test business logic error handling")
    public void iTestBusinessLogicErrorHandling() {
        logger.info("Testing business logic error handling scenarios");
        lastTestResults = errorHandlingUtil.testBusinessLogicErrors();
        logger.info("Business logic error testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I test browser compatibility error handling")
    public void iTestBrowserCompatibilityErrorHandling() {
        logger.info("Testing browser compatibility error handling scenarios");
        lastTestResults = errorHandlingUtil.testBrowserCompatibilityErrors();
        logger.info("Browser compatibility error testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I test timeout error scenarios")
    public void iTestTimeoutErrorScenarios() {
        logger.info("Testing timeout error scenarios");
        List<ErrorScenario> networkErrors = errorHandlingUtil.testNetworkErrors();
        
        // Filter for timeout-related scenarios
        lastTestResults = networkErrors.stream()
                .filter(scenario -> scenario.getId().contains("timeout"))
                .toList();
        
        logger.info("Timeout error testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I test connection error scenarios")
    public void iTestConnectionErrorScenarios() {
        logger.info("Testing connection error scenarios");
        List<ErrorScenario> networkErrors = errorHandlingUtil.testNetworkErrors();
        
        // Filter for connection-related scenarios
        lastTestResults = networkErrors.stream()
                .filter(scenario -> scenario.getId().contains("connection") || scenario.getId().contains("network"))
                .toList();
        
        logger.info("Connection error testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I test HTTP error status handling")
    public void iTestHTTPErrorStatusHandling() {
        logger.info("Testing HTTP error status handling");
        List<ErrorScenario> apiErrors = errorHandlingUtil.testAPIErrors();
        
        // Filter for HTTP status code scenarios
        lastTestResults = apiErrors.stream()
                .filter(scenario -> scenario.getId().matches("\\d{3}_.*"))
                .toList();
        
        logger.info("HTTP error status testing completed. Scenarios: {}", lastTestResults.size());
    }
    
    @When("I simulate invalid promotion code entry")
    public void iSimulateInvalidPromotionCodeEntry() {
        logger.info("Simulating invalid promotion code entry");
        List<ErrorScenario> businessErrors = errorHandlingUtil.testBusinessLogicErrors();
        
        // Filter for promotion-related scenarios
        lastTestResults = businessErrors.stream()
                .filter(scenario -> scenario.getId().contains("promotion"))
                .toList();
        
        logger.info("Invalid promotion code simulation completed");
    }
    
    @When("I simulate form validation errors")
    public void iSimulateFormValidationErrors() {
        logger.info("Simulating form validation errors");
        List<ErrorScenario> formErrors = errorHandlingUtil.testFormValidationErrors();
        
        // Filter for basic validation scenarios
        lastTestResults = formErrors.stream()
                .filter(scenario -> scenario.getId().contains("empty") || scenario.getId().contains("invalid"))
                .toList();
        
        logger.info("Form validation error simulation completed");
    }
    
    @When("I simulate element interaction errors")
    public void iSimulateElementInteractionErrors() {
        logger.info("Simulating element interaction errors");
        List<ErrorScenario> uiErrors = errorHandlingUtil.testUIErrors();
        
        // Filter for element interaction scenarios
        lastTestResults = uiErrors.stream()
                .filter(scenario -> scenario.getId().contains("element"))
                .toList();
        
        logger.info("Element interaction error simulation completed");
    }
    
    @When("I test comprehensive error handling")
    public void iTestComprehensiveErrorHandling() {
        logger.info("Testing comprehensive error handling across all categories");
        
        List<ErrorScenario> allErrors = errorHandlingUtil.testNetworkErrors();
        allErrors.addAll(errorHandlingUtil.testAPIErrors());
        allErrors.addAll(errorHandlingUtil.testUIErrors());
        allErrors.addAll(errorHandlingUtil.testFormValidationErrors());
        allErrors.addAll(errorHandlingUtil.testBusinessLogicErrors());
        allErrors.addAll(errorHandlingUtil.testBrowserCompatibilityErrors());
        
        lastTestResults = allErrors;
        logger.info("Comprehensive error handling testing completed. Total scenarios: {}", lastTestResults.size());
    }
    
    @Then("error handling should be robust")
    public void errorHandlingShouldBeRobust() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull().isNotEmpty();
        
        long failedScenarios = lastTestResults.stream()
                .filter(scenario -> "FAIL".equals(scenario.getResult()))
                .count();
        
        long totalScenarios = lastTestResults.size();
        double successRate = ((double) (totalScenarios - failedScenarios) / totalScenarios) * 100;
        
        assertThat(successRate)
                .as("Error handling success rate should be at least 80%% but was %.1f%%", successRate)
                .isGreaterThanOrEqualTo(80.0);
        
        logger.info("Error handling robustness check passed. Success rate: {:.1f}%", successRate);
    }
    
    @Then("all critical errors should be handled gracefully")
    public void allCriticalErrorsShouldBeHandledGracefully() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        List<ErrorScenario> criticalErrors = lastTestResults.stream()
                .filter(scenario -> scenario.getId().contains("timeout") || 
                                  scenario.getId().contains("connection") ||
                                  scenario.getId().contains("500") ||
                                  scenario.getId().contains("503"))
                .toList();
        
        long failedCriticalErrors = criticalErrors.stream()
                .filter(scenario -> "FAIL".equals(scenario.getResult()))
                .count();
        
        assertThat(failedCriticalErrors)
                .as("No critical errors should fail handling, but %d failed", failedCriticalErrors)
                .isEqualTo(0);
        
        logger.info("Critical error handling check passed. All {} critical scenarios handled gracefully", 
                   criticalErrors.size());
    }
    
    @Then("user-friendly error messages should be displayed")
    public void userFriendlyErrorMessagesShouldBeDisplayed() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        // Check for scenarios that should display user-friendly messages
        List<ErrorScenario> userFacingErrors = lastTestResults.stream()
                .filter(scenario -> scenario.getId().contains("400") ||
                                  scenario.getId().contains("401") ||
                                  scenario.getId().contains("403") ||
                                  scenario.getId().contains("404") ||
                                  scenario.getId().contains("validation") ||
                                  scenario.getId().contains("promotion"))
                .toList();
        
        long failedUserFacingErrors = userFacingErrors.stream()
                .filter(scenario -> "FAIL".equals(scenario.getResult()))
                .count();
        
        // Allow some tolerance for user-facing error messages
        double errorRate = (double) failedUserFacingErrors / Math.max(1, userFacingErrors.size());
        
        assertThat(errorRate)
                .as("User-facing error message failure rate should be less than 30%% but was %.1f%%", errorRate * 100)
                .isLessThan(0.3);
        
        logger.info("User-friendly error message check passed. Error rate: {:.1f}%", errorRate * 100);
    }
    
    @Then("no unhandled exceptions should occur")
    public void noUnhandledExceptionsShouldOccur() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        long unhandledErrors = lastTestResults.stream()
                .filter(scenario -> "ERROR".equals(scenario.getResult()))
                .count();
        
        assertThat(unhandledErrors)
                .as("No unhandled exceptions should occur, but found %d", unhandledErrors)
                .isEqualTo(0);
        
        logger.info("Unhandled exception check passed. No unhandled exceptions found");
    }
    
    @Then("form validation errors should be properly displayed")
    public void formValidationErrorsShouldBeProperlyDisplayed() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        List<ErrorScenario> validationErrors = lastTestResults.stream()
                .filter(scenario -> scenario.getId().contains("validation") || 
                                  scenario.getId().contains("empty") ||
                                  scenario.getId().contains("invalid"))
                .toList();
        
        long failedValidationErrors = validationErrors.stream()
                .filter(scenario -> "FAIL".equals(scenario.getResult()))
                .count();
        
        assertThat(failedValidationErrors)
                .as("Form validation errors should be properly displayed, but %d failed", failedValidationErrors)
                .isLessThanOrEqualTo(1); // Allow 1 failure for edge cases
        
        logger.info("Form validation error display check passed. Failed scenarios: {}", failedValidationErrors);
    }
    
    @Then("network connectivity issues should be handled")
    public void networkConnectivityIssuesShouldBeHandled() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        List<ErrorScenario> networkErrors = lastTestResults.stream()
                .filter(scenario -> scenario.getId().contains("timeout") ||
                                  scenario.getId().contains("connection") ||
                                  scenario.getId().contains("network"))
                .toList();
        
        long failedNetworkErrors = networkErrors.stream()
                .filter(scenario -> "FAIL".equals(scenario.getResult()))
                .count();
        
        assertThat(failedNetworkErrors)
                .as("Network connectivity issues should be handled, but %d failed", failedNetworkErrors)
                .isEqualTo(0);
        
        logger.info("Network connectivity handling check passed. All {} network scenarios handled", 
                   networkErrors.size());
    }
    
    @Then("API errors should have appropriate responses")
    public void apiErrorsShouldHaveAppropriateResponses() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        List<ErrorScenario> apiErrors = lastTestResults.stream()
                .filter(scenario -> scenario.getId().matches("\\d{3}_.*"))
                .toList();
        
        long failedApiErrors = apiErrors.stream()
                .filter(scenario -> "FAIL".equals(scenario.getResult()))
                .count();
        
        double apiErrorRate = (double) failedApiErrors / Math.max(1, apiErrors.size());
        
        assertThat(apiErrorRate)
                .as("API error handling failure rate should be less than 20%% but was %.1f%%", apiErrorRate * 100)
                .isLessThan(0.2);
        
        logger.info("API error response check passed. Error rate: {:.1f}%", apiErrorRate * 100);
    }
    
    @Then("browser compatibility errors should be minimized")
    public void browserCompatibilityErrorsShouldBeMinimized() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        List<ErrorScenario> browserErrors = lastTestResults.stream()
                .filter(scenario -> scenario.getId().contains("browser") ||
                                  scenario.getId().contains("compatibility"))
                .toList();
        
        long failedBrowserErrors = browserErrors.stream()
                .filter(scenario -> "FAIL".equals(scenario.getResult()))
                .count();
        
        // Browser compatibility may have some acceptable failures
        double browserErrorRate = (double) failedBrowserErrors / Math.max(1, browserErrors.size());
        
        assertThat(browserErrorRate)
                .as("Browser compatibility error rate should be less than 40%% but was %.1f%%", browserErrorRate * 100)
                .isLessThan(0.4);
        
        logger.info("Browser compatibility check passed. Error rate: {:.1f}%", browserErrorRate * 100);
    }
    
    @Then("error recovery mechanisms should work")
    public void errorRecoveryMechanismsShouldWork() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        // Check that error scenarios that should have recovery mechanisms are handled
        List<ErrorScenario> recoverableErrors = lastTestResults.stream()
                .filter(scenario -> scenario.getId().contains("timeout") ||
                                  scenario.getId().contains("network") ||
                                  scenario.getId().contains("stale"))
                .toList();
        
        long failedRecoverableErrors = recoverableErrors.stream()
                .filter(scenario -> "FAIL".equals(scenario.getResult()))
                .count();
        
        assertThat(failedRecoverableErrors)
                .as("Error recovery mechanisms should work, but %d recoverable errors failed", failedRecoverableErrors)
                .isLessThanOrEqualTo(1); // Allow 1 failure for complex recovery scenarios
        
        logger.info("Error recovery mechanism check passed. Failed recoverable scenarios: {}", failedRecoverableErrors);
    }
    
    @Then("an error handling report should be generated")
    public void anErrorHandlingReportShouldBeGenerated() {
        assertThat(lastTestResults).as("Error test results should be available for reporting").isNotNull();
        assertThat(lastTestResults).as("Error test results should not be empty").isNotEmpty();
        
        // The report is automatically generated in the @After method
        logger.info("Error handling report will be generated with {} scenarios", lastTestResults.size());
    }
    
    @Then("the overall error handling should be acceptable")
    public void theOverallErrorHandlingShouldBeAcceptable() {
        assertThat(lastTestResults).as("Error test results should be available").isNotNull();
        
        long totalScenarios = lastTestResults.size();
        long passedScenarios = lastTestResults.stream().filter(e -> "PASS".equals(e.getResult())).count();
        long failedScenarios = lastTestResults.stream().filter(e -> "FAIL".equals(e.getResult())).count();
        long errorScenarios = lastTestResults.stream().filter(e -> "ERROR".equals(e.getResult())).count();
        long skippedScenarios = lastTestResults.stream().filter(e -> "SKIP".equals(e.getResult())).count();
        
        // Calculate acceptance criteria
        double passRate = (double) passedScenarios / Math.max(1, totalScenarios - skippedScenarios);
        
        assertThat(passRate)
                .as("Overall error handling pass rate should be at least 75%% but was %.1f%%", passRate * 100)
                .isGreaterThanOrEqualTo(0.75);
        
        assertThat(errorScenarios)
                .as("Should have no unhandled errors, but found %d", errorScenarios)
                .isEqualTo(0);
        
        logger.info("Overall error handling is acceptable. Pass rate: {:.1f}% ({} passed, {} failed, {} errors, {} skipped)", 
                   passRate * 100, passedScenarios, failedScenarios, errorScenarios, skippedScenarios);
    }
}