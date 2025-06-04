package com.retailer.cart.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigReader {

    private static final Properties properties = new Properties();
    private static final String DEFAULT_CONFIG_FILE = "config.properties";
    private static final String CONFIG_DIR = "config/";

    static {
        loadProperties();
    }

    private static void loadProperties() {
        // Get environment from system property or default to 'dev'
        String environment = System.getProperty("environment", "dev");
        String configFileName = CONFIG_DIR + environment + ".properties";
        
        try {
            // Try to load environment-specific config first
            InputStream inputStream = ConfigReader.class.getClassLoader().getResourceAsStream(configFileName);
            
            if (inputStream == null) {
                // Fallback to default config.properties
                inputStream = ConfigReader.class.getClassLoader().getResourceAsStream(DEFAULT_CONFIG_FILE);
                
                if (inputStream == null) {
                    throw new RuntimeException("Configuration file not found: " + configFileName + " or " + DEFAULT_CONFIG_FILE);
                }
            }
            
            properties.load(inputStream);
            inputStream.close();
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to load configuration properties", e);
        }
    }

    public static String getProperty(String key) {
        // System property overrides file property
        String value = System.getProperty(key, properties.getProperty(key));
        
        // Handle environment variable placeholders like ${ENV_VAR}
        if (value != null && value.startsWith("${") && value.endsWith("}")) {
            String envVarName = value.substring(2, value.length() - 1);
            String envValue = System.getenv(envVarName);
            return envValue != null ? envValue : value;
        }
        
        return value;
    }
    
    public static String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        return value != null ? value : defaultValue;
    }

    public static String getBaseUrl() {
        return getProperty("base.url");
    }
    
    public static String getApiBaseUrl() {
        return getProperty("api.base.url");
    }

    public static String getBrowser() {
        return getProperty("browser", "chrome");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("headless.mode", "false"));
    }

    public static int getImplicitWaitSeconds() {
        return Integer.parseInt(getProperty("implicit.wait.seconds", "10"));
    }

    public static int getPageLoadTimeoutSeconds() {
        return Integer.parseInt(getProperty("pageload.timeout.seconds", "30"));
    }

    public static int getExplicitWaitSeconds() {
        return Integer.parseInt(getProperty("explicit.wait.seconds", "15"));
    }
    
    public static String getEnvironmentName() {
        return getProperty("environment.name", "dev");
    }
    
    public static boolean isSeleniumGridEnabled() {
        return Boolean.parseBoolean(getProperty("selenium.grid.enabled", "false"));
    }
    
    public static String getSeleniumGridUrl() {
        return getProperty("selenium.grid.url", "http://localhost:4444/wd/hub");
    }
    
    public static boolean isDebugMode() {
        return Boolean.parseBoolean(getProperty("debug.mode", "false"));
    }
    
    public static boolean isScreenshotOnFailure() {
        return Boolean.parseBoolean(getProperty("screenshot.on.failure", "true"));
    }
    
    public static String getScreenshotPath() {
        return getProperty("screenshot.path", "target/screenshots");
    }
    
    public static int getApiTimeoutSeconds() {
        return Integer.parseInt(getProperty("api.timeout.seconds", "30"));
    }
}
