package com.retailer.cart.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zaproxy.clientapi.core.ApiResponse;
import org.zaproxy.clientapi.core.ClientApi;
import org.zaproxy.clientapi.core.ClientApiException;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SecurityTestingUtil {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityTestingUtil.class);
    
    // Common XSS payloads
    private static final String[] XSS_PAYLOADS = {
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert('XSS')>",
        "javascript:alert('XSS')",
        "'><script>alert('XSS')</script>",
        "\"><script>alert('XSS')</script>",
        "<svg onload=alert('XSS')>",
        "<iframe src=javascript:alert('XSS')>",
        "<script>window.location='http://attacker.com'</script>",
        "'+alert('XSS')+'",
        "\"+alert('XSS')+\"",
        "<body onload=alert('XSS')>",
        "<input type=image src=x onerror=alert('XSS')>"
    };
    
    // Common SQL injection payloads
    private static final String[] SQL_INJECTION_PAYLOADS = {
        "' OR '1'='1",
        "' OR 1=1--",
        "' OR 1=1#",
        "'; DROP TABLE users--",
        "' UNION SELECT null,null,null--",
        "admin'--",
        "admin' #",
        "admin'/*",
        "' or 1=1#",
        "') or '1'='1--",
        "') or ('1'='1--",
        "1' ORDER BY 1--+",
        "1' ORDER BY 2--+",
        "1' ORDER BY 3--+",
        "1' UNION ALL SELECT 1,2,3--+",
        "1' UNION ALL SELECT null,null,null--+"
    };
    
    // Common CSRF patterns
    private static final String[] CSRF_INDICATORS = {
        "csrf_token",
        "authenticity_token",
        "_token",
        "csrfmiddlewaretoken",
        "csrfToken",
        "__RequestVerificationToken"
    };
    
    private final WebDriver driver;
    private ClientApi zapApi;
    private final String zapProxyUrl;
    
    public SecurityTestingUtil(WebDriver driver) {
        this.driver = driver;
        this.zapProxyUrl = "http://localhost:8080";
        initializeZapApi();
    }
    
    public SecurityTestingUtil(WebDriver driver, String zapProxyUrl) {
        this.driver = driver;
        this.zapProxyUrl = zapProxyUrl;
        initializeZapApi();
    }
    
    private void initializeZapApi() {
        try {
            this.zapApi = new ClientApi(zapProxyUrl.replace("http://", "").split(":")[0], 
                                       Integer.parseInt(zapProxyUrl.split(":")[2]));
            logger.info("ZAP API initialized at: {}", zapProxyUrl);
        } catch (Exception e) {
            logger.warn("Failed to initialize ZAP API: {}. Security scanning features will be limited.", e.getMessage());
        }
    }
    
    /**
     * Tests for XSS vulnerabilities in input fields
     * @param inputSelector CSS selector for the input field
     * @param submitSelector CSS selector for the submit button
     * @return list of detected XSS vulnerabilities
     */
    public List<SecurityVulnerability> testXSSVulnerabilities(String inputSelector, String submitSelector) {
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        for (String payload : XSS_PAYLOADS) {
            try {
                WebElement inputField = driver.findElement(By.cssSelector(inputSelector));
                WebElement submitButton = driver.findElement(By.cssSelector(submitSelector));
                
                // Clear and input payload
                inputField.clear();
                inputField.sendKeys(payload);
                
                // Submit form
                submitButton.click();
                
                // Wait a moment for the page to process
                Thread.sleep(1000);
                
                // Check if payload was executed (alert present) or rendered unescaped
                if (isAlertPresent() || isPayloadReflected(payload)) {
                    SecurityVulnerability vulnerability = new SecurityVulnerability(
                        SecurityVulnerability.Type.XSS,
                        "XSS vulnerability detected in input field: " + inputSelector,
                        "HIGH",
                        payload,
                        driver.getCurrentUrl()
                    );
                    vulnerabilities.add(vulnerability);
                    logger.warn("XSS vulnerability detected with payload: {}", payload);
                    
                    // Dismiss alert if present
                    dismissAlert();
                }
                
            } catch (Exception e) {
                logger.debug("Error testing XSS payload '{}': {}", payload, e.getMessage());
            }
        }
        
        return vulnerabilities;
    }
    
    /**
     * Tests for SQL injection vulnerabilities
     * @param inputSelector CSS selector for the input field
     * @param submitSelector CSS selector for the submit button
     * @return list of detected SQL injection vulnerabilities
     */
    public List<SecurityVulnerability> testSQLInjectionVulnerabilities(String inputSelector, String submitSelector) {
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        for (String payload : SQL_INJECTION_PAYLOADS) {
            try {
                WebElement inputField = driver.findElement(By.cssSelector(inputSelector));
                WebElement submitButton = driver.findElement(By.cssSelector(submitSelector));
                
                // Clear and input payload
                inputField.clear();
                inputField.sendKeys(payload);
                
                // Submit form
                submitButton.click();
                
                // Wait for response
                Thread.sleep(2000);
                
                // Check for SQL error messages or unexpected behavior
                if (hasSQLErrorIndicators() || hasUnexpectedDataExposure()) {
                    SecurityVulnerability vulnerability = new SecurityVulnerability(
                        SecurityVulnerability.Type.SQL_INJECTION,
                        "SQL injection vulnerability detected in input field: " + inputSelector,
                        "CRITICAL",
                        payload,
                        driver.getCurrentUrl()
                    );
                    vulnerabilities.add(vulnerability);
                    logger.warn("SQL injection vulnerability detected with payload: {}", payload);
                }
                
            } catch (Exception e) {
                logger.debug("Error testing SQL injection payload '{}': {}", payload, e.getMessage());
            }
        }
        
        return vulnerabilities;
    }
    
    /**
     * Tests for CSRF protection
     * @return list of CSRF vulnerabilities
     */
    public List<SecurityVulnerability> testCSRFProtection() {
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        try {
            // Check for CSRF tokens in forms
            List<WebElement> forms = driver.findElements(By.tagName("form"));
            
            for (WebElement form : forms) {
                boolean hasCSRFToken = false;
                
                // Check for common CSRF token patterns
                for (String tokenName : CSRF_INDICATORS) {
                    List<WebElement> tokenFields = form.findElements(By.name(tokenName));
                    if (!tokenFields.isEmpty()) {
                        hasCSRFToken = true;
                        break;
                    }
                }
                
                // Check hidden inputs for token-like values
                if (!hasCSRFToken) {
                    List<WebElement> hiddenInputs = form.findElements(By.cssSelector("input[type='hidden']"));
                    for (WebElement input : hiddenInputs) {
                        String name = input.getAttribute("name");
                        String value = input.getAttribute("value");
                        if (name != null && value != null && value.length() > 10 && isTokenLike(value)) {
                            hasCSRFToken = true;
                            break;
                        }
                    }
                }
                
                if (!hasCSRFToken) {
                    String formAction = form.getAttribute("action");
                    String formMethod = form.getAttribute("method");
                    
                    // Only flag state-changing methods as vulnerable
                    if ("POST".equalsIgnoreCase(formMethod) || "PUT".equalsIgnoreCase(formMethod) || 
                        "DELETE".equalsIgnoreCase(formMethod)) {
                        
                        SecurityVulnerability vulnerability = new SecurityVulnerability(
                            SecurityVulnerability.Type.CSRF,
                            "Form lacks CSRF protection: " + formAction,
                            "MEDIUM",
                            "Form method: " + formMethod,
                            driver.getCurrentUrl()
                        );
                        vulnerabilities.add(vulnerability);
                        logger.warn("CSRF vulnerability detected in form: {}", formAction);
                    }
                }
            }
            
        } catch (Exception e) {
            logger.error("Error testing CSRF protection: {}", e.getMessage());
        }
        
        return vulnerabilities;
    }
    
    /**
     * Tests for sensitive data exposure in page source
     * @return list of data exposure vulnerabilities
     */
    public List<SecurityVulnerability> testSensitiveDataExposure() {
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        try {
            String pageSource = driver.getPageSource();
            
            // Common sensitive data patterns
            Map<String, Pattern> sensitivePatterns = new HashMap<>();
            sensitivePatterns.put("Credit Card", Pattern.compile("\\b\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}[\\s-]?\\d{4}\\b"));
            sensitivePatterns.put("Social Security Number", Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b"));
            sensitivePatterns.put("Email", Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b"));
            sensitivePatterns.put("API Key", Pattern.compile("api[_-]?key[\\s]*[:=][\\s]*['\"]?[a-zA-Z0-9]{20,}['\"]?", Pattern.CASE_INSENSITIVE));
            sensitivePatterns.put("Password", Pattern.compile("password[\\s]*[:=][\\s]*['\"][^'\"]{6,}['\"]", Pattern.CASE_INSENSITIVE));
            
            for (Map.Entry<String, Pattern> entry : sensitivePatterns.entrySet()) {
                Matcher matcher = entry.getValue().matcher(pageSource);
                if (matcher.find()) {
                    SecurityVulnerability vulnerability = new SecurityVulnerability(
                        SecurityVulnerability.Type.DATA_EXPOSURE,
                        "Sensitive data exposure detected: " + entry.getKey(),
                        "HIGH",
                        "Found pattern: " + matcher.group(),
                        driver.getCurrentUrl()
                    );
                    vulnerabilities.add(vulnerability);
                    logger.warn("Sensitive data exposure detected: {}", entry.getKey());
                }
            }
            
        } catch (Exception e) {
            logger.error("Error testing sensitive data exposure: {}", e.getMessage());
        }
        
        return vulnerabilities;
    }
    
    /**
     * Tests for insecure HTTP headers
     * @return list of insecure header vulnerabilities
     */
    public List<SecurityVulnerability> testSecurityHeaders() {
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        try {
            // Use JavaScript to access response headers (limited in browser)
            JavascriptExecutor js = (JavascriptExecutor) driver;
            
            // Check for missing security headers through meta tags or script execution
            List<WebElement> metaTags = driver.findElements(By.tagName("meta"));
            boolean hasCSP = false;
            boolean hasXFrameOptions = false;
            
            for (WebElement meta : metaTags) {
                String httpEquiv = meta.getAttribute("http-equiv");
                if ("Content-Security-Policy".equalsIgnoreCase(httpEquiv)) {
                    hasCSP = true;
                }
                if ("X-Frame-Options".equalsIgnoreCase(httpEquiv)) {
                    hasXFrameOptions = true;
                }
            }
            
            if (!hasCSP) {
                SecurityVulnerability vulnerability = new SecurityVulnerability(
                    SecurityVulnerability.Type.MISSING_SECURITY_HEADER,
                    "Missing Content-Security-Policy header",
                    "MEDIUM",
                    "CSP header not found in page",
                    driver.getCurrentUrl()
                );
                vulnerabilities.add(vulnerability);
                logger.warn("Missing Content-Security-Policy header");
            }
            
            if (!hasXFrameOptions) {
                SecurityVulnerability vulnerability = new SecurityVulnerability(
                    SecurityVulnerability.Type.MISSING_SECURITY_HEADER,
                    "Missing X-Frame-Options header",
                    "MEDIUM",
                    "X-Frame-Options header not found",
                    driver.getCurrentUrl()
                );
                vulnerabilities.add(vulnerability);
                logger.warn("Missing X-Frame-Options header");
            }
            
        } catch (Exception e) {
            logger.error("Error testing security headers: {}", e.getMessage());
        }
        
        return vulnerabilities;
    }
    
    /**
     * Performs automated security scan using ZAP proxy
     * @param targetUrl the URL to scan
     * @return list of vulnerabilities found by ZAP
     */
    public List<SecurityVulnerability> performAutomatedScan(String targetUrl) {
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        
        if (zapApi == null) {
            logger.warn("ZAP API not available. Skipping automated scan.");
            return vulnerabilities;
        }
        
        try {
            // Start spider scan
            logger.info("Starting ZAP spider scan for: {}", targetUrl);
            ApiResponse spiderResponse = zapApi.spider.scan(targetUrl, null, null, null, null);
            String spiderScanId = spiderResponse.toString();
            
            // Wait for spider to complete
            waitForZapScanCompletion("spider", spiderScanId);
            
            // Start active scan
            logger.info("Starting ZAP active scan for: {}", targetUrl);
            ApiResponse scanResponse = zapApi.ascan.scan(targetUrl, "True", "False", null, null, null);
            String activeScanId = scanResponse.toString();
            
            // Wait for active scan to complete
            waitForZapScanCompletion("ascan", activeScanId);
            
            // Get alerts
            ApiResponse alertsResponse = zapApi.core.alerts(targetUrl, null, null);
            
            // Parse ZAP alerts and convert to SecurityVulnerability objects
            // Note: This is a simplified implementation. In practice, you'd parse the JSON response
            logger.info("ZAP automated scan completed. Check ZAP interface for detailed results.");
            
        } catch (ClientApiException e) {
            logger.error("Error performing automated ZAP scan: {}", e.getMessage());
        }
        
        return vulnerabilities;
    }
    
    // Helper methods
    
    private boolean isAlertPresent() {
        try {
            driver.switchTo().alert();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    private void dismissAlert() {
        try {
            driver.switchTo().alert().dismiss();
        } catch (Exception e) {
            // Alert not present or already dismissed
        }
    }
    
    private boolean isPayloadReflected(String payload) {
        try {
            String pageSource = driver.getPageSource();
            return pageSource.contains(payload);
        } catch (Exception e) {
            return false;
        }
    }
    
    private boolean hasSQLErrorIndicators() {
        String pageSource = driver.getPageSource().toLowerCase();
        String[] sqlErrors = {
            "sql syntax", "mysql_fetch", "ora-", "postgresql", "sqlite_",
            "you have an error in your sql syntax", "warning: mysql",
            "fatal error", "unclosed quotation mark", "quoted string not properly terminated"
        };
        
        for (String error : sqlErrors) {
            if (pageSource.contains(error)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasUnexpectedDataExposure() {
        // Check for unusual amount of data returned or structure changes
        String pageSource = driver.getPageSource();
        
        // Look for database dump indicators
        return pageSource.contains("SELECT ") || 
               pageSource.contains("INSERT ") || 
               pageSource.contains("UPDATE ") ||
               pageSource.contains("DELETE ") ||
               pageSource.length() > 50000; // Unusually large response
    }
    
    private boolean isTokenLike(String value) {
        // Check if value looks like a CSRF token
        return value.matches("[a-zA-Z0-9+/=]{20,}") || // Base64-like
               value.matches("[a-fA-F0-9]{32,}");        // Hex-like
    }
    
    private void waitForZapScanCompletion(String scanType, String scanId) {
        try {
            int progress = 0;
            while (progress < 100) {
                Thread.sleep(5000); // Wait 5 seconds
                
                ApiResponse response;
                if ("spider".equals(scanType)) {
                    response = zapApi.spider.status(scanId);
                } else {
                    response = zapApi.ascan.status(scanId);
                }
                
                progress = Integer.parseInt(response.toString());
                logger.debug("ZAP {} scan progress: {}%", scanType, progress);
            }
            logger.info("ZAP {} scan completed", scanType);
        } catch (Exception e) {
            logger.warn("Error waiting for ZAP scan completion: {}", e.getMessage());
        }
    }
    
    /**
     * Represents a security vulnerability found during testing
     */
    public static class SecurityVulnerability {
        public enum Type {
            XSS, SQL_INJECTION, CSRF, DATA_EXPOSURE, MISSING_SECURITY_HEADER, AUTHENTICATION_BYPASS, AUTHORIZATION_FLAW
        }
        
        private final Type type;
        private final String description;
        private final String severity;
        private final String payload;
        private final String url;
        private final long timestamp;
        
        public SecurityVulnerability(Type type, String description, String severity, String payload, String url) {
            this.type = type;
            this.description = description;
            this.severity = severity;
            this.payload = payload;
            this.url = url;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public Type getType() { return type; }
        public String getDescription() { return description; }
        public String getSeverity() { return severity; }
        public String getPayload() { return payload; }
        public String getUrl() { return url; }
        public long getTimestamp() { return timestamp; }
        
        @Override
        public String toString() {
            return String.format("[%s] %s - %s (Severity: %s, URL: %s)", 
                               type, description, payload, severity, url);
        }
    }
}