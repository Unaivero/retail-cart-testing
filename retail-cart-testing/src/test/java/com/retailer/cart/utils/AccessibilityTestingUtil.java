package com.retailer.cart.utils;

import com.deque.html.axecore.axedriver.AxeBuilder;
import com.deque.html.axecore.axedriver.AxeReporter;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.Rule;
import com.deque.html.axecore.results.CheckedNode;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class AccessibilityTestingUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(AccessibilityTestingUtil.class);
    private static final String REPORTS_DIR = "target/accessibility-reports";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    private final WebDriver driver;
    private final JavascriptExecutor jsExecutor;
    
    public AccessibilityTestingUtil(WebDriver driver) {
        this.driver = driver;
        this.jsExecutor = (JavascriptExecutor) driver;
        createReportsDirectory();
    }
    
    private void createReportsDirectory() {
        try {
            Files.createDirectories(Paths.get(REPORTS_DIR));
        } catch (IOException e) {
            logger.error("Failed to create accessibility reports directory", e);
        }
    }
    
    /**
     * Performs comprehensive accessibility testing using axe-core
     * @return AccessibilityResults containing all findings
     */
    public AccessibilityResults performFullAccessibilityAudit() {
        return performFullAccessibilityAudit(null, null);
    }
    
    /**
     * Performs accessibility testing with specific rules and tags
     * @param includeTags tags to include (e.g., "wcag2a", "wcag2aa", "wcag21aa")
     * @param excludeRules rules to exclude from testing
     * @return AccessibilityResults containing all findings
     */
    public AccessibilityResults performFullAccessibilityAudit(List<String> includeTags, List<String> excludeRules) {
        logger.info("Starting comprehensive accessibility audit for: {}", driver.getCurrentUrl());
        
        try {
            AxeBuilder axeBuilder = new AxeBuilder();
            
            // Configure tags if provided
            if (includeTags != null && !includeTags.isEmpty()) {
                axeBuilder.withTags(includeTags);
            }
            
            // Configure excluded rules if provided
            if (excludeRules != null && !excludeRules.isEmpty()) {
                axeBuilder.disableRules(excludeRules);
            }
            
            // Run axe analysis
            AxeResults axeResults = axeBuilder.analyze(driver);
            
            // Convert to our custom result format
            AccessibilityResults results = convertAxeResults(axeResults);
            
            // Generate report
            generateAccessibilityReport(results);
            
            logger.info("Accessibility audit completed. Violations: {}, Incomplete: {}, Passes: {}", 
                       results.getViolations().size(), 
                       results.getIncomplete().size(), 
                       results.getPasses().size());
            
            return results;
            
        } catch (Exception e) {
            logger.error("Error performing accessibility audit", e);
            return new AccessibilityResults();
        }
    }
    
    /**
     * Tests specific element for accessibility issues
     * @param elementSelector CSS selector for the element to test
     * @return AccessibilityResults for the specific element
     */
    public AccessibilityResults testElementAccessibility(String elementSelector) {
        logger.info("Testing accessibility for element: {}", elementSelector);
        
        try {
            WebElement element = driver.findElement(By.cssSelector(elementSelector));
            
            AxeBuilder axeBuilder = new AxeBuilder();
            AxeResults axeResults = axeBuilder.include(elementSelector).analyze(driver);
            
            AccessibilityResults results = convertAxeResults(axeResults);
            
            logger.info("Element accessibility test completed for: {}. Violations: {}", 
                       elementSelector, results.getViolations().size());
            
            return results;
            
        } catch (Exception e) {
            logger.error("Error testing element accessibility for: " + elementSelector, e);
            return new AccessibilityResults();
        }
    }
    
    /**
     * Tests keyboard navigation accessibility
     * @return AccessibilityResults focusing on keyboard navigation issues
     */
    public AccessibilityResults testKeyboardNavigation() {
        logger.info("Testing keyboard navigation accessibility");
        
        List<String> keyboardTags = Arrays.asList("keyboard");
        return performFullAccessibilityAudit(keyboardTags, null);
    }
    
    /**
     * Tests color contrast accessibility
     * @return AccessibilityResults focusing on color contrast issues
     */
    public AccessibilityResults testColorContrast() {
        logger.info("Testing color contrast accessibility");
        
        List<String> colorTags = Arrays.asList("color-contrast");
        return performFullAccessibilityAudit(colorTags, null);
    }
    
    /**
     * Tests ARIA implementation
     * @return AccessibilityResults focusing on ARIA issues
     */
    public AccessibilityResults testARIAImplementation() {
        logger.info("Testing ARIA implementation");
        
        List<String> ariaTags = Arrays.asList("aria");
        return performFullAccessibilityAudit(ariaTags, null);
    }
    
    /**
     * Tests WCAG 2.1 AA compliance
     * @return AccessibilityResults for WCAG 2.1 AA compliance
     */
    public AccessibilityResults testWCAG21AACompliance() {
        logger.info("Testing WCAG 2.1 AA compliance");
        
        List<String> wcagTags = Arrays.asList("wcag2a", "wcag2aa", "wcag21aa");
        return performFullAccessibilityAudit(wcagTags, null);
    }
    
    /**
     * Tests form accessibility
     * @return AccessibilityResults focusing on form accessibility
     */
    public AccessibilityResults testFormAccessibility() {
        logger.info("Testing form accessibility");
        
        try {
            // Find all forms on the page
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            AccessibilityResults combinedResults = new AccessibilityResults();
            
            for (int i = 0; i < forms.size(); i++) {
                String formSelector = "form:nth-of-type(" + (i + 1) + ")";
                AccessibilityResults formResults = testElementAccessibility(formSelector);
                combinedResults.addResults(formResults);
            }
            
            // Also test form-related WCAG rules
            List<String> formTags = Arrays.asList("forms");
            AccessibilityResults generalFormResults = performFullAccessibilityAudit(formTags, null);
            combinedResults.addResults(generalFormResults);
            
            return combinedResults;
            
        } catch (Exception e) {
            logger.error("Error testing form accessibility", e);
            return new AccessibilityResults();
        }
    }
    
    /**
     * Tests image accessibility (alt text, etc.)
     * @return AccessibilityResults focusing on image accessibility
     */
    public AccessibilityResults testImageAccessibility() {
        logger.info("Testing image accessibility");
        
        try {
            List<WebElement> images = driver.findElements(By.tagName("img"));
            AccessibilityResults results = new AccessibilityResults();
            
            for (WebElement img : images) {
                String alt = img.getAttribute("alt");
                String src = img.getAttribute("src");
                
                if (alt == null || alt.trim().isEmpty()) {
                    AccessibilityViolation violation = new AccessibilityViolation(
                        "missing-alt-text",
                        "Image missing alt text",
                        "critical",
                        "Images must have alternative text",
                        Arrays.asList("wcag2a", "section508"),
                        src
                    );
                    results.addViolation(violation);
                }
            }
            
            // Run axe test for images
            List<String> imageTags = Arrays.asList("images");
            AccessibilityResults axeImageResults = performFullAccessibilityAudit(imageTags, null);
            results.addResults(axeImageResults);
            
            return results;
            
        } catch (Exception e) {
            logger.error("Error testing image accessibility", e);
            return new AccessibilityResults();
        }
    }
    
    /**
     * Tests link accessibility
     * @return AccessibilityResults focusing on link accessibility
     */
    public AccessibilityResults testLinkAccessibility() {
        logger.info("Testing link accessibility");
        
        try {
            List<WebElement> links = driver.findElements(By.tagName("a"));
            AccessibilityResults results = new AccessibilityResults();
            
            for (WebElement link : links) {
                String text = link.getText().trim();
                String href = link.getAttribute("href");
                String ariaLabel = link.getAttribute("aria-label");
                String title = link.getAttribute("title");
                
                // Check for empty link text
                if (text.isEmpty() && (ariaLabel == null || ariaLabel.trim().isEmpty()) 
                    && (title == null || title.trim().isEmpty())) {
                    
                    AccessibilityViolation violation = new AccessibilityViolation(
                        "empty-link-text",
                        "Link has no accessible text",
                        "serious",
                        "Links must have accessible text",
                        Arrays.asList("wcag2a"),
                        href
                    );
                    results.addViolation(violation);
                }
                
                // Check for generic link text
                if (text.toLowerCase().matches("click here|here|more|read more|link")) {
                    AccessibilityViolation violation = new AccessibilityViolation(
                        "generic-link-text",
                        "Link has generic text: " + text,
                        "moderate",
                        "Links should have descriptive text",
                        Arrays.asList("wcag2a"),
                        href
                    );
                    results.addViolation(violation);
                }
            }
            
            return results;
            
        } catch (Exception e) {
            logger.error("Error testing link accessibility", e);
            return new AccessibilityResults();
        }
    }
    
    /**
     * Tests heading structure accessibility
     * @return AccessibilityResults focusing on heading structure
     */
    public AccessibilityResults testHeadingStructure() {
        logger.info("Testing heading structure accessibility");
        
        try {
            AccessibilityResults results = new AccessibilityResults();
            
            // Get all headings
            List<WebElement> headings = driver.findElements(By.cssSelector("h1, h2, h3, h4, h5, h6"));
            
            if (headings.isEmpty()) {
                AccessibilityViolation violation = new AccessibilityViolation(
                    "no-headings",
                    "Page has no headings",
                    "moderate",
                    "Pages should have a proper heading structure",
                    Arrays.asList("wcag2a"),
                    driver.getCurrentUrl()
                );
                results.addViolation(violation);
                return results;
            }
            
            // Check for proper heading hierarchy
            int lastLevel = 0;
            for (WebElement heading : headings) {
                String tagName = heading.getTagName().toLowerCase();
                int currentLevel = Integer.parseInt(tagName.substring(1));
                
                if (lastLevel == 0) {
                    // First heading should be h1
                    if (currentLevel != 1) {
                        AccessibilityViolation violation = new AccessibilityViolation(
                            "first-heading-not-h1",
                            "First heading is not h1",
                            "moderate",
                            "Page should start with h1 heading",
                            Arrays.asList("wcag2a"),
                            heading.getText()
                        );
                        results.addViolation(violation);
                    }
                } else {
                    // Check for skipped heading levels
                    if (currentLevel > lastLevel + 1) {
                        AccessibilityViolation violation = new AccessibilityViolation(
                            "skipped-heading-level",
                            "Heading level skipped from h" + lastLevel + " to h" + currentLevel,
                            "moderate",
                            "Heading levels should not be skipped",
                            Arrays.asList("wcag2a"),
                            heading.getText()
                        );
                        results.addViolation(violation);
                    }
                }
                
                lastLevel = currentLevel;
            }
            
            return results;
            
        } catch (Exception e) {
            logger.error("Error testing heading structure", e);
            return new AccessibilityResults();
        }
    }
    
    /**
     * Converts AxeResults to our custom AccessibilityResults format
     */
    private AccessibilityResults convertAxeResults(AxeResults axeResults) {
        AccessibilityResults results = new AccessibilityResults();
        
        // Convert violations
        for (Rule violation : axeResults.getViolations()) {
            for (CheckedNode node : violation.getNodes()) {
                AccessibilityViolation accessibilityViolation = new AccessibilityViolation(
                    violation.getId(),
                    violation.getDescription(),
                    violation.getImpact(),
                    violation.getHelp(),
                    violation.getTags(),
                    node.getTarget().toString()
                );
                results.addViolation(accessibilityViolation);
            }
        }
        
        // Convert incomplete issues
        for (Rule incomplete : axeResults.getIncomplete()) {
            for (CheckedNode node : incomplete.getNodes()) {
                AccessibilityViolation incompleteIssue = new AccessibilityViolation(
                    incomplete.getId(),
                    incomplete.getDescription(),
                    "needs-review",
                    incomplete.getHelp(),
                    incomplete.getTags(),
                    node.getTarget().toString()
                );
                results.addIncomplete(incompleteIssue);
            }
        }
        
        // Convert passes
        for (Rule pass : axeResults.getPasses()) {
            AccessibilityViolation passedRule = new AccessibilityViolation(
                pass.getId(),
                pass.getDescription(),
                "pass",
                pass.getHelp(),
                pass.getTags(),
                "N/A"
            );
            results.addPass(passedRule);
        }
        
        return results;
    }
    
    /**
     * Generates accessibility report
     */
    private void generateAccessibilityReport(AccessibilityResults results) {
        try {
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String fileName = REPORTS_DIR + "/accessibility_report_" + timestamp + ".html";
            
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html>\n");
            html.append("<html lang=\"en\">\n");
            html.append("<head>\n");
            html.append("  <meta charset=\"UTF-8\">\n");
            html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
            html.append("  <title>Accessibility Report</title>\n");
            html.append("  <style>\n");
            html.append("    body { font-family: Arial, sans-serif; margin: 20px; }\n");
            html.append("    .violation { border-left: 4px solid #d32f2f; padding: 10px; margin: 10px 0; background: #ffebee; }\n");
            html.append("    .incomplete { border-left: 4px solid #f57c00; padding: 10px; margin: 10px 0; background: #fff3e0; }\n");
            html.append("    .pass { border-left: 4px solid #388e3c; padding: 10px; margin: 10px 0; background: #e8f5e8; }\n");
            html.append("    .summary { background: #f5f5f5; padding: 15px; margin: 20px 0; border-radius: 5px; }\n");
            html.append("    .critical { color: #d32f2f; font-weight: bold; }\n");
            html.append("    .serious { color: #f57c00; font-weight: bold; }\n");
            html.append("    .moderate { color: #1976d2; }\n");
            html.append("    .minor { color: #388e3c; }\n");
            html.append("  </style>\n");
            html.append("</head>\n");
            html.append("<body>\n");
            
            html.append("<h1>Accessibility Report</h1>\n");
            html.append("<p>Generated: ").append(timestamp).append("</p>\n");
            html.append("<p>URL: ").append(driver.getCurrentUrl()).append("</p>\n");
            
            // Summary
            html.append("<div class=\"summary\">\n");
            html.append("<h2>Summary</h2>\n");
            html.append("<p>Total Violations: ").append(results.getViolations().size()).append("</p>\n");
            html.append("<p>Issues Needing Review: ").append(results.getIncomplete().size()).append("</p>\n");
            html.append("<p>Passed Rules: ").append(results.getPasses().size()).append("</p>\n");
            html.append("</div>\n");
            
            // Violations
            if (!results.getViolations().isEmpty()) {
                html.append("<h2>Violations</h2>\n");
                for (AccessibilityViolation violation : results.getViolations()) {
                    html.append("<div class=\"violation\">\n");
                    html.append("<h3>").append(violation.getDescription()).append("</h3>\n");
                    html.append("<p class=\"").append(violation.getImpact()).append("\">Impact: ").append(violation.getImpact()).append("</p>\n");
                    html.append("<p>").append(violation.getHelp()).append("</p>\n");
                    html.append("<p><strong>Element:</strong> ").append(violation.getTarget()).append("</p>\n");
                    html.append("<p><strong>Tags:</strong> ").append(String.join(", ", violation.getTags())).append("</p>\n");
                    html.append("</div>\n");
                }
            }
            
            // Incomplete
            if (!results.getIncomplete().isEmpty()) {
                html.append("<h2>Issues Needing Review</h2>\n");
                for (AccessibilityViolation incomplete : results.getIncomplete()) {
                    html.append("<div class=\"incomplete\">\n");
                    html.append("<h3>").append(incomplete.getDescription()).append("</h3>\n");
                    html.append("<p>").append(incomplete.getHelp()).append("</p>\n");
                    html.append("<p><strong>Element:</strong> ").append(incomplete.getTarget()).append("</p>\n");
                    html.append("</div>\n");
                }
            }
            
            html.append("</body>\n");
            html.append("</html>");
            
            Files.write(Paths.get(fileName), html.toString().getBytes());
            logger.info("Accessibility report generated: {}", fileName);
            
        } catch (IOException e) {
            logger.error("Failed to generate accessibility report", e);
        }
    }
    
    /**
     * Represents accessibility test results
     */
    public static class AccessibilityResults {
        private final List<AccessibilityViolation> violations = new ArrayList<>();
        private final List<AccessibilityViolation> incomplete = new ArrayList<>();
        private final List<AccessibilityViolation> passes = new ArrayList<>();
        
        public void addViolation(AccessibilityViolation violation) {
            violations.add(violation);
        }
        
        public void addIncomplete(AccessibilityViolation incomplete) {
            this.incomplete.add(incomplete);
        }
        
        public void addPass(AccessibilityViolation pass) {
            passes.add(pass);
        }
        
        public void addResults(AccessibilityResults other) {
            violations.addAll(other.violations);
            incomplete.addAll(other.incomplete);
            passes.addAll(other.passes);
        }
        
        public List<AccessibilityViolation> getViolations() { return violations; }
        public List<AccessibilityViolation> getIncomplete() { return incomplete; }
        public List<AccessibilityViolation> getPasses() { return passes; }
        
        public boolean hasViolations() { return !violations.isEmpty(); }
        public boolean hasIncomplete() { return !incomplete.isEmpty(); }
        
        public long getCriticalViolations() {
            return violations.stream().filter(v -> "critical".equals(v.getImpact())).count();
        }
        
        public long getSeriousViolations() {
            return violations.stream().filter(v -> "serious".equals(v.getImpact())).count();
        }
    }
    
    /**
     * Represents an accessibility violation
     */
    public static class AccessibilityViolation {
        private final String id;
        private final String description;
        private final String impact;
        private final String help;
        private final List<String> tags;
        private final String target;
        
        public AccessibilityViolation(String id, String description, String impact, 
                                    String help, List<String> tags, String target) {
            this.id = id;
            this.description = description;
            this.impact = impact;
            this.help = help;
            this.tags = tags != null ? new ArrayList<>(tags) : new ArrayList<>();
            this.target = target;
        }
        
        // Getters
        public String getId() { return id; }
        public String getDescription() { return description; }
        public String getImpact() { return impact; }
        public String getHelp() { return help; }
        public List<String> getTags() { return tags; }
        public String getTarget() { return target; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - Impact: %s, Target: %s", 
                               id, description, impact, target);
        }
    }
}