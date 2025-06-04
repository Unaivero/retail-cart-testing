package com.retailer.cart.steps;

import com.retailer.cart.utils.DriverManager;
import com.retailer.cart.utils.SecurityTestingUtil;
import com.retailer.cart.utils.SecurityTestingUtil.SecurityVulnerability;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class SecurityTestingSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityTestingSteps.class);
    
    private WebDriver driver;
    private SecurityTestingUtil securityUtil;
    private List<SecurityVulnerability> foundVulnerabilities;
    private String currentInputSelector;
    private String currentSubmitSelector;
    private String currentTargetUrl;
    
    @Before("@security")
    public void setupSecurityTesting() {
        driver = DriverManager.getDriver();
        securityUtil = new SecurityTestingUtil(driver);
        foundVulnerabilities = new ArrayList<>();
        logger.info("Security testing setup completed");
    }
    
    @After("@security")
    public void tearDownSecurityTesting() {
        if (!foundVulnerabilities.isEmpty()) {
            logger.warn("Security vulnerabilities found during testing:");
            for (SecurityVulnerability vulnerability : foundVulnerabilities) {
                logger.warn("  - {}", vulnerability.toString());
            }
        }
        logger.info("Security testing completed. Total vulnerabilities found: {}", foundVulnerabilities.size());
    }
    
    @Given("I am testing security for the input field {string}")
    public void iAmTestingSecurityForTheInputField(String inputSelector) {
        this.currentInputSelector = inputSelector;
        logger.info("Set target input field for security testing: {}", inputSelector);
    }
    
    @Given("I am using the submit button {string}")
    public void iAmUsingTheSubmitButton(String submitSelector) {
        this.currentSubmitSelector = submitSelector;
        logger.info("Set target submit button for security testing: {}", submitSelector);
    }
    
    @Given("the target URL is {string}")
    public void theTargetURLIs(String targetUrl) {
        this.currentTargetUrl = targetUrl;
        driver.get(targetUrl);
        logger.info("Set target URL for security testing: {}", targetUrl);
    }
    
    @When("I test for XSS vulnerabilities")
    public void iTestForXSSVulnerabilities() {
        assertThat(currentInputSelector).as("Input selector must be set").isNotNull();
        assertThat(currentSubmitSelector).as("Submit selector must be set").isNotNull();
        
        logger.info("Testing for XSS vulnerabilities in input: {}", currentInputSelector);
        List<SecurityVulnerability> xssVulnerabilities = securityUtil.testXSSVulnerabilities(
            currentInputSelector, currentSubmitSelector);
        
        foundVulnerabilities.addAll(xssVulnerabilities);
        logger.info("XSS testing completed. Found {} vulnerabilities", xssVulnerabilities.size());
    }
    
    @When("I test for SQL injection vulnerabilities")
    public void iTestForSQLInjectionVulnerabilities() {
        assertThat(currentInputSelector).as("Input selector must be set").isNotNull();
        assertThat(currentSubmitSelector).as("Submit selector must be set").isNotNull();
        
        logger.info("Testing for SQL injection vulnerabilities in input: {}", currentInputSelector);
        List<SecurityVulnerability> sqlVulnerabilities = securityUtil.testSQLInjectionVulnerabilities(
            currentInputSelector, currentSubmitSelector);
        
        foundVulnerabilities.addAll(sqlVulnerabilities);
        logger.info("SQL injection testing completed. Found {} vulnerabilities", sqlVulnerabilities.size());
    }
    
    @When("I test for CSRF protection")
    public void iTestForCSRFProtection() {
        logger.info("Testing for CSRF protection");
        List<SecurityVulnerability> csrfVulnerabilities = securityUtil.testCSRFProtection();
        
        foundVulnerabilities.addAll(csrfVulnerabilities);
        logger.info("CSRF testing completed. Found {} vulnerabilities", csrfVulnerabilities.size());
    }
    
    @When("I test for sensitive data exposure")
    public void iTestForSensitiveDataExposure() {
        logger.info("Testing for sensitive data exposure");
        List<SecurityVulnerability> dataExposureVulnerabilities = securityUtil.testSensitiveDataExposure();
        
        foundVulnerabilities.addAll(dataExposureVulnerabilities);
        logger.info("Sensitive data exposure testing completed. Found {} vulnerabilities", dataExposureVulnerabilities.size());
    }
    
    @When("I test for missing security headers")
    public void iTestForMissingSecurityHeaders() {
        logger.info("Testing for missing security headers");
        List<SecurityVulnerability> headerVulnerabilities = securityUtil.testSecurityHeaders();
        
        foundVulnerabilities.addAll(headerVulnerabilities);
        logger.info("Security headers testing completed. Found {} vulnerabilities", headerVulnerabilities.size());
    }
    
    @When("I perform an automated security scan")
    public void iPerformAnAutomatedSecurityScan() {
        assertThat(currentTargetUrl).as("Target URL must be set").isNotNull();
        
        logger.info("Performing automated security scan for: {}", currentTargetUrl);
        List<SecurityVulnerability> automatedScanVulnerabilities = securityUtil.performAutomatedScan(currentTargetUrl);
        
        foundVulnerabilities.addAll(automatedScanVulnerabilities);
        logger.info("Automated security scan completed. Found {} vulnerabilities", automatedScanVulnerabilities.size());
    }
    
    @When("I test the promo code input for security vulnerabilities")
    public void iTestThePromoCodeInputForSecurityVulnerabilities() {
        this.currentInputSelector = "#promo-code-input";
        this.currentSubmitSelector = "#apply-promo-btn";
        
        // Test for XSS
        logger.info("Testing promo code input for XSS vulnerabilities");
        List<SecurityVulnerability> xssVulnerabilities = securityUtil.testXSSVulnerabilities(
            currentInputSelector, currentSubmitSelector);
        foundVulnerabilities.addAll(xssVulnerabilities);
        
        // Test for SQL injection
        logger.info("Testing promo code input for SQL injection vulnerabilities");
        List<SecurityVulnerability> sqlVulnerabilities = securityUtil.testSQLInjectionVulnerabilities(
            currentInputSelector, currentSubmitSelector);
        foundVulnerabilities.addAll(sqlVulnerabilities);
        
        logger.info("Promo code security testing completed. Found {} total vulnerabilities", 
                   xssVulnerabilities.size() + sqlVulnerabilities.size());
    }
    
    @When("I test quantity input fields for security vulnerabilities")
    public void iTestQuantityInputFieldsForSecurityVulnerabilities() {
        this.currentInputSelector = ".product-quantity";
        this.currentSubmitSelector = ".update-quantity-btn";
        
        // Test for XSS in quantity fields
        logger.info("Testing quantity input fields for XSS vulnerabilities");
        List<SecurityVulnerability> xssVulnerabilities = securityUtil.testXSSVulnerabilities(
            currentInputSelector, currentSubmitSelector);
        foundVulnerabilities.addAll(xssVulnerabilities);
        
        // Test for SQL injection in quantity fields
        logger.info("Testing quantity input fields for SQL injection vulnerabilities");
        List<SecurityVulnerability> sqlVulnerabilities = securityUtil.testSQLInjectionVulnerabilities(
            currentInputSelector, currentSubmitSelector);
        foundVulnerabilities.addAll(sqlVulnerabilities);
        
        logger.info("Quantity input security testing completed. Found {} total vulnerabilities", 
                   xssVulnerabilities.size() + sqlVulnerabilities.size());
    }
    
    @When("I test all cart forms for CSRF protection")
    public void iTestAllCartFormsForCSRFProtection() {
        logger.info("Testing all cart forms for CSRF protection");
        List<SecurityVulnerability> csrfVulnerabilities = securityUtil.testCSRFProtection();
        
        foundVulnerabilities.addAll(csrfVulnerabilities);
        logger.info("CSRF protection testing for all forms completed. Found {} vulnerabilities", csrfVulnerabilities.size());
    }
    
    @Then("no XSS vulnerabilities should be found")
    public void noXSSVulnerabilitiesShouldBeFound() {
        long xssCount = foundVulnerabilities.stream()
                .filter(v -> v.getType() == SecurityVulnerability.Type.XSS)
                .count();
        
        assertThat(xssCount)
                .as("No XSS vulnerabilities should be found, but found %d", xssCount)
                .isEqualTo(0);
        
        logger.info("XSS vulnerability check passed - no vulnerabilities found");
    }
    
    @Then("no SQL injection vulnerabilities should be found")
    public void noSQLInjectionVulnerabilitiesShouldBeFound() {
        long sqlInjectionCount = foundVulnerabilities.stream()
                .filter(v -> v.getType() == SecurityVulnerability.Type.SQL_INJECTION)
                .count();
        
        assertThat(sqlInjectionCount)
                .as("No SQL injection vulnerabilities should be found, but found %d", sqlInjectionCount)
                .isEqualTo(0);
        
        logger.info("SQL injection vulnerability check passed - no vulnerabilities found");
    }
    
    @Then("CSRF protection should be implemented")
    public void csrfProtectionShouldBeImplemented() {
        long csrfCount = foundVulnerabilities.stream()
                .filter(v -> v.getType() == SecurityVulnerability.Type.CSRF)
                .count();
        
        assertThat(csrfCount)
                .as("CSRF protection should be implemented, but found %d unprotected forms", csrfCount)
                .isEqualTo(0);
        
        logger.info("CSRF protection check passed - all forms are protected");
    }
    
    @Then("no sensitive data should be exposed")
    public void noSensitiveDataShouldBeExposed() {
        long dataExposureCount = foundVulnerabilities.stream()
                .filter(v -> v.getType() == SecurityVulnerability.Type.DATA_EXPOSURE)
                .count();
        
        assertThat(dataExposureCount)
                .as("No sensitive data should be exposed, but found %d exposures", dataExposureCount)
                .isEqualTo(0);
        
        logger.info("Sensitive data exposure check passed - no data exposures found");
    }
    
    @Then("all required security headers should be present")
    public void allRequiredSecurityHeadersShouldBePresent() {
        long headerCount = foundVulnerabilities.stream()
                .filter(v -> v.getType() == SecurityVulnerability.Type.MISSING_SECURITY_HEADER)
                .count();
        
        assertThat(headerCount)
                .as("All required security headers should be present, but %d are missing", headerCount)
                .isEqualTo(0);
        
        logger.info("Security headers check passed - all required headers are present");
    }
    
    @Then("the security scan should pass")
    public void theSecurityScanShouldPass() {
        assertThat(foundVulnerabilities)
                .as("Security scan should pass with no high-severity vulnerabilities, but found: %s", 
                    foundVulnerabilities.stream()
                            .filter(v -> "HIGH".equals(v.getSeverity()) || "CRITICAL".equals(v.getSeverity()))
                            .count())
                .filteredOn(v -> "HIGH".equals(v.getSeverity()) || "CRITICAL".equals(v.getSeverity()))
                .isEmpty();
        
        logger.info("Overall security scan passed - no high-severity vulnerabilities found");
    }
    
    @Then("no more than {int} low-severity vulnerabilities should be found")
    public void noMoreThanLowSeverityVulnerabilitiesShouldBeFound(int maxLowSeverityVulns) {
        long lowSeverityCount = foundVulnerabilities.stream()
                .filter(v -> "LOW".equals(v.getSeverity()))
                .count();
        
        assertThat(lowSeverityCount)
                .as("No more than %d low-severity vulnerabilities should be found, but found %d", 
                    maxLowSeverityVulns, lowSeverityCount)
                .isLessThanOrEqualTo(maxLowSeverityVulns);
        
        logger.info("Low-severity vulnerability threshold check passed: {} <= {}", lowSeverityCount, maxLowSeverityVulns);
    }
    
    @Then("vulnerability details should be logged")
    public void vulnerabilityDetailsShouldBeLogged() {
        // This step ensures that vulnerability details are properly captured and logged
        assertThat(foundVulnerabilities).isNotNull();
        
        for (SecurityVulnerability vulnerability : foundVulnerabilities) {
            assertThat(vulnerability.getDescription()).as("Vulnerability description should not be empty").isNotEmpty();
            assertThat(vulnerability.getType()).as("Vulnerability type should be set").isNotNull();
            assertThat(vulnerability.getSeverity()).as("Vulnerability severity should be set").isNotEmpty();
        }
        
        logger.info("Vulnerability logging validation passed - all vulnerabilities have proper details");
    }
    
    @Then("I should get a security report")
    public void iShouldGetASecurityReport() {
        // Generate and validate security report
        StringBuilder report = new StringBuilder();
        report.append("SECURITY TEST REPORT\n");
        report.append("===================\n");
        report.append("Total vulnerabilities found: ").append(foundVulnerabilities.size()).append("\n\n");
        
        // Group by type
        for (SecurityVulnerability.Type type : SecurityVulnerability.Type.values()) {
            long count = foundVulnerabilities.stream()
                    .filter(v -> v.getType() == type)
                    .count();
            if (count > 0) {
                report.append(type.name()).append(": ").append(count).append(" vulnerabilities\n");
            }
        }
        
        // Group by severity
        report.append("\nBy Severity:\n");
        for (String severity : new String[]{"CRITICAL", "HIGH", "MEDIUM", "LOW"}) {
            long count = foundVulnerabilities.stream()
                    .filter(v -> severity.equals(v.getSeverity()))
                    .count();
            if (count > 0) {
                report.append(severity).append(": ").append(count).append(" vulnerabilities\n");
            }
        }
        
        logger.info("Security Report Generated:\n{}", report.toString());
        
        // Ensure report is not empty
        assertThat(report.toString()).as("Security report should be generated").isNotEmpty();
    }
}