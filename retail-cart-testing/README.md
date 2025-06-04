# Retail Cart Testing Framework

A comprehensive test automation framework for retail shopping cart functionality, built with Java, Selenium WebDriver, Cucumber BDD, and REST Assured.

## 🚀 Features

- **BDD Testing** with Cucumber for business-readable test scenarios
- **UI Testing** with Selenium WebDriver and Page Object Model
- **API Testing** with REST Assured for backend validation
- **Multi-environment** support (dev, staging, production)
- **Parallel Test Execution** for faster feedback
- **Allure Reporting** for detailed test reports
- **Docker Support** for consistent cross-platform testing
- **CI/CD Ready** with GitHub Actions integration

## 📋 Prerequisites

- **Java 11** or higher
- **Maven 3.6+**
- **Docker** (optional, for containerized testing)
- **Git**
- **Chrome Browser** (for UI tests)

## 🛠️ Quick Setup

### 1. Clone the Repository
```bash
git clone <repository-url>
cd retail-cart-testing
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Configure Test Environment
```bash
# Copy environment template
cp src/test/resources/config.properties.template src/test/resources/config.properties

# Edit configuration for your environment
nano src/test/resources/config.properties
```

### 4. Run Tests
```bash
# Run all tests
mvn test

# Run specific test suite
mvn test -Dcucumber.filter.tags="@smoke"

# Run with specific browser
mvn test -Dbrowser=chrome

# Run in headless mode
mvn test -Dheadless.mode=true
```

## 🏗️ Project Structure

```
retail-cart-testing/
├── src/test/java/com/retailer/cart/
│   ├── api/tests/           # API test classes
│   ├── models/             # Data models (Product, Promotion, ShoppingCart)
│   ├── pages/              # Page Object Model classes
│   ├── runners/            # Test runners and suites
│   ├── steps/              # Cucumber step definitions
│   └── utils/              # Utility classes (drivers, config, services)
├── src/test/resources/
│   ├── config/             # Environment-specific configurations
│   ├── features/           # Cucumber feature files
│   ├── schemas/            # JSON schemas for API validation
│   └── testdata/           # Test data files
├── reports/                # Generated test reports
├── docker/                 # Docker configurations
└── .github/workflows/      # CI/CD pipeline definitions
```

## 🧪 Testing Capabilities

### UI Testing Features
- Shopping cart operations (add, remove, update quantities)
- Promotion code application and validation
- Price calculations and discount logic
- Empty cart scenarios
- Cross-browser compatibility

### API Testing Features
- Cart management endpoints
- Product catalog validation
- Promotion service testing
- JSON schema validation
- Response time assertions

### Test Data Management
- CSV-based test data for products and promotions
- Dynamic test data generation
- Environment-specific data sets

## 🔧 Configuration

### Environment Configuration
Edit `src/test/resources/config.properties`:

```properties
# Application URLs
base.url=https://your-app-url.com/cart
api.base.url=https://api.your-app.com/v1

# Browser Settings
browser=chrome
headless.mode=false

# Timeouts (seconds)
implicit.wait.seconds=10
pageload.timeout.seconds=30
explicit.wait.seconds=15

# Test Environment
environment=staging
```

### Browser Configuration
Supported browsers:
- `chrome` (default)
- `firefox`
- `edge`
- `safari` (macOS only)

### Parallel Execution
Configure in `pom.xml`:
```xml
<forkCount>2C</forkCount> <!-- 2 threads per CPU core -->
<parallel>classes</parallel>
```

## 📊 Test Reporting

### Allure Reports
```bash
# Generate and serve Allure report
mvn allure:serve

# Generate report only
mvn allure:report
```

### Surefire Reports
Maven Surefire reports are generated in `target/surefire-reports/`

## 🐳 Docker Support

### Run Tests in Docker
```bash
# Build test image
docker build -t retail-cart-tests .

# Run tests
docker run --rm -v $(pwd)/reports:/app/reports retail-cart-tests

# Run with custom configuration
docker run --rm -e ENVIRONMENT=staging retail-cart-tests
```

### Docker Compose (with Selenium Grid)
```bash
# Start Selenium Grid and run tests
docker-compose up --abort-on-container-exit

# Clean up
docker-compose down
```

## 🚦 CI/CD Integration

### GitHub Actions
The project includes GitHub Actions workflows for:
- **Pull Request Validation**: Runs smoke tests on PRs
- **Main Branch Testing**: Full test suite on merges
- **Nightly Regression**: Complete test execution
- **Release Testing**: Production readiness validation

### Jenkins Integration
```bash
# Run tests with Jenkins-friendly output
mvn test -Djunit.jupiter.execution.parallel.enabled=true
```

## 📝 Test Scenarios

### Promotion Testing
- ✅ Single discount code application
- ✅ Multiple combinable discount codes
- ✅ Non-combinable discount code validation
- ✅ Invalid/expired promotion codes
- ✅ Minimum spend requirements
- ✅ Product-specific promotions

### Cart Management
- ✅ Add/remove products
- ✅ Update quantities (including zero)
- ✅ Empty cart handling
- ✅ Price calculations
- ✅ Subtotal and final price validation

## 🎯 Tag-based Test Execution

```bash
# Smoke tests
mvn test -Dcucumber.filter.tags="@smoke"

# Regression tests
mvn test -Dcucumber.filter.tags="@regression"

# API tests only
mvn test -Dcucumber.filter.tags="@api"

# UI tests only
mvn test -Dcucumber.filter.tags="@ui"

# Critical path tests
mvn test -Dcucumber.filter.tags="@critical"
```

## 🔍 Debugging

### Running Tests in Debug Mode
```bash
# Enable debug logging
mvn test -Dlogback.configurationFile=src/test/resources/logback-debug.xml

# Take screenshots on failure
mvn test -Dscreenshot.on.failure=true

# Keep browser open on failure
mvn test -Dkeep.browser.open=true
```

### Common Issues
1. **WebDriver issues**: Ensure ChromeDriver is compatible with your Chrome version
2. **Timeouts**: Increase wait times in config.properties for slower environments
3. **Port conflicts**: Check if ports 4444, 4445 are available for Selenium Grid

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature`
3. Write tests for your changes
4. Ensure all tests pass: `mvn test`
5. Submit a pull request

### Code Standards
- Follow Page Object Model pattern for UI tests
- Use BDD scenarios for test documentation
- Maintain test data in separate files
- Add appropriate test tags for categorization

## 📚 Additional Resources

- [Selenium Documentation](https://selenium-python.readthedocs.io/)
- [Cucumber Documentation](https://cucumber.io/docs)
- [REST Assured Documentation](https://rest-assured.io/)
- [Allure Framework](https://docs.qameta.io/allure/)

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📞 Support

For questions or issues:
- Create an issue in the GitHub repository
- Contact the QA team at qa-team@company.com
- Check the [Wiki](../../wiki) for detailed documentation