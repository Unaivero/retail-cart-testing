package com.retailer.cart.utils.exceptions;

/**
 * Exception thrown when test data operations fail
 */
public class TestDataException extends FrameworkException {
    
    private final String dataSource;
    private final String dataKey;
    
    public TestDataException(String message) {
        super("TESTDATA_ERROR", "DATA", message);
        this.dataSource = "UNKNOWN";
        this.dataKey = "UNKNOWN";
    }
    
    public TestDataException(String message, Throwable cause) {
        super("TESTDATA_ERROR", "DATA", message, cause);
        this.dataSource = "UNKNOWN";
        this.dataKey = "UNKNOWN";
    }
    
    public TestDataException(String dataSource, String dataKey, String message) {
        super("TESTDATA_ERROR", "DATA", message);
        this.dataSource = dataSource;
        this.dataKey = dataKey;
    }
    
    public TestDataException(String dataSource, String dataKey, String message, Throwable cause) {
        super("TESTDATA_ERROR", "DATA", message, cause);
        this.dataSource = dataSource;
        this.dataKey = dataKey;
    }
    
    public String getDataSource() {
        return dataSource;
    }
    
    public String getDataKey() {
        return dataKey;
    }
    
    @Override
    public String toString() {
        return String.format("[%s:%s] Source: %s, Key: %s - %s", 
            getErrorCode(), getComponent(), dataSource, dataKey, getMessage());
    }
}