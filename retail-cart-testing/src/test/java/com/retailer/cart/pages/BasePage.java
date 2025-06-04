package com.retailer.cart.pages;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.interactions.Actions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import com.retailer.cart.utils.ConfigReader;

public class BasePage {
    protected static final Logger logger = LoggerFactory.getLogger(BasePage.class);
    protected WebDriver driver;
    protected WebDriverWait wait;
    protected WebDriverWait shortWait;
    protected Actions actions;
    protected JavascriptExecutor jsExecutor;
    
    private static final int DEFAULT_RETRY_COUNT = 3;
    private static final int SHORT_WAIT_SECONDS = 2;
    
    public BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(ConfigReader.getExplicitWaitSeconds()));
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(SHORT_WAIT_SECONDS));
        this.actions = new Actions(driver);
        this.jsExecutor = (JavascriptExecutor) driver;
        PageFactory.initElements(driver, this);
        logger.debug("Initialized {} page object", this.getClass().getSimpleName());
    }
    
    // Enhanced wait methods with better error handling
    protected void waitForElementToBeClickable(WebElement element) {
        waitForElementToBeClickable(element, ConfigReader.getExplicitWaitSeconds());
    }
    
    protected void waitForElementToBeClickable(WebElement element, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.elementToBeClickable(element));
            logger.debug("Element is clickable: {}", getElementInfo(element));
        } catch (TimeoutException e) {
            logger.error("Element not clickable within {} seconds: {}", timeoutSeconds, getElementInfo(element));
            throw new RuntimeException("Element not clickable: " + getElementInfo(element), e);
        }
    }
    
    protected void waitForElementToBeVisible(WebElement element) {
        waitForElementToBeVisible(element, ConfigReader.getExplicitWaitSeconds());
    }
    
    protected void waitForElementToBeVisible(WebElement element, int timeoutSeconds) {
        try {
            WebDriverWait customWait = new WebDriverWait(driver, Duration.ofSeconds(timeoutSeconds));
            customWait.until(ExpectedConditions.visibilityOf(element));
            logger.debug("Element is visible: {}", getElementInfo(element));
        } catch (TimeoutException e) {
            logger.error("Element not visible within {} seconds: {}", timeoutSeconds, getElementInfo(element));
            throw new RuntimeException("Element not visible: " + getElementInfo(element), e);
        }
    }
    
    protected void waitForElementToBeInvisible(By locator) {
        try {
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            logger.debug("Element is invisible: {}", locator);
        } catch (TimeoutException e) {
            logger.warn("Element still visible after timeout: {}", locator);
        }
    }
    
    protected void waitForPageToLoad() {
        try {
            wait.until(webDriver -> jsExecutor.executeScript("return document.readyState").equals("complete"));
            logger.debug("Page load completed");
        } catch (TimeoutException e) {
            logger.warn("Page load timeout");
        }
    }
    
    // Enhanced interaction methods with retry logic
    protected void clickElement(WebElement element) {
        retryAction(() -> {
            scrollToElementIfNeeded(element);
            waitForElementToBeClickable(element);
            if (ConfigReader.isDebugMode()) {
                highlightElement(element);
            }
            element.click();
            logger.debug("Clicked element: {}", getElementInfo(element));
            return true;
        }, "click element: " + getElementInfo(element));
    }
    
    protected void clickElementWithJS(WebElement element) {
        try {
            scrollToElementIfNeeded(element);
            jsExecutor.executeScript("arguments[0].click();", element);
            logger.debug("Clicked element with JavaScript: {}", getElementInfo(element));
        } catch (Exception e) {
            logger.error("Failed to click element with JavaScript: {}", getElementInfo(element), e);
            throw new RuntimeException("JavaScript click failed", e);
        }
    }
    
    protected void doubleClickElement(WebElement element) {
        try {
            scrollToElementIfNeeded(element);
            waitForElementToBeClickable(element);
            actions.doubleClick(element).perform();
            logger.debug("Double-clicked element: {}", getElementInfo(element));
        } catch (Exception e) {
            logger.error("Failed to double-click element: {}", getElementInfo(element), e);
            throw new RuntimeException("Double-click failed", e);
        }
    }
    
    protected void rightClickElement(WebElement element) {
        try {
            scrollToElementIfNeeded(element);
            waitForElementToBeClickable(element);
            actions.contextClick(element).perform();
            logger.debug("Right-clicked element: {}", getElementInfo(element));
        } catch (Exception e) {
            logger.error("Failed to right-click element: {}", getElementInfo(element), e);
            throw new RuntimeException("Right-click failed", e);
        }
    }
    
    protected void typeIntoField(WebElement element, String text) {
        retryAction(() -> {
            scrollToElementIfNeeded(element);
            waitForElementToBeVisible(element);
            waitForElementToBeClickable(element);
            element.clear();
            element.sendKeys(text);
            logger.debug("Typed '{}' into element: {}", text, getElementInfo(element));
            return true;
        }, "type into field: " + getElementInfo(element));
    }
    
    protected void typeSlowly(WebElement element, String text, int delayMillis) {
        scrollToElementIfNeeded(element);
        waitForElementToBeClickable(element);
        element.clear();
        
        for (char c : text.toCharArray()) {
            element.sendKeys(String.valueOf(c));
            try {
                Thread.sleep(delayMillis);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Interrupted while typing slowly", e);
            }
        }
        logger.debug("Typed slowly '{}' into element: {}", text, getElementInfo(element));
    }
    
    protected void clearField(WebElement element) {
        try {
            scrollToElementIfNeeded(element);
            waitForElementToBeClickable(element);
            element.clear();
            // Alternative clearing methods for stubborn fields
            element.sendKeys(Keys.CONTROL + "a");
            element.sendKeys(Keys.DELETE);
            logger.debug("Cleared field: {}", getElementInfo(element));
        } catch (Exception e) {
            logger.error("Failed to clear field: {}", getElementInfo(element), e);
            throw new RuntimeException("Field clearing failed", e);
        }
    }
    
    protected String getElementText(WebElement element) {
        try {
            waitForElementToBeVisible(element);
            String text = element.getText();
            if (text.isEmpty()) {
                text = element.getAttribute("value");
            }
            if (text.isEmpty()) {
                text = element.getAttribute("innerText");
            }
            logger.debug("Retrieved text '{}' from element: {}", text, getElementInfo(element));
            return text;
        } catch (Exception e) {
            logger.error("Failed to get text from element: {}", getElementInfo(element), e);
            throw new RuntimeException("Text retrieval failed", e);
        }
    }
    
    protected String getElementAttribute(WebElement element, String attributeName) {
        try {
            waitForElementToBeVisible(element);
            String value = element.getAttribute(attributeName);
            logger.debug("Retrieved attribute '{}' = '{}' from element: {}", attributeName, value, getElementInfo(element));
            return value;
        } catch (Exception e) {
            logger.error("Failed to get attribute '{}' from element: {}", attributeName, getElementInfo(element), e);
            throw new RuntimeException("Attribute retrieval failed", e);
        }
    }
    
    protected boolean isElementPresent(WebElement element) {
        try {
            shortWait.until(ExpectedConditions.visibilityOf(element));
            boolean isDisplayed = element.isDisplayed();
            logger.debug("Element presence check: {} - {}", isDisplayed, getElementInfo(element));
            return isDisplayed;
        } catch (NoSuchElementException | TimeoutException | StaleElementReferenceException e) {
            logger.debug("Element not present: {}", getElementInfo(element));
            return false;
        }
    }
    
    protected boolean isElementClickable(WebElement element) {
        try {
            shortWait.until(ExpectedConditions.elementToBeClickable(element));
            logger.debug("Element is clickable: {}", getElementInfo(element));
            return true;
        } catch (TimeoutException | NoSuchElementException e) {
            logger.debug("Element not clickable: {}", getElementInfo(element));
            return false;
        }
    }
    
    protected boolean isElementEnabled(WebElement element) {
        try {
            boolean enabled = element.isEnabled();
            logger.debug("Element enabled check: {} - {}", enabled, getElementInfo(element));
            return enabled;
        } catch (Exception e) {
            logger.debug("Element enabled check failed: {}", getElementInfo(element));
            return false;
        }
    }
    
    protected boolean isElementSelected(WebElement element) {
        try {
            boolean selected = element.isSelected();
            logger.debug("Element selected check: {} - {}", selected, getElementInfo(element));
            return selected;
        } catch (Exception e) {
            logger.debug("Element selected check failed: {}", getElementInfo(element));
            return false;
        }
    }
    
    // Enhanced dropdown methods
    protected void selectOptionByVisibleText(WebElement dropdownElement, String visibleText) {
        try {
            scrollToElementIfNeeded(dropdownElement);
            waitForElementToBeClickable(dropdownElement);
            Select dropdown = new Select(dropdownElement);
            dropdown.selectByVisibleText(visibleText);
            logger.debug("Selected option '{}' from dropdown: {}", visibleText, getElementInfo(dropdownElement));
        } catch (Exception e) {
            logger.error("Failed to select option '{}' from dropdown: {}", visibleText, getElementInfo(dropdownElement), e);
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }
    
    protected void selectOptionByValue(WebElement dropdownElement, String value) {
        try {
            scrollToElementIfNeeded(dropdownElement);
            waitForElementToBeClickable(dropdownElement);
            Select dropdown = new Select(dropdownElement);
            dropdown.selectByValue(value);
            logger.debug("Selected option value '{}' from dropdown: {}", value, getElementInfo(dropdownElement));
        } catch (Exception e) {
            logger.error("Failed to select option value '{}' from dropdown: {}", value, getElementInfo(dropdownElement), e);
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }
    
    protected void selectOptionByIndex(WebElement dropdownElement, int index) {
        try {
            scrollToElementIfNeeded(dropdownElement);
            waitForElementToBeClickable(dropdownElement);
            Select dropdown = new Select(dropdownElement);
            dropdown.selectByIndex(index);
            logger.debug("Selected option at index {} from dropdown: {}", index, getElementInfo(dropdownElement));
        } catch (Exception e) {
            logger.error("Failed to select option at index {} from dropdown: {}", index, getElementInfo(dropdownElement), e);
            throw new RuntimeException("Dropdown selection failed", e);
        }
    }
    
    protected List<WebElement> getDropdownOptions(WebElement dropdownElement) {
        try {
            Select dropdown = new Select(dropdownElement);
            List<WebElement> options = dropdown.getOptions();
            logger.debug("Retrieved {} options from dropdown: {}", options.size(), getElementInfo(dropdownElement));
            return options;
        } catch (Exception e) {
            logger.error("Failed to get dropdown options: {}", getElementInfo(dropdownElement), e);
            throw new RuntimeException("Dropdown options retrieval failed", e);
        }
    }
    
    // Utility methods for better debugging and error handling
    protected void highlightElement(WebElement element) {
        try {
            jsExecutor.executeScript("arguments[0].setAttribute('style', 'background: yellow; border: 2px solid red;');", element);
            Thread.sleep(500); // Brief pause to see highlight
            jsExecutor.executeScript("arguments[0].setAttribute('style', '');", element);
        } catch (Exception e) {
            logger.debug("Failed to highlight element: {}", getElementInfo(element));
        }
    }
    
    protected void scrollToElement(WebElement element) {
        try {
            jsExecutor.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", element);
            Thread.sleep(500); // Wait for smooth scroll
            logger.debug("Scrolled to element: {}", getElementInfo(element));
        } catch (Exception e) {
            logger.warn("Failed to scroll to element: {}", getElementInfo(element), e);
        }
    }
    
    protected void scrollToElementIfNeeded(WebElement element) {
        try {
            if (!isElementInViewport(element)) {
                scrollToElement(element);
            }
        } catch (Exception e) {
            logger.debug("Scroll check failed, continuing: {}", getElementInfo(element));
        }
    }
    
    protected boolean isElementInViewport(WebElement element) {
        try {
            return (Boolean) jsExecutor.executeScript(
                "var rect = arguments[0].getBoundingClientRect();" +
                "return (rect.top >= 0 && rect.left >= 0 && rect.bottom <= window.innerHeight && rect.right <= window.innerWidth);",
                element
            );
        } catch (Exception e) {
            return false;
        }
    }
    
    protected void takeScreenshot(String fileName) {
        try {
            // This would be implemented with screenshot utility
            logger.info("Screenshot taken: {}", fileName);
        } catch (Exception e) {
            logger.error("Failed to take screenshot: {}", fileName, e);
        }
    }
    
    // Retry mechanism for flaky operations
    protected <T> T retryAction(Function<Void, T> action, String actionDescription) {
        return retryAction(action, actionDescription, DEFAULT_RETRY_COUNT);
    }
    
    protected <T> T retryAction(Function<Void, T> action, String actionDescription, int maxRetries) {
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                logger.debug("Attempting {} (attempt {}/{})", actionDescription, attempt, maxRetries);
                return action.apply(null);
            } catch (StaleElementReferenceException | ElementNotInteractableException e) {
                lastException = e;
                logger.warn("Attempt {}/{} failed for {}: {}", attempt, maxRetries, actionDescription, e.getMessage());
                
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(500 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted during retry", ie);
                    }
                }
            }
        }
        
        logger.error("All {} attempts failed for {}", maxRetries, actionDescription);
        throw new RuntimeException("Action failed after " + maxRetries + " attempts: " + actionDescription, lastException);
    }
    
    // Helper method to get element information for logging
    private String getElementInfo(WebElement element) {
        try {
            String tagName = element.getTagName();
            String id = element.getAttribute("id");
            String className = element.getAttribute("class");
            String text = element.getText();
            
            StringBuilder info = new StringBuilder(tagName);
            if (id != null && !id.isEmpty()) {
                info.append("#").append(id);
            }
            if (className != null && !className.isEmpty()) {
                info.append(".").append(className.replace(" ", "."));
            }
            if (text != null && !text.isEmpty() && text.length() <= 20) {
                info.append(" ('").append(text).append("')");
            }
            
            return info.toString();
        } catch (Exception e) {
            return "unknown element";
        }
    }
    
    // Navigation utilities
    protected void navigateToUrl(String url) {
        try {
            driver.get(url);
            waitForPageToLoad();
            logger.info("Navigated to URL: {}", url);
        } catch (Exception e) {
            logger.error("Failed to navigate to URL: {}", url, e);
            throw new RuntimeException("Navigation failed", e);
        }
    }
    
    protected void refreshPage() {
        try {
            driver.navigate().refresh();
            waitForPageToLoad();
            logger.debug("Page refreshed");
        } catch (Exception e) {
            logger.error("Failed to refresh page", e);
            throw new RuntimeException("Page refresh failed", e);
        }
    }
    
    protected String getCurrentUrl() {
        try {
            String url = driver.getCurrentUrl();
            logger.debug("Current URL: {}", url);
            return url;
        } catch (Exception e) {
            logger.error("Failed to get current URL", e);
            throw new RuntimeException("URL retrieval failed", e);
        }
    }
    
    protected String getPageTitle() {
        try {
            String title = driver.getTitle();
            logger.debug("Page title: {}", title);
            return title;
        } catch (Exception e) {
            logger.error("Failed to get page title", e);
            throw new RuntimeException("Title retrieval failed", e);
        }
    }
}
