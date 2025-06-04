Feature: Security Testing for Shopping Cart
  As a security engineer
  I want to verify that the shopping cart application is secure
  So that customer data and transactions are protected from malicious attacks

  Background:
    Given the user is on the shopping cart page

  @security @xss @critical
  Scenario: XSS protection for promo code input
    When I test the promo code input for security vulnerabilities
    Then no XSS vulnerabilities should be found
    And vulnerability details should be logged

  @security @sql-injection @critical
  Scenario: SQL injection protection for promo code input
    Given I am testing security for the input field "#promo-code-input"
    And I am using the submit button "#apply-promo-btn"
    When I test for SQL injection vulnerabilities
    Then no SQL injection vulnerabilities should be found

  @security @xss @high
  Scenario: XSS protection for quantity input fields
    Given the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
    When I test quantity input fields for security vulnerabilities
    Then no XSS vulnerabilities should be found
    And no SQL injection vulnerabilities should be found

  @security @csrf @high
  Scenario: CSRF protection for cart forms
    When I test all cart forms for CSRF protection
    Then CSRF protection should be implemented
    And vulnerability details should be logged

  @security @data-exposure @high
  Scenario: Sensitive data exposure validation
    When I test for sensitive data exposure
    Then no sensitive data should be exposed

  @security @headers @medium
  Scenario: Security headers validation
    When I test for missing security headers
    Then all required security headers should be present

  @security @comprehensive @regression
  Scenario: Comprehensive security scan
    Given the target URL is the current page URL
    When I test for XSS vulnerabilities using the promo code input
    And I test for SQL injection vulnerabilities using the promo code input
    And I test for CSRF protection
    And I test for sensitive data exposure
    And I test for missing security headers
    Then the security scan should pass
    And no more than 2 low-severity vulnerabilities should be found
    And I should get a security report

  @security @input-validation @high
  Scenario Outline: Input validation security testing
    Given I am testing security for the input field "<input_field>"
    And I am using the submit button "<submit_button>"
    When I test for XSS vulnerabilities
    And I test for SQL injection vulnerabilities
    Then no XSS vulnerabilities should be found
    And no SQL injection vulnerabilities should be found

    Examples:
      | input_field          | submit_button         |
      | #promo-code-input    | #apply-promo-btn     |
      | .product-quantity    | .update-quantity-btn |
      | #customer-email      | #update-profile-btn  |
      | #billing-address     | #save-address-btn    |

  @security @authentication @medium
  Scenario: Authentication bypass testing
    # Test for authentication bypass vulnerabilities
    When I attempt to access restricted cart operations without authentication
    Then access should be denied
    And proper error messages should be displayed

  @security @authorization @medium
  Scenario: Authorization flaw testing
    # Test for authorization flaws in cart operations
    Given I am authenticated as a regular user
    When I attempt to access another user's cart
    Then access should be denied
    And no unauthorized data should be exposed

  @security @session @medium
  Scenario: Session security testing
    Given I have an active session
    When I test session security mechanisms
    Then session tokens should be properly protected
    And session fixation should not be possible
    And session hijacking should not be possible

  @security @api @high
  Scenario: API security testing
    When I test API endpoints for security vulnerabilities
    Then API endpoints should have proper authentication
    And API endpoints should have proper authorization
    And API endpoints should validate input properly
    And API endpoints should not expose sensitive information

  @security @file-upload @medium
  Scenario: File upload security testing
    Given the cart supports file uploads for receipts
    When I test file upload functionality for security vulnerabilities
    Then malicious file uploads should be blocked
    And file type validation should be enforced
    And file size limits should be enforced

  @security @error-handling @low
  Scenario: Error handling security testing
    When I cause various error conditions in the cart
    Then error messages should not reveal sensitive information
    And error messages should not expose system details
    And error handling should be consistent

  @security @rate-limiting @medium
  Scenario: Rate limiting protection testing
    When I perform rapid repeated requests to cart endpoints
    Then rate limiting should be enforced
    And excessive requests should be blocked
    And proper rate limit headers should be present

  @security @clickjacking @medium
  Scenario: Clickjacking protection testing
    When I test for clickjacking vulnerabilities
    Then X-Frame-Options header should be present
    And Content-Security-Policy should prevent framing
    And the page should not be frameable by external sites

  @security @content-type @low
  Scenario: Content type security testing
    When I test content type handling
    Then responses should have proper Content-Type headers
    And MIME type sniffing should be prevented
    And Content-Type validation should be enforced

  @security @redirect @medium
  Scenario: Open redirect vulnerability testing
    When I test redirect functionality in the cart
    Then open redirects should not be possible
    And redirect destinations should be validated
    And only trusted domains should be allowed

  @security @cookie @medium
  Scenario: Cookie security testing
    When I examine cart application cookies
    Then cookies should have Secure flag set
    And cookies should have HttpOnly flag set
    And cookies should have appropriate SameSite attribute
    And session cookies should not be persistent

  @security @cors @medium
  Scenario: CORS policy security testing
    When I test Cross-Origin Resource Sharing policies
    Then CORS should be properly configured
    And only trusted origins should be allowed
    And credentials should be handled securely

  @security @penetration @manual
  Scenario: Manual penetration testing validation
    # This scenario serves as a checklist for manual testing
    Given I am performing manual penetration testing
    Then I should verify the following security aspects:
      | Aspect                    | Status |
      | Input validation          | Pass   |
      | Authentication mechanisms | Pass   |
      | Authorization controls    | Pass   |
      | Session management        | Pass   |
      | Error handling            | Pass   |
      | Logging and monitoring    | Pass   |
      | Data encryption           | Pass   |
      | Communication security    | Pass   |