package com.retailer.cart.steps;

import com.retailer.cart.models.Product;
import com.retailer.cart.models.Promotion;
import com.retailer.cart.models.ShoppingCart;
import com.retailer.cart.pages.ShoppingCartPage;
import com.retailer.cart.utils.DriverManager;
import com.retailer.cart.utils.PromotionService;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.SoftAssertions;
import org.openqa.selenium.WebDriver;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

public class ShoppingCartSteps {
    
    private WebDriver driver;
    private ShoppingCartPage cartPage;
    private ShoppingCart cart;
    private PromotionService promotionService;
    
    @Before
    public void setup() {
        driver = DriverManager.getDriver();
        cartPage = new ShoppingCartPage(driver);
        cart = new ShoppingCart();
        promotionService = new PromotionService();
    }
    
    @After
    public void tearDown() {
        DriverManager.quitDriver();
    }
    
    @Given("the user is on the shopping cart page")
    public void theUserIsOnTheShoppingCartPage() {
        cartPage.navigateToCart();
    }
    
    @Given("the cart contains the following items:")
    public void theCartContainsTheFollowingItems(DataTable dataTable) {
        List<Map<String, String>> items = dataTable.asMaps();
        
        for (Map<String, String> item : items) {
            String productId = item.get("productId");
            String name = item.get("name");
            double price = Double.parseDouble(item.get("price"));
            int quantity = Integer.parseInt(item.get("quantity"));
            
            Product product = new Product(productId, name, price, quantity);
            cart.addProduct(product);
            
            // In a real scenario, we would interact with the UI to add these products
            // For this test, we'll assume the products are already in the cart
        }
        
        // Verify that the products in the UI match our model
        List<Product> uiProducts = cartPage.getCartItems();
        assertThat(uiProducts).hasSameSizeAs(cart.getProducts());
        
        double expectedSubtotal = cart.getSubtotal();
        double actualSubtotal = cartPage.getSubtotal();
        assertThat(actualSubtotal).isCloseTo(expectedSubtotal, within(0.01));
    }
    
    @When("the user applies the promotion code {string}")
    public void theUserAppliesThePromotionCode(String promoCode) {
        cartPage.applyPromoCode(promoCode);
        
        // Update our model with the same promotion
        Promotion promotion = promotionService.getPromotionByCode(promoCode);
        if (promotion != null) {
            cart.applyPromotion(promotion);
        }
    }
    
    @When("the user attempts to apply the promotion code {string}")
    public void theUserAttemptsToApplyThePromotionCode(String promoCode) {
        cartPage.applyPromoCode(promoCode);
        
        // Try to apply in our model as well
        Promotion promotion = promotionService.getPromotionByCode(promoCode);
        if (promotion != null) {
            cart.applyPromotion(promotion);
        }
    }
    
    @Given("the user has applied the promotion code {string}")
    public void theUserHasAppliedThePromotionCode(String promoCode) {
        cartPage.applyPromoCode(promoCode);
        
        // Update our model with the same promotion
        Promotion promotion = promotionService.getPromotionByCode(promoCode);
        if (promotion != null) {
            cart.applyPromotion(promotion);
        }
        
        // Verify the promotion was applied
        List<String> appliedCodes = cartPage.getAppliedPromoCodes();
        assertThat(appliedCodes).contains(promoCode);
    }
    
    @Then("a {int}% discount should be applied to the cart")
    public void aDiscountShouldBeAppliedToTheCart(int discountPercentage) {
        double subtotal = cartPage.getSubtotal();
        double expectedDiscount = subtotal * (discountPercentage / 100.0);
        double actualDiscount = cartPage.getDiscount();
        
        assertThat(actualDiscount).isCloseTo(expectedDiscount, within(0.01));
    }
    
    @Then("the cart total should be correctly calculated")
    public void theCartTotalShouldBeCorrectlyCalculated() {
        double subtotal = cartPage.getSubtotal();
        double discount = cartPage.getDiscount();
        double expectedFinalPrice = subtotal - discount;
        double actualFinalPrice = cartPage.getFinalPrice();
        
        assertThat(actualFinalPrice).isCloseTo(expectedFinalPrice, within(0.01));
    }
    
    @Then("the discount amount should be displayed")
    public void theDiscountAmountShouldBeDisplayed() {
        double discount = cartPage.getDiscount();
        assertThat(discount).isGreaterThan(0);
    }
    
    @Then("the final price should be the original price minus the discount")
    public void theFinalPriceShouldBeTheOriginalPriceMinusTheDiscount() {
        double subtotal = cartPage.getSubtotal();
        double discount = cartPage.getDiscount();
        double expectedFinalPrice = subtotal - discount;
        double actualFinalPrice = cartPage.getFinalPrice();
        
        assertThat(actualFinalPrice).isCloseTo(expectedFinalPrice, within(0.01));
    }
    
    @Then("both discounts should be applied to the cart")
    public void bothDiscountsShouldBeAppliedToTheCart() {
        List<String> appliedCodes = cartPage.getAppliedPromoCodes();
        assertThat(appliedCodes).hasSize(2);
    }
    
    @Then("the cart total should reflect the combined discounts")
    public void theCartTotalShouldReflectTheCombinedDiscounts() {
        double subtotal = cartPage.getSubtotal();
        double discount = cartPage.getDiscount();
        double expectedFinalPrice = subtotal - discount;
        double actualFinalPrice = cartPage.getFinalPrice();
        
        assertThat(actualFinalPrice).isCloseTo(expectedFinalPrice, within(0.01));
        assertThat(discount).isGreaterThan(0);
    }
    
    @Then("the discount breakdown should show each applied promotion")
    public void theDiscountBreakdownShouldShowEachAppliedPromotion() {
        List<String> appliedCodes = cartPage.getAppliedPromoCodes();
        assertThat(appliedCodes).hasSize(cart.getAppliedPromotions().size());
    }
    
    @Then("an error message should indicate the codes cannot be combined")
    public void anErrorMessageShouldIndicateTheCodesCannotBeCombined() {
        assertThat(cartPage.isErrorMessageDisplayed()).isTrue();
        String errorMsg = cartPage.getErrorMessage();
        assertThat(errorMsg).containsIgnoringCase("cannot be combined");
    }
    
    @Then("only the first promotion code {string} should remain applied")
    public void onlyTheFirstPromotionCodeShouldRemainApplied(String promoCode) {
        List<String> appliedCodes = cartPage.getAppliedPromoCodes();
        assertThat(appliedCodes).containsExactly(promoCode);
    }
    
    @Then("the cart total should reflect only the {string} discount")
    public void theCartTotalShouldReflectOnlyTheDiscount(String promoCode) {
        Promotion promotion = promotionService.getPromotionByCode(promoCode);
        double subtotal = cartPage.getSubtotal();
        double expectedDiscount = promotion.calculateDiscount(subtotal);
        double actualDiscount = cartPage.getDiscount();
        
        assertThat(actualDiscount).isCloseTo(expectedDiscount, within(0.01));
    }
    
    @Then("an error message should indicate {string}")
    public void anErrorMessageShouldIndicate(String errorMessage) {
        assertThat(cartPage.isErrorMessageDisplayed()).isTrue();
        String actualError = cartPage.getErrorMessage();
        assertThat(actualError).containsIgnoringCase(errorMessage);
    }
    
    @Then("no discount should be applied to the cart")
    public void noDiscountShouldBeAppliedToTheCart() {
        double discount = cartPage.getDiscount();
        assertThat(discount).isCloseTo(0, within(0.01));
    }
    
    @Then("the cart total should remain unchanged")
    public void theCartTotalShouldRemainUnchanged() {
        double subtotal = cartPage.getSubtotal();
        double finalPrice = cartPage.getFinalPrice();
        assertThat(finalPrice).isCloseTo(subtotal, within(0.01));
    }

    @When("the user updates the quantity of product {string} to {int}")
    public void theUserUpdatesTheQuantityOfProductTo(String productId, int quantity) {
        cartPage.updateProductQuantity(productId, quantity);
        // Update internal cart model for consistency if needed for subsequent steps
        // For now, focusing on UI validation
    }

    @Then("product {string} should not be present in the cart items list")
    public void productShouldNotBePresentInTheCartItemsList(String productId) {
        List<Product> uiCartItems = cartPage.getCartItems();
        assertThat(uiCartItems.stream().anyMatch(p -> p.getProductId().equals(productId)))
                .as("Product " + productId + " should not be in the cart")
                .isFalse();
    }

    @Then("the cart should contain {int} item(?:s)?") // Regex to make 's' optional
    public void theCartShouldContainItems(int expectedItemCount) {
        List<Product> uiCartItems = cartPage.getCartItems();
        assertThat(uiCartItems.size())
                .as("Cart should contain " + expectedItemCount + " item(s)")
                .isEqualTo(expectedItemCount);
    }

    @Then("the cart subtotal should be {double}")
    public void theCartSubtotalShouldBe(double expectedSubtotal) {
        double actualSubtotal = cartPage.getSubtotal();
        assertThat(actualSubtotal)
                .as("Cart subtotal should be " + expectedSubtotal)
                .isCloseTo(expectedSubtotal, within(0.01));
    }

    @When("the user removes product {string}")
    public void theUserRemovesProduct(String productId) {
        cartPage.removeProduct(productId);
    }

    @Then("the cart should display an {string} message")
    public void theCartShouldDisplayAnMessage(String expectedMessage) {
        String actualMessage = "";
        boolean messageFound = false;

        if (expectedMessage.toLowerCase().contains("empty cart")) {
            if (cartPage.isEmptyCartMessageDisplayed()) {
                actualMessage = cartPage.getEmptyCartMessageText();
                messageFound = true;
            } else if (cartPage.isErrorMessageDisplayed()) { // Fallback to generic error if specific empty cart message not found
                actualMessage = cartPage.getErrorMessage();
                messageFound = true;
            }
        } else { // For other generic error messages
            if (cartPage.isErrorMessageDisplayed()) {
                actualMessage = cartPage.getErrorMessage();
                messageFound = true;
            }
        }

        assertThat(messageFound)
            .as("Expected a message element (either empty cart or generic error) to be present.")
            .isTrue();
        assertThat(actualMessage).containsIgnoringCase(expectedMessage);
    }

    @Then("the final price should be {double}")
    public void theFinalPriceShouldBe(double expectedFinalPrice) {
        double actualFinalPrice = cartPage.getFinalPrice();
        assertThat(actualFinalPrice)
                .as("Final price should be " + expectedFinalPrice)
                .isCloseTo(expectedFinalPrice, within(0.01));
    }
}
