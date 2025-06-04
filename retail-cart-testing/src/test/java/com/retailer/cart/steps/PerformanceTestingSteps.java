package com.retailer.cart.steps;

import com.retailer.cart.utils.PerformanceMonitor;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class PerformanceTestingSteps {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceTestingSteps.class);
    
    private PerformanceMonitor performanceMonitor;
    private Response lastResponse;
    private long operationStartTime;
    private String currentOperation;
    
    // Performance thresholds
    private double maxAverageResponseTime = 2000.0; // 2 seconds default
    private double maxErrorRate = 5.0; // 5% default
    private double maxP95ResponseTime = 5000.0; // 5 seconds default
    
    @Before("@performance")
    public void setupPerformanceMonitoring() {
        String testName = "performance_test";
        performanceMonitor = new PerformanceMonitor(testName);
        logger.info("Performance monitoring initialized for: {}", testName);
    }
    
    @After("@performance")
    public void tearDownPerformanceMonitoring() {
        if (performanceMonitor != null) {
            performanceMonitor.printSummary();
            performanceMonitor.saveReportToFile();
            performanceMonitor.exportToJTLFormat("performance_results.jtl");
        }
        logger.info("Performance monitoring completed");
    }
    
    @Given("performance monitoring is enabled")
    public void performanceMonitoringIsEnabled() {
        assertThat(performanceMonitor).as("Performance monitor should be initialized").isNotNull();
        logger.info("Performance monitoring is active");
    }
    
    @Given("the maximum average response time is {double} milliseconds")
    public void theMaximumAverageResponseTimeIsMilliseconds(double maxResponseTime) {
        this.maxAverageResponseTime = maxResponseTime;
        logger.info("Set maximum average response time threshold to: {}ms", maxResponseTime);
    }
    
    @Given("the maximum error rate is {double} percent")
    public void theMaximumErrorRateIsPercent(double maxErrorRate) {
        this.maxErrorRate = maxErrorRate;
        logger.info("Set maximum error rate threshold to: {}%", maxErrorRate);
    }
    
    @Given("the maximum 95th percentile response time is {double} milliseconds")
    public void theMaximum95thPercentileResponseTimeIsMilliseconds(double maxP95ResponseTime) {
        this.maxP95ResponseTime = maxP95ResponseTime;
        logger.info("Set maximum 95th percentile response time threshold to: {}ms", maxP95ResponseTime);
    }
    
    @When("I start monitoring the {string} operation")
    public void iStartMonitoringTheOperation(String operationName) {
        this.currentOperation = operationName;
        this.operationStartTime = System.currentTimeMillis();
        logger.info("Started monitoring operation: {}", operationName);
    }
    
    @When("I perform {int} {string} requests to {string}")
    public void iPerformRequestsTo(int requestCount, String httpMethod, String endpoint) {
        logger.info("Performing {} {} requests to {}", requestCount, httpMethod, endpoint);
        
        for (int i = 0; i < requestCount; i++) {
            long startTime = System.currentTimeMillis();
            
            try {
                Response response;
                switch (httpMethod.toUpperCase()) {
                    case "GET":
                        response = RestAssured.get(endpoint);
                        break;
                    case "POST":
                        response = RestAssured.post(endpoint);
                        break;
                    case "PUT":
                        response = RestAssured.put(endpoint);
                        break;
                    case "DELETE":
                        response = RestAssured.delete(endpoint);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
                }
                
                long responseTime = System.currentTimeMillis() - startTime;
                performanceMonitor.recordApiCall(endpoint, httpMethod, response.getStatusCode(), responseTime);
                
                this.lastResponse = response;
                
                // Small delay between requests to avoid overwhelming the server
                if (i < requestCount - 1) {
                    Thread.sleep(100);
                }
                
            } catch (Exception e) {
                long responseTime = System.currentTimeMillis() - startTime;
                performanceMonitor.recordApiCall(endpoint, httpMethod, 0, responseTime);
                logger.warn("Request failed: {}", e.getMessage());
            }
        }
        
        logger.info("Completed {} {} requests to {}", requestCount, httpMethod, endpoint);
    }
    
    @When("I perform {int} concurrent {string} requests to {string}")
    public void iPerformConcurrentRequestsTo(int requestCount, String httpMethod, String endpoint) {
        logger.info("Performing {} concurrent {} requests to {}", requestCount, httpMethod, endpoint);
        
        Thread[] threads = new Thread[requestCount];
        
        for (int i = 0; i < requestCount; i++) {
            final int requestIndex = i;
            threads[i] = new Thread(() -> {
                long startTime = System.currentTimeMillis();
                
                try {
                    Response response;
                    switch (httpMethod.toUpperCase()) {
                        case "GET":
                            response = RestAssured.get(endpoint);
                            break;
                        case "POST":
                            response = RestAssured.post(endpoint);
                            break;
                        case "PUT":
                            response = RestAssured.put(endpoint);
                            break;
                        case "DELETE":
                            response = RestAssured.delete(endpoint);
                            break;
                        default:
                            throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
                    }
                    
                    long responseTime = System.currentTimeMillis() - startTime;
                    performanceMonitor.recordApiCall(endpoint + "_concurrent", httpMethod, response.getStatusCode(), responseTime);
                    
                    logger.debug("Concurrent request {} completed in {}ms", requestIndex, responseTime);
                    
                } catch (Exception e) {
                    long responseTime = System.currentTimeMillis() - startTime;
                    performanceMonitor.recordApiCall(endpoint + "_concurrent", httpMethod, 0, responseTime);
                    logger.warn("Concurrent request {} failed: {}", requestIndex, e.getMessage());
                }
            });
        }
        
        // Start all threads
        for (Thread thread : threads) {
            thread.start();
        }
        
        // Wait for all threads to complete
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.warn("Thread interrupted while waiting for concurrent requests to complete");
            }
        }
        
        logger.info("Completed {} concurrent {} requests to {}", requestCount, httpMethod, endpoint);
    }
    
    @When("I stop monitoring the operation")
    public void iStopMonitoringTheOperation() {
        if (currentOperation != null && operationStartTime > 0) {
            long operationTime = System.currentTimeMillis() - operationStartTime;
            performanceMonitor.recordRequest(currentOperation, operationTime, false);
            logger.info("Stopped monitoring operation: {} (took {}ms)", currentOperation, operationTime);
        }
    }
    
    @Then("the average response time for {string} should be less than {double} milliseconds")
    public void theAverageResponseTimeForShouldBeLessThanMilliseconds(String operationName, double maxTime) {
        double actualAvgTime = performanceMonitor.getAverageResponseTime(operationName);
        assertThat(actualAvgTime)
                .as("Average response time for '%s' should be less than %sms but was %sms", 
                    operationName, maxTime, actualAvgTime)
                .isLessThan(maxTime);
        
        logger.info("Average response time check passed for {}: {}ms < {}ms", 
                   operationName, actualAvgTime, maxTime);
    }
    
    @Then("the error rate for {string} should be less than {double} percent")
    public void theErrorRateForShouldBeLessThanPercent(String operationName, double maxErrorRate) {
        double actualErrorRate = performanceMonitor.getErrorRate(operationName);
        assertThat(actualErrorRate)
                .as("Error rate for '%s' should be less than %s%% but was %s%%", 
                    operationName, maxErrorRate, actualErrorRate)
                .isLessThan(maxErrorRate);
        
        logger.info("Error rate check passed for {}: {}% < {}%", 
                   operationName, actualErrorRate, maxErrorRate);
    }
    
    @Then("the 95th percentile response time for {string} should be less than {double} milliseconds")
    public void the95thPercentileResponseTimeForShouldBeLessThanMilliseconds(String operationName, double maxP95Time) {
        long actualP95Time = performanceMonitor.getPercentileResponseTime(operationName, 95.0);
        assertThat(actualP95Time)
                .as("95th percentile response time for '%s' should be less than %sms but was %sms", 
                    operationName, maxP95Time, actualP95Time)
                .isLessThan((long) maxP95Time);
        
        logger.info("95th percentile response time check passed for {}: {}ms < {}ms", 
                   operationName, actualP95Time, maxP95Time);
    }
    
    @Then("the throughput for {string} should be at least {double} requests per second")
    public void theThroughputForShouldBeAtLeastRequestsPerSecond(String operationName, double minThroughput) {
        double actualThroughput = performanceMonitor.getThroughput(operationName);
        assertThat(actualThroughput)
                .as("Throughput for '%s' should be at least %s req/s but was %s req/s", 
                    operationName, minThroughput, actualThroughput)
                .isGreaterThanOrEqualTo(minThroughput);
        
        logger.info("Throughput check passed for {}: {} req/s >= {} req/s", 
                   operationName, actualThroughput, minThroughput);
    }
    
    @Then("all performance thresholds should be met")
    public void allPerformanceThresholdsShouldBeMet() {
        boolean thresholdsMet = performanceMonitor.checkThresholds(maxAverageResponseTime, maxErrorRate);
        assertThat(thresholdsMet)
                .as("All performance thresholds should be met (avg response time < %sms, error rate < %s%%)", 
                    maxAverageResponseTime, maxErrorRate)
                .isTrue();
        
        logger.info("All performance thresholds check passed");
    }
    
    @Then("the performance report should be generated")
    public void thePerformanceReportShouldBeGenerated() {
        assertThat(performanceMonitor).as("Performance monitor should be active").isNotNull();
        
        // Generate and validate report
        var report = performanceMonitor.generateReport();
        assertThat(report).as("Performance report should not be empty").isNotEmpty();
        assertThat(report.get("sessionId")).as("Report should contain session ID").isNotNull();
        assertThat(report.get("operations")).as("Report should contain operations data").isNotNull();
        
        logger.info("Performance report validation passed");
    }
    
    @When("I wait {int} seconds")
    public void iWaitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
            logger.info("Waited {} seconds", seconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Wait interrupted");
        }
    }
}