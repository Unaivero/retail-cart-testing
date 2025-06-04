package com.retailer.cart.utils.exceptions;

/**
 * Exception thrown when API operations fail
 */
public class ApiException extends FrameworkException {
    
    private final String endpoint;
    private final int statusCode;
    private final String httpMethod;
    
    public ApiException(String message) {
        super("API_ERROR", "REST", message);
        this.endpoint = "UNKNOWN";
        this.statusCode = -1;
        this.httpMethod = "UNKNOWN";
    }
    
    public ApiException(String message, Throwable cause) {
        super("API_ERROR", "REST", message, cause);
        this.endpoint = "UNKNOWN";
        this.statusCode = -1;
        this.httpMethod = "UNKNOWN";
    }
    
    public ApiException(String endpoint, String httpMethod, int statusCode, String message) {
        super("API_ERROR", "REST", message);
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.statusCode = statusCode;
    }
    
    public ApiException(String endpoint, String httpMethod, int statusCode, String message, Throwable cause) {
        super("API_ERROR", "REST", message, cause);
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.statusCode = statusCode;
    }
    
    public String getEndpoint() {
        return endpoint;
    }
    
    public int getStatusCode() {
        return statusCode;
    }
    
    public String getHttpMethod() {
        return httpMethod;
    }
    
    @Override
    public String toString() {
        return String.format("[%s:%s] %s %s (Status: %d) - %s", 
            getErrorCode(), getComponent(), httpMethod, endpoint, statusCode, getMessage());
    }
}