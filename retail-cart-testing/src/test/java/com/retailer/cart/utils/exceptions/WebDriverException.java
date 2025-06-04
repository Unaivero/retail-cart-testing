package com.retailer.cart.utils.exceptions;

/**
 * Exception thrown when WebDriver operations fail
 */
public class WebDriverException extends FrameworkException {
    
    private final String browserType;
    private final String elementInfo;
    
    public WebDriverException(String message) {
        super("WEBDRIVER_ERROR", "SELENIUM", message);
        this.browserType = "UNKNOWN";
        this.elementInfo = "UNKNOWN";
    }
    
    public WebDriverException(String message, Throwable cause) {
        super("WEBDRIVER_ERROR", "SELENIUM", message, cause);
        this.browserType = "UNKNOWN";
        this.elementInfo = "UNKNOWN";
    }
    
    public WebDriverException(String browserType, String elementInfo, String message) {
        super("WEBDRIVER_ERROR", "SELENIUM", message);
        this.browserType = browserType;
        this.elementInfo = elementInfo;
    }
    
    public WebDriverException(String browserType, String elementInfo, String message, Throwable cause) {
        super("WEBDRIVER_ERROR", "SELENIUM", message, cause);
        this.browserType = browserType;
        this.elementInfo = elementInfo;
    }
    
    public String getBrowserType() {
        return browserType;
    }
    
    public String getElementInfo() {
        return elementInfo;
    }
    
    @Override
    public String toString() {
        return String.format("[%s:%s] Browser: %s, Element: %s - %s", 
            getErrorCode(), getComponent(), browserType, elementInfo, getMessage());
    }
}