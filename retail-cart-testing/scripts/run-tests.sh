#!/bin/bash

# Retail Cart Testing - Test Execution Script
# This script provides easy test execution with different configurations

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Default values
ENVIRONMENT="dev"
BROWSER="chrome"
TAGS=""
HEADLESS="false"
PARALLEL="false"
GENERATE_REPORTS="true"

# Function to display usage
usage() {
    echo -e "${BLUE}Retail Cart Testing - Test Execution Script${NC}"
    echo ""
    echo "Usage: $0 [options]"
    echo ""
    echo "Options:"
    echo "  -e, --environment   Environment to test against (dev, staging, production) [default: dev]"
    echo "  -b, --browser       Browser to use (chrome, firefox, edge) [default: chrome]"
    echo "  -t, --tags          Cucumber tags to filter tests (e.g., @smoke, @regression)"
    echo "  -h, --headless      Run tests in headless mode [default: false]"
    echo "  -p, --parallel      Enable parallel execution [default: false]"
    echo "  -r, --no-reports    Skip report generation [default: generate reports]"
    echo "  --help              Show this help message"
    echo ""
    echo "Examples:"
    echo "  $0 -e staging -t @smoke"
    echo "  $0 -b firefox -h true"
    echo "  $0 -e production -t \"@smoke and not @wip\" -h true"
    echo ""
}

# Parse command line arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        -e|--environment)
            ENVIRONMENT="$2"
            shift 2
            ;;
        -b|--browser)
            BROWSER="$2"
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
            PARALLEL="true"
            shift
            ;;
        -r|--no-reports)
            GENERATE_REPORTS="false"
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

# Validate environment
if [[ ! "$ENVIRONMENT" =~ ^(dev|staging|production)$ ]]; then
    echo -e "${RED}❌ Invalid environment: $ENVIRONMENT${NC}"
    echo -e "${YELLOW}Valid environments: dev, staging, production${NC}"
    exit 1
fi

# Validate browser
if [[ ! "$BROWSER" =~ ^(chrome|firefox|edge|safari)$ ]]; then
    echo -e "${RED}❌ Invalid browser: $BROWSER${NC}"
    echo -e "${YELLOW}Valid browsers: chrome, firefox, edge, safari${NC}"
    exit 1
fi

echo -e "${BLUE}🚀 Starting Retail Cart Test Execution${NC}"
echo -e "${YELLOW}Configuration:${NC}"
echo -e "  Environment: ${GREEN}$ENVIRONMENT${NC}"
echo -e "  Browser: ${GREEN}$BROWSER${NC}"
echo -e "  Headless: ${GREEN}$HEADLESS${NC}"
echo -e "  Parallel: ${GREEN}$PARALLEL${NC}"
echo -e "  Tags: ${GREEN}${TAGS:-'all tests'}${NC}"
echo ""

# Check if Maven is available
if ! command -v mvn &> /dev/null; then
    echo -e "${RED}❌ Maven is not installed or not in PATH${NC}"
    exit 1
fi

# Prepare Maven command
MAVEN_CMD="mvn test"
MAVEN_CMD="$MAVEN_CMD -Denvironment=$ENVIRONMENT"
MAVEN_CMD="$MAVEN_CMD -Dbrowser=$BROWSER"
MAVEN_CMD="$MAVEN_CMD -Dheadless.mode=$HEADLESS"

# Add tags if specified
if [[ -n "$TAGS" ]]; then
    MAVEN_CMD="$MAVEN_CMD -Dcucumber.filter.tags=\"$TAGS\""
fi

# Add parallel execution if enabled
if [[ "$PARALLEL" == "true" ]]; then
    MAVEN_CMD="$MAVEN_CMD -Djunit.jupiter.execution.parallel.enabled=true"
fi

# Create necessary directories
mkdir -p target/{screenshots,allure-results,surefire-reports}
mkdir -p reports/{allure,surefire,custom,screenshots}

echo -e "${YELLOW}🔧 Executing Maven command:${NC}"
echo -e "${BLUE}$MAVEN_CMD${NC}"
echo ""

# Execute tests
start_time=$(date +%s)

if eval $MAVEN_CMD; then
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    echo -e "${GREEN}✅ Tests completed successfully in ${duration}s${NC}"
    TEST_EXIT_CODE=0
else
    end_time=$(date +%s)
    duration=$((end_time - start_time))
    echo -e "${RED}❌ Tests failed after ${duration}s${NC}"
    TEST_EXIT_CODE=1
fi

# Generate reports if requested
if [[ "$GENERATE_REPORTS" == "true" ]]; then
    echo -e "${YELLOW}📊 Generating test reports...${NC}"
    
    if [[ -f "scripts/generate-reports.sh" ]]; then
        ./scripts/generate-reports.sh
    else
        echo -e "${YELLOW}⚠️ Report generation script not found${NC}"
        
        # Fallback: generate basic Allure report
        if command -v allure &> /dev/null; then
            echo -e "${YELLOW}📊 Generating Allure report...${NC}"
            allure generate target/allure-results --clean -o target/allure-report
            echo -e "${GREEN}✅ Allure report generated at: target/allure-report/index.html${NC}"
        else
            echo -e "${YELLOW}⚠️ Allure CLI not found. Using Maven plugin...${NC}"
            mvn allure:report
        fi
    fi
fi

# Summary
echo ""
echo -e "${BLUE}📋 Test Execution Summary${NC}"
echo -e "${BLUE}=========================${NC}"
echo -e "Environment: ${GREEN}$ENVIRONMENT${NC}"
echo -e "Browser: ${GREEN}$BROWSER${NC}"
echo -e "Duration: ${GREEN}${duration}s${NC}"
echo -e "Status: $( [[ $TEST_EXIT_CODE -eq 0 ]] && echo -e "${GREEN}PASSED${NC}" || echo -e "${RED}FAILED${NC}" )"

if [[ "$GENERATE_REPORTS" == "true" ]]; then
    echo ""
    echo -e "${YELLOW}📊 Reports Available:${NC}"
    echo -e "  Allure Report: ${BLUE}target/allure-report/index.html${NC}"
    echo -e "  Surefire Reports: ${BLUE}target/surefire-reports/${NC}"
    echo -e "  Screenshots: ${BLUE}target/screenshots/${NC}"
fi

exit $TEST_EXIT_CODE