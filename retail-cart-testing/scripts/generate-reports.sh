#!/bin/bash

# Retail Cart Testing - Report Generation Script
# This script generates comprehensive test reports including Allure, JUnit, and custom reports

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
REPORTS_DIR="reports"
ALLURE_RESULTS_DIR="target/allure-results"
ALLURE_REPORT_DIR="target/allure-report"
SUREFIRE_REPORTS_DIR="target/surefire-reports"
CUSTOM_REPORTS_DIR="$REPORTS_DIR/custom"

echo -e "${GREEN}🚀 Starting report generation...${NC}"

# Create reports directory structure
mkdir -p "$REPORTS_DIR"/{allure,surefire,custom,screenshots,jmeter}
mkdir -p "$CUSTOM_REPORTS_DIR"

# Function to check if command exists
command_exists() {
    command -v "$1" >/dev/null 2>&1
}

# Function to generate Allure report
generate_allure_report() {
    echo -e "${YELLOW}📊 Generating Allure report...${NC}"
    
    if [ ! -d "$ALLURE_RESULTS_DIR" ]; then
        echo -e "${RED}❌ No Allure results found. Run tests first.${NC}"
        return 1
    fi
    
    if command_exists allure; then
        allure generate "$ALLURE_RESULTS_DIR" --clean -o "$ALLURE_REPORT_DIR"
        cp -r "$ALLURE_REPORT_DIR"/* "$REPORTS_DIR/allure/"
        echo -e "${GREEN}✅ Allure report generated successfully${NC}"
    else
        echo -e "${YELLOW}⚠️ Allure CLI not found. Using Maven plugin...${NC}"
        mvn allure:report
        if [ -d "$ALLURE_REPORT_DIR" ]; then
            cp -r "$ALLURE_REPORT_DIR"/* "$REPORTS_DIR/allure/"
            echo -e "${GREEN}✅ Allure report generated via Maven${NC}"
        fi
    fi
}

# Function to copy Surefire reports
copy_surefire_reports() {
    echo -e "${YELLOW}📋 Copying Surefire reports...${NC}"
    
    if [ -d "$SUREFIRE_REPORTS_DIR" ]; then
        cp -r "$SUREFIRE_REPORTS_DIR"/* "$REPORTS_DIR/surefire/"
        echo -e "${GREEN}✅ Surefire reports copied${NC}"
    else
        echo -e "${YELLOW}⚠️ No Surefire reports found${NC}"
    fi
}

# Function to generate custom summary report
generate_custom_summary() {
    echo -e "${YELLOW}📈 Generating custom summary report...${NC}"
    
    cat > "$CUSTOM_REPORTS_DIR/test-summary.html" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Retail Cart Testing - Test Summary</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .header { background: #2196F3; color: white; padding: 20px; border-radius: 5px; }
        .summary-card { border: 1px solid #ddd; margin: 10px 0; padding: 15px; border-radius: 5px; }
        .success { background: #e8f5e8; border-color: #4caf50; }
        .warning { background: #fff3cd; border-color: #ffc107; }
        .error { background: #f8d7da; border-color: #dc3545; }
        .metric { display: inline-block; margin: 10px; padding: 10px; background: #f5f5f5; border-radius: 3px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }
        th { background-color: #f2f2f2; }
    </style>
</head>
<body>
    <div class="header">
        <h1>🛒 Retail Cart Testing Suite</h1>
        <p>Comprehensive test execution report</p>
    </div>
    
    <div class="summary-card success">
        <h2>📊 Test Execution Summary</h2>
        <div class="metric"><strong>Total Tests:</strong> <span id="totalTests">-</span></div>
        <div class="metric"><strong>Passed:</strong> <span id="passedTests">-</span></div>
        <div class="metric"><strong>Failed:</strong> <span id="failedTests">-</span></div>
        <div class="metric"><strong>Skipped:</strong> <span id="skippedTests">-</span></div>
        <div class="metric"><strong>Success Rate:</strong> <span id="successRate">-</span>%</div>
    </div>
    
    <div class="summary-card">
        <h2>🔍 Test Categories</h2>
        <table>
            <tr><th>Category</th><th>Tests</th><th>Status</th></tr>
            <tr><td>Shopping Cart Operations</td><td id="cartTests">-</td><td id="cartStatus">-</td></tr>
            <tr><td>Promotion Logic</td><td id="promoTests">-</td><td id="promoStatus">-</td></tr>
            <tr><td>API Validation</td><td id="apiTests">-</td><td id="apiStatus">-</td></tr>
            <tr><td>UI Interactions</td><td id="uiTests">-</td><td id="uiStatus">-</td></tr>
        </table>
    </div>
    
    <div class="summary-card">
        <h2>🌐 Browser Coverage</h2>
        <table>
            <tr><th>Browser</th><th>Tests Executed</th><th>Status</th></tr>
            <tr><td>Chrome</td><td id="chromeTests">-</td><td id="chromeStatus">-</td></tr>
            <tr><td>Firefox</td><td id="firefoxTests">-</td><td id="firefoxStatus">-</td></tr>
            <tr><td>Edge</td><td id="edgeTests">-</td><td id="edgeStatus">-</td></tr>
        </table>
    </div>
    
    <div class="summary-card">
        <h2>📈 Performance Metrics</h2>
        <div class="metric"><strong>Avg Test Duration:</strong> <span id="avgDuration">-</span>s</div>
        <div class="metric"><strong>Total Execution Time:</strong> <span id="totalTime">-</span></div>
        <div class="metric"><strong>Environment:</strong> <span id="environment">-</span></div>
    </div>
    
    <div class="summary-card">
        <h2>📋 Quick Links</h2>
        <ul>
            <li><a href="../allure/index.html" target="_blank">🔗 Detailed Allure Report</a></li>
            <li><a href="../surefire/index.html" target="_blank">🔗 Surefire Reports</a></li>
            <li><a href="../screenshots/" target="_blank">🔗 Test Screenshots</a></li>
        </ul>
    </div>
    
    <script>
        // This would be populated by the report generation script
        // For now, showing placeholder values
        document.getElementById('totalTests').textContent = 'Loading...';
        document.getElementById('environment').textContent = 'Staging';
    </script>
</body>
</html>
EOF
    
    echo -e "${GREEN}✅ Custom summary report generated${NC}"
}

# Function to copy screenshots
copy_screenshots() {
    echo -e "${YELLOW}📸 Copying screenshots...${NC}"
    
    if [ -d "target/screenshots" ]; then
        cp -r target/screenshots/* "$REPORTS_DIR/screenshots/" 2>/dev/null || true
        echo -e "${GREEN}✅ Screenshots copied${NC}"
    else
        echo -e "${YELLOW}⚠️ No screenshots found${NC}"
    fi
}

# Function to generate test execution summary
generate_execution_summary() {
    echo -e "${YELLOW}📝 Generating execution summary...${NC}"
    
    TIMESTAMP=$(date '+%Y-%m-%d %H:%M:%S')
    
    cat > "$REPORTS_DIR/execution-summary.txt" << EOF
===========================================
RETAIL CART TESTING - EXECUTION SUMMARY
===========================================

Execution Date: $TIMESTAMP
Test Suite: Retail Cart Testing Framework
Environment: ${ENVIRONMENT:-staging}
Browser: ${BROWSER:-chrome}

Report Locations:
- Allure Report: $REPORTS_DIR/allure/index.html
- Surefire Reports: $REPORTS_DIR/surefire/
- Custom Summary: $REPORTS_DIR/custom/test-summary.html
- Screenshots: $REPORTS_DIR/screenshots/

Generated by: Retail Cart Testing Report Generator
===========================================
EOF
    
    echo -e "${GREEN}✅ Execution summary generated${NC}"
}

# Function to create index.html for easy navigation
create_index() {
    echo -e "${YELLOW}🏠 Creating navigation index...${NC}"
    
    cat > "$REPORTS_DIR/index.html" << 'EOF'
<!DOCTYPE html>
<html>
<head>
    <title>Retail Cart Testing - Report Center</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; }
        .header { background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white; padding: 30px; border-radius: 10px; text-align: center; margin-bottom: 30px; }
        .card-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 20px; }
        .card { background: white; border-radius: 10px; padding: 20px; box-shadow: 0 4px 6px rgba(0,0,0,0.1); transition: transform 0.3s; }
        .card:hover { transform: translateY(-5px); }
        .card h3 { margin-top: 0; color: #333; }
        .btn { display: inline-block; background: #007bff; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px; margin: 5px 0; }
        .btn:hover { background: #0056b3; }
        .btn-success { background: #28a745; }
        .btn-info { background: #17a2b8; }
        .btn-warning { background: #ffc107; color: #212529; }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>🛒 Retail Cart Testing</h1>
            <p>Comprehensive Test Report Center</p>
        </div>
        
        <div class="card-grid">
            <div class="card">
                <h3>📊 Allure Report</h3>
                <p>Detailed test execution results with rich visualizations, trends, and analytics.</p>
                <a href="allure/index.html" class="btn btn-success" target="_blank">View Allure Report</a>
            </div>
            
            <div class="card">
                <h3>📋 Surefire Reports</h3>
                <p>Maven Surefire test results with JUnit compatible format.</p>
                <a href="surefire/" class="btn btn-info" target="_blank">View Surefire Reports</a>
            </div>
            
            <div class="card">
                <h3>📈 Custom Summary</h3>
                <p>High-level test execution summary with key metrics and insights.</p>
                <a href="custom/test-summary.html" class="btn btn-warning" target="_blank">View Summary</a>
            </div>
            
            <div class="card">
                <h3>📸 Screenshots</h3>
                <p>Test failure screenshots and visual evidence for debugging.</p>
                <a href="screenshots/" class="btn" target="_blank">View Screenshots</a>
            </div>
        </div>
    </div>
</body>
</html>
EOF
    
    echo -e "${GREEN}✅ Navigation index created${NC}"
}

# Main execution
main() {
    echo -e "${GREEN}🎯 Starting comprehensive report generation...${NC}"
    
    # Generate all reports
    generate_allure_report
    copy_surefire_reports
    generate_custom_summary
    copy_screenshots
    generate_execution_summary
    create_index
    
    echo -e "${GREEN}🎉 Report generation completed successfully!${NC}"
    echo -e "${YELLOW}📁 Reports available in: $REPORTS_DIR/${NC}"
    echo -e "${YELLOW}🌐 Open: $REPORTS_DIR/index.html${NC}"
}

# Run main function
main "$@"