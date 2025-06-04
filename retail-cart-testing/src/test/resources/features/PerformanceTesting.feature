Feature: Performance Testing for Shopping Cart
  As a performance engineer
  I want to verify that the shopping cart system meets performance requirements
  So that users have a smooth shopping experience even under load

  Background:
    Given performance monitoring is enabled
    And the maximum average response time is 2000 milliseconds
    And the maximum error rate is 5 percent
    And the maximum 95th percentile response time is 5000 milliseconds

  @performance @api @smoke
  Scenario: API response time validation for cart operations
    When I perform 10 "GET" requests to "/cart/123"
    Then the average response time for "GET /cart/123" should be less than 1000 milliseconds
    And the error rate for "GET /cart/123" should be less than 1 percent
    And the performance report should be generated

  @performance @api @load
  Scenario: Load testing for cart creation
    When I perform 50 "POST" requests to "/cart"
    Then the average response time for "POST /cart" should be less than 1500 milliseconds
    And the 95th percentile response time for "POST /cart" should be less than 3000 milliseconds
    And the throughput for "POST /cart" should be at least 5 requests per second

  @performance @api @stress
  Scenario: Concurrent user simulation for cart operations
    When I perform 20 concurrent "GET" requests to "/cart/456"
    Then the average response time for "GET /cart/456_concurrent" should be less than 2000 milliseconds
    And the error rate for "GET /cart/456_concurrent" should be less than 5 percent

  @performance @api @regression
  Scenario: Performance regression test for add item to cart
    When I perform 30 "POST" requests to "/cart/789/items"
    Then the average response time for "POST /cart/789/items" should be less than 1200 milliseconds
    And the error rate for "POST /cart/789/items" should be less than 2 percent
    And the 95th percentile response time for "POST /cart/789/items" should be less than 2500 milliseconds

  @performance @api @endurance
  Scenario: Endurance testing for cart operations
    Given the maximum average response time is 3000 milliseconds
    When I start monitoring the "endurance_test" operation
    And I perform 100 "GET" requests to "/cart/endurance"
    And I wait 5 seconds
    And I perform 100 "POST" requests to "/cart/endurance/items"
    And I wait 5 seconds
    And I perform 100 "PUT" requests to "/cart/endurance/items/123"
    And I stop monitoring the operation
    Then all performance thresholds should be met

  @performance @api @spike
  Scenario: Spike testing for sudden load increase
    When I perform 5 "GET" requests to "/cart/spike"
    And I perform 50 concurrent "GET" requests to "/cart/spike"
    And I perform 5 "GET" requests to "/cart/spike"
    Then the average response time for "GET /cart/spike" should be less than 2500 milliseconds
    And the average response time for "GET /cart/spike_concurrent" should be less than 4000 milliseconds

  @performance @ui @page-load
  Scenario: UI page load performance validation
    Given the user is on the shopping cart page
    When I start monitoring the "page_load" operation
    And I wait 3 seconds
    And I stop monitoring the operation
    Then the average response time for "page_load" should be less than 3000 milliseconds

  @performance @mixed @realistic
  Scenario: Realistic user journey performance test
    When I start monitoring the "user_journey" operation
    # Simulate browsing
    And I perform 5 "GET" requests to "/products"
    And I wait 2 seconds
    # Add items to cart
    And I perform 3 "POST" requests to "/cart/journey/items"
    And I wait 1 seconds
    # Update quantities
    And I perform 2 "PUT" requests to "/cart/journey/items/456"
    And I wait 1 seconds
    # Apply promotion
    And I perform 1 "POST" requests to "/cart/journey/promotions"
    And I wait 1 seconds
    # Checkout
    And I perform 1 "POST" requests to "/cart/journey/checkout"
    And I stop monitoring the operation
    Then the average response time for "user_journey" should be less than 8000 milliseconds
    And all performance thresholds should be met

  @performance @database @query
  Scenario: Database query performance validation
    When I perform 25 "GET" requests to "/cart/database-heavy"
    Then the average response time for "GET /cart/database-heavy" should be less than 1800 milliseconds
    And the 95th percentile response time for "GET /cart/database-heavy" should be less than 3500 milliseconds
    And the throughput for "GET /cart/database-heavy" should be at least 3 requests per second

  @performance @api @validation
  Scenario Outline: Performance validation across different cart sizes
    When I perform 20 "GET" requests to "/cart/<cart_size>"
    Then the average response time for "GET /cart/<cart_size>" should be less than <max_response_time> milliseconds
    And the error rate for "GET /cart/<cart_size>" should be less than 3 percent

    Examples:
      | cart_size | max_response_time |
      | empty     | 800               |
      | small     | 1200              |
      | medium    | 1800              |
      | large     | 2500              |

  @performance @memory @leak
  Scenario: Memory usage monitoring during extended operations
    Given the maximum average response time is 4000 milliseconds
    When I start monitoring the "memory_test" operation
    And I perform 200 "POST" requests to "/cart/memory-test/items"
    And I stop monitoring the operation
    Then the average response time for "POST /cart/memory-test/items" should be less than 2000 milliseconds
    And the error rate for "POST /cart/memory-test/items" should be less than 1 percent
    # Memory usage should remain stable (would require additional monitoring)

  @performance @cdn @static
  Scenario: Static resource loading performance
    When I perform 30 "GET" requests to "/static/cart.css"
    And I perform 30 "GET" requests to "/static/cart.js"
    Then the average response time for "GET /static/cart.css" should be less than 200 milliseconds
    And the average response time for "GET /static/cart.js" should be less than 300 milliseconds
    And the error rate for "GET /static/cart.css" should be less than 0.1 percent

  @performance @api @timeout
  Scenario: Timeout handling performance test
    Given the maximum error rate is 100 percent
    When I perform 10 "GET" requests to "/cart/timeout-test"
    Then the error rate for "GET /cart/timeout-test" should be less than 100 percent
    # Verify graceful timeout handling

  @performance @security @rate-limit
  Scenario: Rate limiting performance validation
    When I perform 100 "GET" requests to "/cart/rate-limited"
    Then the error rate for "GET /cart/rate-limited" should be greater than 50 percent
    # Expect rate limiting to kick in
    And the average response time for "GET /cart/rate-limited" should be less than 1000 milliseconds