package com.retailer.cart.utils;

import com.retailer.cart.utils.exceptions.WebDriverException;
import com.retailer.cart.utils.exceptions.ConfigurationException;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public class DriverManager {
    private static final Logger logger = LoggerFactory.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> driverThreadLocal = new ThreadLocal<>();
    private static final ThreadLocal<String> browserThreadLocal = new ThreadLocal<>();
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final int RETRY_DELAY_MS = 1000;

    private DriverManager() {
        // Private constructor to prevent instantiation
    }

    public static WebDriver getDriver() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            throw new WebDriverException("No WebDriver instance found in current thread. Call setDriver() first.");
        }
        return driver;
    }
    
    public static String getCurrentBrowser() {
        return browserThreadLocal.get();
    }

    public static void setDriver(String browserName) {
        setDriver(browserName, null);
    }
    
    public static void setDriver(String browserName, Map<String, Object> capabilities) {
        if (browserName == null || browserName.trim().isEmpty()) {
            throw new ConfigurationException("browserName", "driver setup", "Browser name cannot be null or empty");
        }
        
        WebDriver driver = null;
        Exception lastException = null;
        
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                logger.info("Attempting to create {} driver (attempt {}/{})", browserName, attempt, MAX_RETRY_ATTEMPTS);
                
                if (ConfigReader.isSeleniumGridEnabled()) {
                    driver = createRemoteDriver(browserName, capabilities);
                } else {
                    driver = createLocalDriver(browserName, capabilities);
                }
                
                configureDriver(driver);
                driverThreadLocal.set(driver);
                browserThreadLocal.set(browserName.toLowerCase());
                
                logger.info("Successfully created {} driver on attempt {}", browserName, attempt);
                return;
                
            } catch (Exception e) {
                lastException = e;
                logger.warn("Failed to create {} driver on attempt {}: {}", browserName, attempt, e.getMessage());
                
                if (driver != null) {
                    try {
                        driver.quit();
                    } catch (Exception quitException) {
                        logger.warn("Failed to quit driver after creation failure", quitException);
                    }
                }
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new WebDriverException("Interrupted while retrying driver creation", ie);
                    }
                }
            }
        }
        
        throw new WebDriverException(browserName, "driver creation", 
            "Failed to create driver after " + MAX_RETRY_ATTEMPTS + " attempts", lastException);
    }
    
    private static WebDriver createLocalDriver(String browserName, Map<String, Object> capabilities) {
        switch (browserName.toLowerCase()) {
            case "chrome":
                return createChromeDriver(capabilities);
            case "firefox":
                return createFirefoxDriver(capabilities);
            case "edge":
                return createEdgeDriver(capabilities);
            case "safari":
                return createSafariDriver(capabilities);
            default:
                throw new ConfigurationException("browserName", "driver setup", 
                    "Unsupported browser: " + browserName + ". Supported browsers: chrome, firefox, edge, safari");
        }
    }
    
    private static WebDriver createRemoteDriver(String browserName, Map<String, Object> capabilities) {
        try {
            String gridUrl = ConfigReader.getSeleniumGridUrl();
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            
            // Set browser-specific capabilities
            switch (browserName.toLowerCase()) {
                case "chrome":
                    desiredCapabilities.setBrowserName("chrome");
                    ChromeOptions chromeOptions = createChromeOptions(capabilities);
                    desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
                    break;
                case "firefox":
                    desiredCapabilities.setBrowserName("firefox");
                    FirefoxOptions firefoxOptions = createFirefoxOptions(capabilities);
                    desiredCapabilities.setCapability(FirefoxOptions.FIREFOX_OPTIONS, firefoxOptions);
                    break;
                case "edge":
                    desiredCapabilities.setBrowserName("MicrosoftEdge");
                    EdgeOptions edgeOptions = createEdgeOptions(capabilities);
                    desiredCapabilities.setCapability(EdgeOptions.CAPABILITY, edgeOptions);
                    break;
                default:
                    throw new ConfigurationException("browserName", "remote driver", 
                        "Browser " + browserName + " not supported for remote execution");
            }
            
            // Add custom capabilities if provided
            if (capabilities != null) {
                capabilities.forEach(desiredCapabilities::setCapability);
            }
            
            logger.info("Connecting to Selenium Grid at: {}", gridUrl);
            return new RemoteWebDriver(new URL(gridUrl), desiredCapabilities);
            
        } catch (Exception e) {
            throw new WebDriverException(browserName, "remote driver creation", 
                "Failed to create remote driver", e);
        }
    }
    
    private static WebDriver createChromeDriver(Map<String, Object> capabilities) {
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = createChromeOptions(capabilities);
            return new ChromeDriver(options);
        } catch (Exception e) {
            throw new WebDriverException("chrome", "local driver creation", 
                "Failed to create Chrome driver", e);
        }
    }
    
    private static ChromeOptions createChromeOptions(Map<String, Object> capabilities) {
        ChromeOptions options = new ChromeOptions();
        
        // Basic options
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--disable-extensions");
        options.addArguments("--disable-plugins");
        options.addArguments("--disable-popup-blocking");
        options.addArguments("--window-size=1920,1080");
        
        // Headless mode
        if (ConfigReader.isHeadless()) {
            options.addArguments("--headless=new");
            logger.debug("Chrome running in headless mode");
        }
        
        // Performance optimizations
        options.addArguments("--memory-pressure-off");
        options.addArguments("--max_old_space_size=4096");
        
        // Security options for testing
        options.addArguments("--disable-web-security");
        options.addArguments("--allow-running-insecure-content");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--ignore-ssl-errors");
        
        // Set preferences
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("profile.default_content_setting_values.notifications", 2);
        prefs.put("profile.default_content_settings.popups", 0);
        options.setExperimentalOption("prefs", prefs);
        
        // Add custom capabilities
        if (capabilities != null) {
            capabilities.forEach((key, value) -> {
                if (value instanceof String && ((String) value).startsWith("--")) {
                    options.addArguments((String) value);
                } else {
                    options.setCapability(key, value);
                }
            });
        }
        
        return options;
    }
    
    private static WebDriver createFirefoxDriver(Map<String, Object> capabilities) {
        try {
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions options = createFirefoxOptions(capabilities);
            return new FirefoxDriver(options);
        } catch (Exception e) {
            throw new WebDriverException("firefox", "local driver creation", 
                "Failed to create Firefox driver", e);
        }
    }
    
    private static FirefoxOptions createFirefoxOptions(Map<String, Object> capabilities) {
        FirefoxOptions options = new FirefoxOptions();
        
        // Headless mode
        if (ConfigReader.isHeadless()) {
            options.addArguments("--headless");
            logger.debug("Firefox running in headless mode");
        }
        
        // Performance optimizations
        options.addPreference("dom.webnotifications.enabled", false);
        options.addPreference("media.volume_scale", "0.0");
        options.addPreference("browser.tabs.remote.autostart", false);
        options.addPreference("security.tls.insecure_fallback_hosts", "localhost");
        
        // Add custom capabilities
        if (capabilities != null) {
            capabilities.forEach(options::setCapability);
        }
        
        return options;
    }
    
    private static WebDriver createEdgeDriver(Map<String, Object> capabilities) {
        try {
            WebDriverManager.edgedriver().setup();
            EdgeOptions options = createEdgeOptions(capabilities);
            return new EdgeDriver(options);
        } catch (Exception e) {
            throw new WebDriverException("edge", "local driver creation", 
                "Failed to create Edge driver", e);
        }
    }
    
    private static EdgeOptions createEdgeOptions(Map<String, Object> capabilities) {
        EdgeOptions options = new EdgeOptions();
        
        // Basic options
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");
        
        // Headless mode
        if (ConfigReader.isHeadless()) {
            options.addArguments("--headless");
            logger.debug("Edge running in headless mode");
        }
        
        // Add custom capabilities
        if (capabilities != null) {
            capabilities.forEach(options::setCapability);
        }
        
        return options;
    }
    
    private static WebDriver createSafariDriver(Map<String, Object> capabilities) {
        try {
            SafariOptions options = new SafariOptions();
            
            // Note: Safari doesn't support headless mode
            if (ConfigReader.isHeadless()) {
                logger.warn("Safari does not support headless mode, running in normal mode");
            }
            
            // Add custom capabilities
            if (capabilities != null) {
                capabilities.forEach(options::setCapability);
            }
            
            return new SafariDriver(options);
        } catch (Exception e) {
            throw new WebDriverException("safari", "local driver creation", 
                "Failed to create Safari driver", e);
        }
    }
    
    private static void configureDriver(WebDriver driver) {
        try {
            // Set timeouts
            driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(ConfigReader.getImplicitWaitSeconds()));
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(ConfigReader.getPageLoadTimeoutSeconds()));
            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
            
            // Maximize window (unless headless)
            if (!ConfigReader.isHeadless()) {
                try {
                    driver.manage().window().maximize();
                } catch (Exception e) {
                    logger.warn("Failed to maximize window: {}", e.getMessage());
                    // Set a specific window size as fallback
                    driver.manage().window().setSize(new org.openqa.selenium.Dimension(1920, 1080));
                }
            }
            
            logger.debug("Driver configured successfully");
            
        } catch (Exception e) {
            logger.warn("Failed to configure driver: {}", e.getMessage());
            // Don't throw exception here as driver creation was successful
        }
    }

    public static void quitDriver() {
        WebDriver driver = driverThreadLocal.get();
        String browser = browserThreadLocal.get();
        
        if (driver != null) {
            try {
                logger.info("Quitting {} driver", browser != null ? browser : "unknown");
                driver.quit();
                logger.debug("Driver quit successfully");
            } catch (Exception e) {
                logger.warn("Error occurred while quitting driver: {}", e.getMessage());
            } finally {
                driverThreadLocal.remove();
                browserThreadLocal.remove();
            }
        } else {
            logger.debug("No driver to quit in current thread");
        }
    }
    
    public static boolean isDriverAlive() {
        WebDriver driver = driverThreadLocal.get();
        if (driver == null) {
            return false;
        }
        
        try {
            driver.getCurrentUrl();
            return true;
        } catch (Exception e) {
            logger.debug("Driver is not alive: {}", e.getMessage());
            return false;
        }
    }
    
    // Legacy method for backward compatibility
    public static void initDriver() {
        String browser = ConfigReader.getBrowser();
        setDriver(browser);
    }
}
