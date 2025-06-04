package com.retailer.cart.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class PerformanceMonitor {
    
    private static final Logger logger = LoggerFactory.getLogger(PerformanceMonitor.class);
    private static final String PERFORMANCE_REPORTS_DIR = "target/performance-reports";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    
    private final Map<String, AtomicLong> responseTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> requestCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> errorCounts = new ConcurrentHashMap<>();
    private final Map<String, List<Long>> responseTimeHistory = new ConcurrentHashMap<>();
    
    private final long testStartTime;
    private final String testSessionId;
    
    public PerformanceMonitor(String testName) {
        this.testStartTime = System.currentTimeMillis();
        this.testSessionId = testName + "_" + LocalDateTime.now().format(TIMESTAMP_FORMAT);
        createReportsDirectory();
        logger.info("Performance monitoring started for session: {}", testSessionId);
    }
    
    private void createReportsDirectory() {
        try {
            Files.createDirectories(Paths.get(PERFORMANCE_REPORTS_DIR));
        } catch (IOException e) {
            logger.error("Failed to create performance reports directory", e);
        }
    }
    
    /**
     * Records a request execution time
     * @param operationName the name of the operation
     * @param responseTime the response time in milliseconds
     * @param isError whether the request resulted in an error
     */
    public void recordRequest(String operationName, long responseTime, boolean isError) {
        responseTimes.computeIfAbsent(operationName, k -> new AtomicLong(0)).addAndGet(responseTime);
        requestCounts.computeIfAbsent(operationName, k -> new AtomicInteger(0)).incrementAndGet();
        
        if (isError) {
            errorCounts.computeIfAbsent(operationName, k -> new AtomicInteger(0)).incrementAndGet();
        }
        
        // Store individual response times for percentile calculations
        responseTimeHistory.computeIfAbsent(operationName, k -> Collections.synchronizedList(new ArrayList<>()))
                .add(responseTime);
        
        logger.debug("Recorded request - Operation: {}, Response Time: {}ms, Error: {}", 
                    operationName, responseTime, isError);
    }
    
    /**
     * Records API call performance
     * @param endpoint the API endpoint
     * @param httpMethod the HTTP method (GET, POST, etc.)
     * @param statusCode the HTTP status code
     * @param responseTime the response time in milliseconds
     */
    public void recordApiCall(String endpoint, String httpMethod, int statusCode, long responseTime) {
        String operationName = httpMethod + " " + endpoint;
        boolean isError = statusCode >= 400;
        recordRequest(operationName, responseTime, isError);
        
        // Also record by status code
        String statusOperation = operationName + " [" + statusCode + "]";
        recordRequest(statusOperation, responseTime, false);
    }
    
    /**
     * Records UI operation performance
     * @param action the UI action performed
     * @param elementLocator the element locator
     * @param executionTime the execution time in milliseconds
     * @param success whether the operation was successful
     */
    public void recordUIOperation(String action, String elementLocator, long executionTime, boolean success) {
        String operationName = "UI_" + action + " (" + elementLocator + ")";
        recordRequest(operationName, executionTime, !success);
    }
    
    /**
     * Gets the average response time for an operation
     * @param operationName the operation name
     * @return average response time in milliseconds
     */
    public double getAverageResponseTime(String operationName) {
        AtomicLong totalTime = responseTimes.get(operationName);
        AtomicInteger count = requestCounts.get(operationName);
        
        if (totalTime == null || count == null || count.get() == 0) {
            return 0.0;
        }
        
        return (double) totalTime.get() / count.get();
    }
    
    /**
     * Gets the error rate for an operation
     * @param operationName the operation name
     * @return error rate as a percentage (0.0 to 100.0)
     */
    public double getErrorRate(String operationName) {
        AtomicInteger errors = errorCounts.get(operationName);
        AtomicInteger total = requestCounts.get(operationName);
        
        if (errors == null || total == null || total.get() == 0) {
            return 0.0;
        }
        
        return (double) errors.get() / total.get() * 100.0;
    }
    
    /**
     * Gets the throughput for an operation (requests per second)
     * @param operationName the operation name
     * @return throughput in requests per second
     */
    public double getThroughput(String operationName) {
        AtomicInteger count = requestCounts.get(operationName);
        if (count == null || count.get() == 0) {
            return 0.0;
        }
        
        long elapsedSeconds = (System.currentTimeMillis() - testStartTime) / 1000;
        if (elapsedSeconds == 0) {
            return 0.0;
        }
        
        return (double) count.get() / elapsedSeconds;
    }
    
    /**
     * Gets the percentile response time for an operation
     * @param operationName the operation name
     * @param percentile the percentile (e.g., 95.0 for 95th percentile)
     * @return percentile response time in milliseconds
     */
    public long getPercentileResponseTime(String operationName, double percentile) {
        List<Long> times = responseTimeHistory.get(operationName);
        if (times == null || times.isEmpty()) {
            return 0;
        }
        
        List<Long> sortedTimes = new ArrayList<>(times);
        Collections.sort(sortedTimes);
        
        int index = (int) Math.ceil((percentile / 100.0) * sortedTimes.size()) - 1;
        index = Math.max(0, Math.min(index, sortedTimes.size() - 1));
        
        return sortedTimes.get(index);
    }
    
    /**
     * Generates a comprehensive performance report
     * @return performance report as a map
     */
    public Map<String, Object> generateReport() {
        Map<String, Object> report = new HashMap<>();
        report.put("sessionId", testSessionId);
        report.put("testStartTime", testStartTime);
        report.put("testDuration", System.currentTimeMillis() - testStartTime);
        report.put("timestamp", LocalDateTime.now().format(TIMESTAMP_FORMAT));
        
        Map<String, Map<String, Object>> operations = new HashMap<>();
        
        for (String operation : requestCounts.keySet()) {
            Map<String, Object> operationStats = new HashMap<>();
            operationStats.put("totalRequests", requestCounts.get(operation).get());
            operationStats.put("totalErrors", errorCounts.getOrDefault(operation, new AtomicInteger(0)).get());
            operationStats.put("averageResponseTime", getAverageResponseTime(operation));
            operationStats.put("errorRate", getErrorRate(operation));
            operationStats.put("throughput", getThroughput(operation));
            operationStats.put("p95ResponseTime", getPercentileResponseTime(operation, 95.0));
            operationStats.put("p99ResponseTime", getPercentileResponseTime(operation, 99.0));
            
            // Min and Max response times
            List<Long> times = responseTimeHistory.get(operation);
            if (times != null && !times.isEmpty()) {
                operationStats.put("minResponseTime", Collections.min(times));
                operationStats.put("maxResponseTime", Collections.max(times));
            }
            
            operations.put(operation, operationStats);
        }
        
        report.put("operations", operations);
        return report;
    }
    
    /**
     * Saves the performance report to a JSON file
     */
    public void saveReportToFile() {
        try {
            Map<String, Object> report = generateReport();
            String fileName = PERFORMANCE_REPORTS_DIR + "/performance_report_" + testSessionId + ".json";
            
            // Convert to JSON manually (simple implementation)
            StringBuilder json = new StringBuilder();
            json.append("{\n");
            
            // Basic report info
            json.append("  \"sessionId\": \"").append(report.get("sessionId")).append("\",\n");
            json.append("  \"testStartTime\": ").append(report.get("testStartTime")).append(",\n");
            json.append("  \"testDuration\": ").append(report.get("testDuration")).append(",\n");
            json.append("  \"timestamp\": \"").append(report.get("timestamp")).append("\",\n");
            
            // Operations
            json.append("  \"operations\": {\n");
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> operations = (Map<String, Map<String, Object>>) report.get("operations");
            
            int operationIndex = 0;
            for (Map.Entry<String, Map<String, Object>> entry : operations.entrySet()) {
                if (operationIndex > 0) json.append(",\n");
                
                json.append("    \"").append(entry.getKey()).append("\": {\n");
                Map<String, Object> stats = entry.getValue();
                
                int statIndex = 0;
                for (Map.Entry<String, Object> statEntry : stats.entrySet()) {
                    if (statIndex > 0) json.append(",\n");
                    
                    Object value = statEntry.getValue();
                    if (value instanceof String) {
                        json.append("      \"").append(statEntry.getKey()).append("\": \"").append(value).append("\"");
                    } else {
                        json.append("      \"").append(statEntry.getKey()).append("\": ").append(value);
                    }
                    statIndex++;
                }
                
                json.append("\n    }");
                operationIndex++;
            }
            
            json.append("\n  }\n");
            json.append("}");
            
            Files.write(Paths.get(fileName), json.toString().getBytes());
            logger.info("Performance report saved to: {}", fileName);
            
        } catch (IOException e) {
            logger.error("Failed to save performance report", e);
        }
    }
    
    /**
     * Prints a summary of performance metrics to the console
     */
    public void printSummary() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("PERFORMANCE TEST SUMMARY");
        System.out.println("=".repeat(60));
        System.out.println("Session ID: " + testSessionId);
        System.out.println("Test Duration: " + (System.currentTimeMillis() - testStartTime) + "ms");
        System.out.println();
        
        for (String operation : requestCounts.keySet()) {
            System.out.println("Operation: " + operation);
            System.out.println("  Total Requests: " + requestCounts.get(operation).get());
            System.out.println("  Total Errors: " + errorCounts.getOrDefault(operation, new AtomicInteger(0)).get());
            System.out.printf("  Average Response Time: %.2fms%n", getAverageResponseTime(operation));
            System.out.printf("  Error Rate: %.2f%%%n", getErrorRate(operation));
            System.out.printf("  Throughput: %.2f req/s%n", getThroughput(operation));
            System.out.printf("  95th Percentile: %dms%n", getPercentileResponseTime(operation, 95.0));
            System.out.printf("  99th Percentile: %dms%n", getPercentileResponseTime(operation, 99.0));
            System.out.println();
        }
        
        System.out.println("=".repeat(60));
    }
    
    /**
     * Checks if performance thresholds are met
     * @param maxAverageResponseTime maximum allowed average response time
     * @param maxErrorRate maximum allowed error rate percentage
     * @return true if all thresholds are met
     */
    public boolean checkThresholds(double maxAverageResponseTime, double maxErrorRate) {
        boolean allPassed = true;
        
        for (String operation : requestCounts.keySet()) {
            double avgResponseTime = getAverageResponseTime(operation);
            double errorRate = getErrorRate(operation);
            
            if (avgResponseTime > maxAverageResponseTime) {
                logger.warn("Performance threshold exceeded for {}: avg response time {}ms > {}ms", 
                           operation, avgResponseTime, maxAverageResponseTime);
                allPassed = false;
            }
            
            if (errorRate > maxErrorRate) {
                logger.warn("Performance threshold exceeded for {}: error rate {}% > {}%", 
                           operation, errorRate, maxErrorRate);
                allPassed = false;
            }
        }
        
        return allPassed;
    }
    
    /**
     * Exports performance data in JMeter JTL format for integration
     * @param filename the output filename
     */
    public void exportToJTLFormat(String filename) {
        try {
            Path outputPath = Paths.get(PERFORMANCE_REPORTS_DIR, filename);
            
            try (PrintWriter writer = new PrintWriter(Files.newBufferedWriter(outputPath))) {
                // JTL header
                writer.println("timeStamp,elapsed,label,responseCode,responseMessage,threadName,dataType,success,failureMessage,bytes,sentBytes,grpThreads,allThreads,URL,Filename,latency,encoding,SampleCount,ErrorCount,hostname,idleTime");
                
                // Export data for each operation
                long currentTime = System.currentTimeMillis();
                for (String operation : requestCounts.keySet()) {
                    List<Long> times = responseTimeHistory.get(operation);
                    int errorCount = errorCounts.getOrDefault(operation, new AtomicInteger(0)).get();
                    int totalCount = requestCounts.get(operation).get();
                    
                    if (times != null) {
                        for (int i = 0; i < times.size(); i++) {
                            long responseTime = times.get(i);
                            boolean isError = i < errorCount; // Simple approximation
                            
                            writer.printf("%d,%d,%s,%d,%s,Thread-1,text,%s,,1024,512,1,1,,,%d,,1,%d,localhost,0%n",
                                currentTime - (times.size() - i) * 1000, // Simulated timestamp
                                responseTime,
                                operation,
                                isError ? 500 : 200,
                                isError ? "Error" : "OK",
                                isError ? "false" : "true",
                                responseTime, // latency = response time for simplicity
                                isError ? 1 : 0
                            );
                        }
                    }
                }
            }
            
            logger.info("Performance data exported to JTL format: {}", outputPath);
            
        } catch (IOException e) {
            logger.error("Failed to export performance data to JTL format", e);
        }
    }
}