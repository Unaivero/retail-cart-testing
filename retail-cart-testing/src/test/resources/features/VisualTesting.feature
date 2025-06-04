Feature: Visual Regression Testing
  As a QA engineer
  I want to detect visual changes in the shopping cart interface
  So that I can ensure UI consistency across releases

  Background:
    Given the user is on the shopping cart page

  @visual @regression
  Scenario: Full page visual validation of empty cart
    When I capture a full page screenshot for "empty_cart_page"
    Then the full page screenshot should match the baseline

  @visual @regression
  Scenario: Full page visual validation of cart with items
    Given the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
      | P002      | Cotton T-shirt | 19.99 | 2        |
    When I capture a full page screenshot for "cart_with_items"
    Then the full page screenshot should match the baseline

  @visual @regression
  Scenario: Visual validation of cart items container
    Given the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
      | P002      | Cotton T-shirt | 19.99 | 2        |
    When I capture a screenshot of element "#cart-items" for "cart_items_container"
    Then the element "#cart-items" screenshot should match the baseline

  @visual @regression
  Scenario: Visual validation of promotion section
    Given the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
    When the user applies the promotion code "SUMMER25"
    And I capture a screenshot of element "#applied-promos-container" for "promotion_section"
    Then the element "#applied-promos-container" screenshot should match the baseline

  @visual @regression
  Scenario: Visual validation with custom threshold
    Given the cart contains the following items:
      | productId | name           | price | quantity |
      | P003      | Leather Jacket | 199.99| 1        |
    When I capture a full page screenshot for "cart_high_value_item"
    Then the full page screenshot should match the baseline with 2.0% threshold

  @visual @smoke
  Scenario: Visual validation of cart pricing section
    Given the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
      | P002      | Cotton T-shirt | 19.99 | 2        |
    When I capture a screenshot of element ".pricing-section" for "pricing_section"
    Then the element ".pricing-section" screenshot should match the baseline

  @visual @regression
  Scenario: Visual validation after quantity update
    Given the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
    When the user updates the quantity of product "P001" to 3
    And I capture a screenshot of element "#cart-items" for "cart_after_quantity_update"
    Then the element "#cart-items" screenshot should match the baseline

  @visual @regression
  Scenario: Visual validation of error states
    When the user applies the promotion code "INVALID123"
    And I capture a screenshot of element "#error-message" for "error_message_display"
    Then the element "#error-message" screenshot should match the baseline

  @visual @cross-browser
  Scenario Outline: Cross-browser visual validation
    Given the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
    When I capture a full page screenshot for "cart_<browser>"
    Then the full page screenshot should match the baseline with 5.0% threshold

    Examples:
      | browser |
      | chrome  |
      | firefox |
      | edge    |

  @visual @responsive
  Scenario Outline: Responsive design visual validation
    Given the browser viewport is set to <width>x<height>
    And the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
    When I capture a full page screenshot for "cart_<resolution>"
    Then the full page screenshot should match the baseline with 3.0% threshold

    Examples:
      | resolution | width | height |
      | mobile     | 375   | 667    |
      | tablet     | 768   | 1024   |
      | desktop    | 1920  | 1080   |

  @visual @dark-mode
  Scenario: Visual validation in dark mode
    Given the user enables dark mode
    And the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
    When I capture a full page screenshot for "cart_dark_mode"
    Then the full page screenshot should match the baseline

  @visual @accessibility
  Scenario: Visual validation with high contrast mode
    Given the user enables high contrast mode
    And the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        |
    When I capture a full page screenshot for "cart_high_contrast"
    Then the full page screenshot should match the baseline with 10.0% threshold