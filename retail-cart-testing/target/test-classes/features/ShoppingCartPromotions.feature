Feature: Shopping Cart Promotion Logic
  As a fashion retail customer
  I want to apply promotional codes to my shopping cart
  So that I can get discounts on my purchases

  Background:
    Given the user is on the shopping cart page
    And the cart contains the following items:
      | productId | name                  | price  | quantity |
      | P001      | Slim Fit Jeans        | 49.99  | 1        |
      | P002      | Cotton T-shirt        | 19.99  | 2        |
      | P003      | Leather Jacket        | 199.99 | 1        |

  Scenario: Apply a single valid discount code
    When the user applies the promotion code "SUMMER25"
    Then a 25% discount should be applied to the cart
    And the cart total should be correctly calculated
    And the discount amount should be displayed
    And the final price should be the original price minus the discount

  Scenario: Apply multiple combinable discount codes
    When the user applies the promotion code "SUMMER10"
    And the user applies the promotion code "NEWCUSTOMER5"
    Then both discounts should be applied to the cart
    And the cart total should reflect the combined discounts
    And the discount breakdown should show each applied promotion

  Scenario: Attempt to apply non-combinable discount codes
    Given the user has applied the promotion code "SALE30"
    When the user attempts to apply the promotion code "BUNDLE20"
    Then an error message should indicate the codes cannot be combined
    And only the first promotion code "SALE30" should remain applied
    And the cart total should reflect only the "SALE30" discount

  Scenario Outline: Apply invalid or expired promotion codes
    When the user applies the promotion code "<promoCode>"
    Then an error message should indicate "<errorMessage>"
    And no discount should be applied to the cart
    And the cart total should remain unchanged

  Scenario: Update product quantity to zero
    Given the user is on the shopping cart page
    # Assuming the cart is pre-populated via UI actions not covered by current Gherkin steps
    # or that the initial state is set up by previous scenarios/tests if run in sequence.
    # For a self-contained test, UI steps to add these items would be needed.
    # We'll proceed assuming P001 and P002 are in the cart with quantity 1 each.
    # Current 'Given the cart contains...' step only updates an internal model, not UI.
    When the user updates the quantity of product "P001" to 0
    Then product "P001" should not be present in the cart items list
    And the cart should contain 1 item
    And the cart subtotal should be 19.99

  Scenario: Empty the cart
    Given the user is on the shopping cart page
    # Assuming P001 (Slim Fit Jeans) and P002 (Cotton T-shirt) are in the cart initially.
    # For a self-contained test, UI steps to add these items would be needed first.
    When the user removes product "P001"
    And the user removes product "P002"
    Then the cart should display an "empty cart" message
    And the cart subtotal should be 0.00
    And no discount should be applied to the cart
    And the final price should be 0.00

  Scenario: Apply product-specific promo code to an ineligible cart
    Given the user is on the shopping cart page
    And the cart contains the following items:
      | productId | name           | price | quantity |
      | P001      | Slim Fit Jeans | 49.99 | 1        | # Not a book
      | P002      | Cotton T-shirt | 19.99 | 1        | # Not a book
    When the user applies the promotion code "SAVEONBOOKS" # Assumes this code is for books only
    Then an error message should indicate "promo code not applicable to items in cart"
    And no discount should be applied to the cart
    And the cart total should remain unchanged

    Examples:
      | promoCode   | errorMessage                           |
      | EXPIRED21   | This promotion code has expired        |
      | INVALID123  | Invalid promotion code                 |
      | SEASONAL22  | This promotion is not currently active |

  Scenario: Apply promo code with unmet minimum spend condition
    Given the user is on the shopping cart page
    And the cart contains the following items:
      | productId | name           | price | quantity |
      | P004      | Silk Scarf     | 25.00 | 1        |
      | P005      | Woolen Socks   | 15.00 | 2        | # Subtotal = 25 + 30 = 55.00
    When the user applies the promotion code "MINSPEND10" # Assumes MINSPEND10 requires $75 spend
    Then an error message should indicate "minimum spend requirement not met"
    And no discount should be applied to the cart
    And the cart total should remain unchanged
