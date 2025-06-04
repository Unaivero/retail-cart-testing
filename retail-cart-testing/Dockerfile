FROM maven:3.8.6-openjdk-11-slim

# Install dependencies
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    unzip \
    gnupg \
    software-properties-common \
    && rm -rf /var/lib/apt/lists/*

# Install Google Chrome
RUN wget -q -O - https://dl-ssl.google.com/linux/linux_signing_key.pub | apt-key add - \
    && echo "deb [arch=amd64] http://dl.google.com/linux/chrome/deb/ stable main" >> /etc/apt/sources.list.d/google.list \
    && apt-get update \
    && apt-get install -y google-chrome-stable \
    && rm -rf /var/lib/apt/lists/*

# Install Firefox
RUN apt-get update && apt-get install -y firefox-esr \
    && rm -rf /var/lib/apt/lists/*

# Set up working directory
WORKDIR /app

# Copy Maven files for dependency caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src/ src/

# Create directories for reports and screenshots
RUN mkdir -p target/allure-results target/surefire-reports target/screenshots reports/

# Set environment variables
ENV MAVEN_OPTS="-Xmx1024m -XX:MaxPermSize=256m"
ENV JAVA_OPTS="-Xmx1024m"

# Default command runs all tests
CMD ["mvn", "test", "-Dheadless.mode=true"]

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:4444/wd/hub/status || exit 1