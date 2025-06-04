#!/bin/bash

# Retail Cart Testing - Performance Test Execution Script
# This script runs JMeter performance tests with various configurations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
JMETER_HOME="${JMETER_HOME:-}"
JMETER_VERSION="5.5"
RESULTS_DIR="target/jmeter-results"
REPORTS_DIR="reports/jmeter"
TEST_DIR="src/test/jmeter"

# Default test parameters
USERS=10
RAMP_UP=30
DURATION=300
BASE_URL="https://api.retailer.com/v1"
API_DOMAIN="api.retailer.com"

# Function to display usage
usage() {
    echo -e "${BLUE}Performance Test Execution Script${NC}"
    echo ""
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  -u, --users        Number of concurrent users [default: 10]"
    echo "  -r, --rampup       Ramp-up time in seconds [default: 30]"
    echo "  -d, --duration     Test duration in seconds [default: 300]"
    echo "  -b, --baseurl      Base API URL [default: https://api.retailer.com/v1]"
    echo "  -t, --test         Test file to run [default: cart-performance.jmx]"
    echo "  -e, --environment  Test environment [default: staging]"
    echo "  --install-jmeter   Download and install JMeter"
    echo "  --help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -u 50 -r 60 -d 600"
    echo "  $0 --test cart-stress.jmx -u 100"
    echo "  $0 --install-jmeter"
    echo ""
}

# Function to check if JMeter is installed
check_jmeter() {
    if command -v jmeter &> /dev/null; then
        JMETER_CMD="jmeter"
        echo -e "${GREEN}✅ JMeter found in PATH${NC}"
        return 0
    elif [ -n "$JMETER_HOME" ] && [ -f "$JMETER_HOME/bin/jmeter" ]; then
        JMETER_CMD="$JMETER_HOME/bin/jmeter"
        echo -e "${GREEN}✅ JMeter found at: $JMETER_HOME${NC}"
        return 0
    elif [ -f "apache-jmeter-$JMETER_VERSION/bin/jmeter" ]; then
        JMETER_CMD="apache-jmeter-$JMETER_VERSION/bin/jmeter"
        echo -e "${GREEN}✅ Local JMeter installation found${NC}"
        return 0
    else
        echo -e "${RED}❌ JMeter not found${NC}"
        echo -e "${YELLOW}Run with --install-jmeter to download and install JMeter${NC}"
        return 1
    fi
}

# Function to install JMeter
install_jmeter() {
    echo -e "${YELLOW}📦 Installing JMeter $JMETER_VERSION...${NC}"
    
    if [ -d "apache-jmeter-$JMETER_VERSION" ]; then
        echo -e "${YELLOW}JMeter $JMETER_VERSION already exists${NC}"
        return 0
    fi
    
    # Download JMeter
    JMETER_URL="https://archive.apache.org/dist/jmeter/binaries/apache-jmeter-$JMETER_VERSION.tgz"
    echo -e "${BLUE}Downloading JMeter from: $JMETER_URL${NC}"
    
    if command -v wget &> /dev/null; then
        wget -q --show-progress "$JMETER_URL"
    elif command -v curl &> /dev/null; then
        curl -L -o "apache-jmeter-$JMETER_VERSION.tgz" "$JMETER_URL"
    else
        echo -e "${RED}❌ Neither wget nor curl found. Please install one of them.${NC}"
        exit 1
    fi
    
    # Extract JMeter
    echo -e "${BLUE}Extracting JMeter...${NC}"
    tar -xzf "apache-jmeter-$JMETER_VERSION.tgz"
    rm "apache-jmeter-$JMETER_VERSION.tgz"
    
    # Make JMeter executable
    chmod +x "apache-jmeter-$JMETER_VERSION/bin/jmeter"
    
    echo -e "${GREEN}✅ JMeter $JMETER_VERSION installed successfully${NC}"
}

# Function to prepare test environment
prepare_environment() {
    echo -e "${YELLOW}🔧 Preparing test environment...${NC}"
    
    # Create necessary directories
    mkdir -p "$RESULTS_DIR" "$REPORTS_DIR" "target/screenshots"
    
    # Clean previous results
    rm -f "$RESULTS_DIR"/*.jtl "$RESULTS_DIR"/*.log
    
    echo -e "${GREEN}✅ Environment prepared${NC}"
}

# Function to validate test file
validate_test_file() {
    local test_file="$1"
    
    if [ ! -f "$TEST_DIR/$test_file" ]; then
        echo -e "${RED}❌ Test file not found: $TEST_DIR/$test_file${NC}"
        echo -e "${YELLOW}Available test files:${NC}"
        ls -1 "$TEST_DIR"/*.jmx 2>/dev/null || echo "No JMeter test files found"
        exit 1
    fi
    
    echo -e "${GREEN}✅ Test file validated: $test_file${NC}"
}

# Function to run performance test
run_performance_test() {
    local test_file="$1"
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    
    echo -e "${BLUE}🚀 Starting performance test...${NC}"
    echo -e "${YELLOW}Configuration:${NC}"
    echo -e "  Test File: ${GREEN}$test_file${NC}"
    echo -e "  Users: ${GREEN}$USERS${NC}"
    echo -e "  Ramp-up: ${GREEN}$RAMP_UP seconds${NC}"
    echo -e "  Duration: ${GREEN}$DURATION seconds${NC}"
    echo -e "  Base URL: ${GREEN}$BASE_URL${NC}"
    echo ""
    
    # JMeter command with parameters
    local jmeter_cmd="$JMETER_CMD -n -t $TEST_DIR/$test_file"
    jmeter_cmd="$jmeter_cmd -Jusers=$USERS"
    jmeter_cmd="$jmeter_cmd -Jrampup=$RAMP_UP"
    jmeter_cmd="$jmeter_cmd -Jduration=$DURATION"
    jmeter_cmd="$jmeter_cmd -Jbase.url=$BASE_URL"
    jmeter_cmd="$jmeter_cmd -Japi.domain=$API_DOMAIN"
    jmeter_cmd="$jmeter_cmd -l $RESULTS_DIR/results_${timestamp}.jtl"
    jmeter_cmd="$jmeter_cmd -e -o $REPORTS_DIR/report_${timestamp}"
    jmeter_cmd="$jmeter_cmd -j $RESULTS_DIR/jmeter_${timestamp}.log"
    
    echo -e "${BLUE}Executing: $jmeter_cmd${NC}"
    echo ""
    
    # Execute JMeter test
    local start_time=$(date +%s)
    
    if eval "$jmeter_cmd"; then
        local end_time=$(date +%s)
        local duration=$((end_time - start_time))
        
        echo ""
        echo -e "${GREEN}✅ Performance test completed successfully in ${duration}s${NC}"
        
        # Generate summary
        generate_summary "$timestamp"
        
        # Display results location
        echo -e "${YELLOW}📊 Results available at:${NC}"
        echo -e "  HTML Report: ${BLUE}$REPORTS_DIR/report_${timestamp}/index.html${NC}"
        echo -e "  Raw Results: ${BLUE}$RESULTS_DIR/results_${timestamp}.jtl${NC}"
        echo -e "  JMeter Log: ${BLUE}$RESULTS_DIR/jmeter_${timestamp}.log${NC}"
        
        return 0
    else
        echo -e "${RED}❌ Performance test failed${NC}"
        echo -e "${YELLOW}Check the JMeter log for details: $RESULTS_DIR/jmeter_${timestamp}.log${NC}"
        return 1
    fi
}

# Function to generate test summary
generate_summary() {
    local timestamp="$1"
    local results_file="$RESULTS_DIR/results_${timestamp}.jtl"
    local summary_file="$REPORTS_DIR/summary_${timestamp}.txt"
    
    if [ ! -f "$results_file" ]; then
        echo -e "${YELLOW}⚠️ Results file not found, skipping summary${NC}"
        return
    fi
    
    echo -e "${YELLOW}📋 Generating test summary...${NC}"
    
    cat > "$summary_file" << EOF
===========================================
PERFORMANCE TEST SUMMARY
===========================================

Test Execution Details:
- Timestamp: $(date)
- Test File: $TEST_FILE
- Users: $USERS
- Ramp-up: $RAMP_UP seconds
- Duration: $DURATION seconds
- Base URL: $BASE_URL

Results:
- Results File: $results_file
- HTML Report: $REPORTS_DIR/report_${timestamp}/index.html
- Summary File: $summary_file

Key Metrics:
EOF
    
    # Extract key metrics from JTL file (if available)
    if command -v awk &> /dev/null && [ -f "$results_file" ]; then
        echo "- Total Samples: $(tail -n +2 "$results_file" | wc -l)" >> "$summary_file"
        echo "- Success Rate: $(tail -n +2 "$results_file" | awk -F, '{if($8=="true") success++; total++} END {printf "%.2f%%", (success/total)*100}')" >> "$summary_file"
        echo "- Average Response Time: $(tail -n +2 "$results_file" | awk -F, '{sum+=$2; count++} END {printf "%.0fms", sum/count}')" >> "$summary_file"
    fi
    
    echo "" >> "$summary_file"
    echo "Generated by: Retail Cart Performance Test Suite" >> "$summary_file"
    echo "==========================================" >> "$summary_file"
    
    echo -e "${GREEN}✅ Summary generated: $summary_file${NC}"
}

# Function to run stress test
run_stress_test() {
    echo -e "${YELLOW}🔥 Running stress test configuration...${NC}"
    USERS=100
    RAMP_UP=60
    DURATION=600
    run_performance_test "cart-performance.jmx"
}

# Function to run smoke test
run_smoke_test() {
    echo -e "${YELLOW}💨 Running smoke test configuration...${NC}"
    USERS=5
    RAMP_UP=10
    DURATION=60
    run_performance_test "cart-performance.jmx"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -u|--users)
            USERS="$2"
            shift 2
            ;;
        -r|--rampup)
            RAMP_UP="$2"
            shift 2
            ;;
        -d|--duration)
            DURATION="$2"
            shift 2
            ;;
        -b|--baseurl)
            BASE_URL="$2"
            API_DOMAIN=$(echo "$BASE_URL" | sed 's|https\?://||' | sed 's|/.*||')
            shift 2
            ;;
        -t|--test)
            TEST_FILE="$2"
            shift 2
            ;;
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        --stress)
            RUN_STRESS=true
            shift
            ;;
        --smoke)
            RUN_SMOKE=true
            shift
            ;;
        --install-jmeter)
            install_jmeter
            exit 0
            ;;
        --help)
            usage
            exit 0
            ;;
        *)
            echo -e "${RED}Unknown option: $1${NC}"
            usage
            exit 1
            ;;
    esac
done

# Set default test file if not provided
TEST_FILE="${TEST_FILE:-cart-performance.jmx}"

# Main execution
echo -e "${BLUE}🧪 Retail Cart Performance Testing Suite${NC}"
echo ""

# Check JMeter installation
if ! check_jmeter; then
    exit 1
fi

# Prepare environment
prepare_environment

# Validate test file
validate_test_file "$TEST_FILE"

# Run appropriate test
if [ "$RUN_STRESS" = true ]; then
    run_stress_test
elif [ "$RUN_SMOKE" = true ]; then
    run_smoke_test
else
    run_performance_test "$TEST_FILE"
fi

echo ""
echo -e "${GREEN}🎉 Performance testing completed!${NC}"