#!/bin/bash

# Retail Cart Testing - Cross-Browser Test Execution Script
# This script runs tests across multiple browsers in parallel

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
BROWSERS=("chrome" "firefox" "edge")
ENVIRONMENT="staging"
TAGS="@cross-browser"
HEADLESS="true"
PARALLEL="true"
MAX_PARALLEL_BROWSERS=3

# Function to display usage
usage() {
    echo -e "${BLUE}Cross-Browser Test Execution Script${NC}"
    echo ""
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  -b, --browsers     Comma-separated list of browsers [default: chrome,firefox,edge]"
    echo "  -e, --environment  Test environment [default: staging]"
    echo "  -t, --tags         Test tags to run [default: @cross-browser]"
    echo "  -h, --headless     Run in headless mode [default: true]"
    echo "  -p, --parallel     Run browsers in parallel [default: true]"
    echo "  -m, --max-parallel Maximum parallel browsers [default: 3]"
    echo "  --sequential       Run browsers sequentially"
    echo "  --help             Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -b chrome,firefox"
    echo "  $0 --sequential -t @smoke"
    echo "  $0 -e production -h false"
    echo ""
}

# Function to check browser availability
check_browser_availability() {
    local browser="$1"
    
    case $browser in
        chrome)
            if command -v google-chrome &> /dev/null || command -v chromium-browser &> /dev/null || command -v google-chrome-stable &> /dev/null; then
                echo -e "${GREEN}✅ Chrome browser available${NC}"
                return 0
            else
                echo -e "${YELLOW}⚠️ Chrome browser not found${NC}"
                return 1
            fi
            ;;
        firefox)
            if command -v firefox &> /dev/null; then
                echo -e "${GREEN}✅ Firefox browser available${NC}"
                return 0
            else
                echo -e "${YELLOW}⚠️ Firefox browser not found${NC}"
                return 1
            fi
            ;;
        edge)
            if command -v microsoft-edge &> /dev/null || command -v msedge &> /dev/null; then
                echo -e "${GREEN}✅ Edge browser available${NC}"
                return 0
            else
                echo -e "${YELLOW}⚠️ Edge browser not found (continuing anyway)${NC}"
                return 0  # Edge might be available through WebDriverManager
            fi
            ;;
        safari)
            if [[ "$OSTYPE" == "darwin"* ]] && command -v safaridriver &> /dev/null; then
                echo -e "${GREEN}✅ Safari browser available${NC}"
                return 0
            else
                echo -e "${YELLOW}⚠️ Safari not available (macOS only)${NC}"
                return 1
            fi
            ;;
        *)
            echo -e "${RED}❌ Unknown browser: $browser${NC}"
            return 1
            ;;
    esac
}

# Function to run tests for a single browser
run_browser_test() {
    local browser="$1"
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local log_file="target/cross-browser-logs/${browser}_${timestamp}.log"
    local report_dir="reports/cross-browser/${browser}_${timestamp}"
    
    echo -e "${BLUE}🚀 Starting tests for: $browser${NC}"
    
    # Create log directory
    mkdir -p "target/cross-browser-logs" "reports/cross-browser"
    
    # Maven command for single browser
    local maven_cmd="mvn test"
    maven_cmd="$maven_cmd -Denvironment=$ENVIRONMENT"
    maven_cmd="$maven_cmd -Dbrowser=$browser"
    maven_cmd="$maven_cmd -Dheadless.mode=$HEADLESS"
    maven_cmd="$maven_cmd -Dcucumber.filter.tags=\"$TAGS\""
    maven_cmd="$maven_cmd -Dtest=CrossBrowserTest"
    maven_cmd="$maven_cmd -Dallure.results.directory=target/allure-results-$browser"
    
    # Execute test
    if eval "$maven_cmd" > "$log_file" 2>&1; then
        echo -e "${GREEN}✅ $browser tests completed successfully${NC}"
        
        # Generate Allure report for this browser
        if command -v allure &> /dev/null; then
            allure generate "target/allure-results-$browser" --clean -o "$report_dir" >> "$log_file" 2>&1
            echo -e "${GREEN}📊 $browser report: $report_dir/index.html${NC}"
        fi
        
        return 0
    else
        echo -e "${RED}❌ $browser tests failed${NC}"
        echo -e "${YELLOW}Check log: $log_file${NC}"
        return 1
    fi
}

# Function to run tests in parallel
run_parallel_tests() {
    local browsers=("$@")
    local pids=()
    local results=()
    local active_jobs=0
    
    echo -e "${BLUE}🚀 Running cross-browser tests in parallel...${NC}"
    echo -e "${YELLOW}Browsers: ${browsers[*]}${NC}"
    echo -e "${YELLOW}Max parallel: $MAX_PARALLEL_BROWSERS${NC}"
    echo ""
    
    for browser in "${browsers[@]}"; do
        # Wait if we've reached the maximum parallel jobs
        while [ $active_jobs -ge $MAX_PARALLEL_BROWSERS ]; do
            wait -n
            active_jobs=$((active_jobs - 1))
        done
        
        # Start browser test in background
        (
            run_browser_test "$browser"
            echo $? > "target/cross-browser-logs/${browser}.result"
        ) &
        
        pids+=($!)
        active_jobs=$((active_jobs + 1))
        
        echo -e "${BLUE}Started $browser tests (PID: ${pids[-1]})${NC}"
    done
    
    # Wait for all background jobs to complete
    echo -e "${YELLOW}⏳ Waiting for all browser tests to complete...${NC}"
    for pid in "${pids[@]}"; do
        wait $pid
    done
    
    # Collect results
    local failed_browsers=()
    local successful_browsers=()
    
    for browser in "${browsers[@]}"; do
        if [ -f "target/cross-browser-logs/${browser}.result" ]; then
            result=$(cat "target/cross-browser-logs/${browser}.result")
            if [ "$result" -eq 0 ]; then
                successful_browsers+=("$browser")
            else
                failed_browsers+=("$browser")
            fi
        else
            failed_browsers+=("$browser")
        fi
    done
    
    # Display results summary
    echo ""
    echo -e "${BLUE}📊 Cross-Browser Test Results Summary${NC}"
    echo -e "${BLUE}=====================================${NC}"
    
    if [ ${#successful_browsers[@]} -gt 0 ]; then
        echo -e "${GREEN}✅ Successful browsers: ${successful_browsers[*]}${NC}"
    fi
    
    if [ ${#failed_browsers[@]} -gt 0 ]; then
        echo -e "${RED}❌ Failed browsers: ${failed_browsers[*]}${NC}"
        return 1
    fi
    
    echo -e "${GREEN}🎉 All browser tests completed successfully!${NC}"
    return 0
}

# Function to run tests sequentially
run_sequential_tests() {
    local browsers=("$@")
    local failed_browsers=()
    local successful_browsers=()
    
    echo -e "${BLUE}🚀 Running cross-browser tests sequentially...${NC}"
    echo -e "${YELLOW}Browsers: ${browsers[*]}${NC}"
    echo ""
    
    for browser in "${browsers[@]}"; do
        if run_browser_test "$browser"; then
            successful_browsers+=("$browser")
        else
            failed_browsers+=("$browser")
        fi
        echo ""
    done
    
    # Display results summary
    echo -e "${BLUE}📊 Cross-Browser Test Results Summary${NC}"
    echo -e "${BLUE}=====================================${NC}"
    
    if [ ${#successful_browsers[@]} -gt 0 ]; then
        echo -e "${GREEN}✅ Successful browsers: ${successful_browsers[*]}${NC}"
    fi
    
    if [ ${#failed_browsers[@]} -gt 0 ]; then
        echo -e "${RED}❌ Failed browsers: ${failed_browsers[*]}${NC}"
        return 1
    fi
    
    echo -e "${GREEN}🎉 All browser tests completed successfully!${NC}"
    return 0
}

# Function to generate combined report
generate_combined_report() {
    echo -e "${YELLOW}📊 Generating combined cross-browser report...${NC}"
    
    local timestamp=$(date '+%Y%m%d_%H%M%S')
    local combined_results_dir="target/allure-results-combined"
    local combined_report_dir="reports/cross-browser/combined_${timestamp}"
    
    # Create combined results directory
    mkdir -p "$combined_results_dir"
    
    # Copy all browser results to combined directory
    for browser in "${BROWSERS[@]}"; do
        if [ -d "target/allure-results-$browser" ]; then
            cp -r "target/allure-results-$browser"/* "$combined_results_dir/" 2>/dev/null || true
        fi
    done
    
    # Generate combined report
    if command -v allure &> /dev/null && [ "$(ls -A $combined_results_dir 2>/dev/null)" ]; then
        allure generate "$combined_results_dir" --clean -o "$combined_report_dir"
        echo -e "${GREEN}✅ Combined report: $combined_report_dir/index.html${NC}"
    else
        echo -e "${YELLOW}⚠️ Allure not available or no results found${NC}"
    fi
}

# Function to cleanup previous results
cleanup_previous_results() {
    echo -e "${YELLOW}🧹 Cleaning up previous test results...${NC}"
    
    rm -rf target/allure-results-*
    rm -rf target/cross-browser-logs/*
    rm -f target/cross-browser-logs/*.result
    
    mkdir -p target/cross-browser-logs reports/cross-browser
    
    echo -e "${GREEN}✅ Cleanup completed${NC}"
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -b|--browsers)
            IFS=',' read -ra BROWSERS <<< "$2"
            shift 2
            ;;
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -t|--tags)
            TAGS="$2"
            shift 2
            ;;
        -h|--headless)
            HEADLESS="$2"
            shift 2
            ;;
        -p|--parallel)
            PARALLEL="$2"
            shift 2
            ;;
        -m|--max-parallel)
            MAX_PARALLEL_BROWSERS="$2"
            shift 2
            ;;
        --sequential)
            PARALLEL="false"
            shift
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

# Main execution
echo -e "${BLUE}🌐 Cross-Browser Testing Suite${NC}"
echo ""

# Display configuration
echo -e "${YELLOW}Configuration:${NC}"
echo -e "  Browsers: ${GREEN}${BROWSERS[*]}${NC}"
echo -e "  Environment: ${GREEN}$ENVIRONMENT${NC}"
echo -e "  Tags: ${GREEN}$TAGS${NC}"
echo -e "  Headless: ${GREEN}$HEADLESS${NC}"
echo -e "  Parallel: ${GREEN}$PARALLEL${NC}"
echo ""

# Check browser availability
echo -e "${YELLOW}🔍 Checking browser availability...${NC}"
available_browsers=()
for browser in "${BROWSERS[@]}"; do
    if check_browser_availability "$browser"; then
        available_browsers+=("$browser")
    fi
done

if [ ${#available_browsers[@]} -eq 0 ]; then
    echo -e "${RED}❌ No browsers available for testing${NC}"
    exit 1
fi

echo -e "${GREEN}Available browsers: ${available_browsers[*]}${NC}"
echo ""

# Cleanup previous results
cleanup_previous_results

# Run tests
if [ "$PARALLEL" = "true" ]; then
    run_parallel_tests "${available_browsers[@]}"
    test_result=$?
else
    run_sequential_tests "${available_browsers[@]}"
    test_result=$?
fi

# Generate combined report
generate_combined_report

# Exit with appropriate code
if [ $test_result -eq 0 ]; then
    echo ""
    echo -e "${GREEN}🎉 Cross-browser testing completed successfully!${NC}"
    exit 0
else
    echo ""
    echo -e "${RED}❌ Some browser tests failed${NC}"
    exit 1
fi