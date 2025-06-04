package com.retailer.cart.pages;

import com.retailer.cart.models.Product;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import com.retailer.cart.utils.ConfigReader;

import java.util.ArrayList;
import java.util.List;

public class ShoppingCartPage extends BasePage {
    
    @FindBy(id = "cart-items")
    private WebElement cartItemsContainer;
    
    @FindBy(id = "promo-code-input")
    private WebElement promoCodeInput;
    
    @FindBy(id = "apply-promo-btn")
    private WebElement applyPromoButton;
    
    @FindBy(id = "subtotal-value")
    private WebElement subtotalValue;
    
    @FindBy(id = "discount-value")
    private WebElement discountValue;
    
    @FindBy(id = "final-price-value")
    private WebElement finalPriceValue;
    
    @FindBy(id = "applied-promos-container")
    private WebElement appliedPromosContainer;
    
    @FindBy(id = "error-message")
    private WebElement errorMessage;

    @FindBy(id = "empty-cart-message") // Assuming this ID for the empty cart message element
    private WebElement emptyCartMessageArea;
    
    public ShoppingCartPage(WebDriver driver) {
        super(driver);
    }
    
    public void navigateToCart() {
        driver.get(ConfigReader.getBaseUrl());
    }
    
    public void applyPromoCode(String promoCode) {
        typeIntoField(promoCodeInput, promoCode);
        clickElement(applyPromoButton);
    }
    
    public List<Product> getCartItems() {
        List<Product> products = new ArrayList<>();
        List<WebElement> productElements = cartItemsContainer.findElements(By.cssSelector(".cart-item"));
        
        for (WebElement productElement : productElements) {
            String productId = productElement.getAttribute("data-product-id");
            String name = getElementText(productElement.findElement(By.cssSelector(".product-name")));
            double price = Double.parseDouble(getElementText(productElement.findElement(By.cssSelector(".product-price")))
                    .replace("$", ""));
            int quantity = Integer.parseInt(productElement.findElement(By.cssSelector(".product-quantity"))
                    .getAttribute("value")); // Kept as getAttribute("value") for input field
            
            products.add(new Product(productId, name, price, quantity));
        }
        
        return products;
    }
    
    public double getSubtotal() {
        String subtotal = getElementText(subtotalValue).replace("$", "");
        return Double.parseDouble(subtotal);
    }
    
    public double getDiscount() {
        String discount = getElementText(discountValue).replace("$", "");
        return Double.parseDouble(discount);
    }
    
    public double getFinalPrice() {
        String finalPrice = getElementText(finalPriceValue).replace("$", "");
        return Double.parseDouble(finalPrice);
    }
    
    public List<String> getAppliedPromoCodes() {
        List<String> promoCodes = new ArrayList<>();
        List<WebElement> promoElements = appliedPromosContainer.findElements(By.cssSelector(".promo-code"));
        
        for (WebElement promoElement : promoElements) {
            promoCodes.add(getElementText(promoElement));
        }
        
        return promoCodes;
    }
    
    public boolean isErrorMessageDisplayed() {
        return isElementPresent(errorMessage);
    }
    
    public String getErrorMessage() {
        if (isElementPresent(errorMessage)) {
            return getElementText(errorMessage);
        }
        return "";
    }
    
    public void updateProductQuantity(String productId, int quantity) {
        WebElement quantityInput = driver.findElement(
                By.cssSelector(".cart-item[data-product-id='" + productId + "'] .product-quantity"));
        typeIntoField(quantityInput, String.valueOf(quantity));
        
        WebElement updateButton = driver.findElement(
                By.cssSelector(".cart-item[data-product-id='" + productId + "'] .update-quantity-btn"));
        clickElement(updateButton);
    }
    
    public void removeProduct(String productId) {
        WebElement removeButton = driver.findElement(
                By.cssSelector(".cart-item[data-product-id='" + productId + "'] .remove-item-btn"));
        clickElement(removeButton);
    }
    
    public void removePromoCode(String promoCode) {
        WebElement removeButton = driver.findElement(
                By.cssSelector(".promo-code[data-code='" + promoCode + "'] .remove-promo-btn"));
        clickElement(removeButton);
    }

    // Getter for the error message WebElement, used in ShoppingCartSteps
    public WebElement getErrorMessageElement() {
        return errorMessage;
    }

    public boolean isEmptyCartMessageDisplayed() {
        return isElementPresent(emptyCartMessageArea);
    }

    public String getEmptyCartMessageText() {
        if (isEmptyCartMessageDisplayed()) {
            return getElementText(emptyCartMessageArea);
        }
        return "";
    }
}
