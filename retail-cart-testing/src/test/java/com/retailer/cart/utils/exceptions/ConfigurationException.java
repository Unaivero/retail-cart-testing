package com.retailer.cart.utils.exceptions;

/**
 * Exception thrown when configuration-related errors occur
 */
public class ConfigurationException extends FrameworkException {
    
    private final String configKey;
    private final String configFile;
    
    public ConfigurationException(String message) {
        super("CONFIG_ERROR", "CONFIGURATION", message);
        this.configKey = "UNKNOWN";
        this.configFile = "UNKNOWN";
    }
    
    public ConfigurationException(String message, Throwable cause) {
        super("CONFIG_ERROR", "CONFIGURATION", message, cause);
        this.configKey = "UNKNOWN";
        this.configFile = "UNKNOWN";
    }
    
    public ConfigurationException(String configKey, String configFile, String message) {
        super("CONFIG_ERROR", "CONFIGURATION", message);
        this.configKey = configKey;
        this.configFile = configFile;
    }
    
    public ConfigurationException(String configKey, String configFile, String message, Throwable cause) {
        super("CONFIG_ERROR", "CONFIGURATION", message, cause);
        this.configKey = configKey;
        this.configFile = configFile;
    }
    
    public String getConfigKey() {
        return configKey;
    }
    
    public String getConfigFile() {
        return configFile;
    }
    
    @Override
    public String toString() {
        return String.format("[%s:%s] Key: %s, File: %s - %s", 
            getErrorCode(), getComponent(), configKey, configFile, getMessage());
    }
}