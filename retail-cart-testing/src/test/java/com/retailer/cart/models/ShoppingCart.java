package com.retailer.cart.models;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShoppingCart {
    private List<Product> products;
    private Map<String, Promotion> appliedPromotions;
    private List<String> errorMessages;
    
    public ShoppingCart() {
        this.products = new ArrayList<>();
        this.appliedPromotions = new HashMap<>();
        this.errorMessages = new ArrayList<>();
    }
    
    public void addProduct(Product product) {
        // Check if product already exists in cart
        for (Product p : products) {
            if (p.getProductId().equals(product.getProductId())) {
                p.setQuantity(p.getQuantity() + product.getQuantity());
                return;
            }
        }
        products.add(product);
    }
    
    public void removeProduct(String productId) {
        products.removeIf(p -> p.getProductId().equals(productId));
    }
    
    public void updateProductQuantity(String productId, int quantity) {
        for (Product p : products) {
            if (p.getProductId().equals(productId)) {
                if (quantity <= 0) {
                    removeProduct(productId);
                } else {
                    p.setQuantity(quantity);
                }
                return;
            }
        }
    }
    
    public List<Product> getProducts() {
        return products;
    }
    
    public Map<String, Promotion> getAppliedPromotions() {
        return appliedPromotions;
    }
    
    public List<String> getErrorMessages() {
        return errorMessages;
    }
    
    public void clearErrorMessages() {
        errorMessages.clear();
    }
    
    public double getSubtotal() {
        double subtotal = 0;
        for (Product product : products) {
            subtotal += product.getSubtotal();
        }
        return subtotal;
    }
    
    public double getTotalDiscount() {
        double totalDiscount = 0;
        double subtotal = getSubtotal();
        
        for (Promotion promotion : appliedPromotions.values()) {
            totalDiscount += promotion.calculateDiscount(subtotal);
        }
        
        return totalDiscount;
    }
    
    public double getFinalPrice() {
        return Math.max(0, getSubtotal() - getTotalDiscount());
    }
    
    public boolean applyPromotion(Promotion promotion) {
        LocalDate currentDate = LocalDate.now();
        
        // Check if promotion is valid and active
        if (promotion == null) {
            errorMessages.add("Invalid promotion code");
            return false;
        }
        
        if (!promotion.isActive(currentDate)) {
            if (currentDate.isBefore(promotion.getStartDate())) {
                errorMessages.add("This promotion is not currently active");
            } else {
                errorMessages.add("This promotion code has expired");
            }
            return false;
        }
        
        // Check compatibility with existing promotions
        for (Promotion existingPromotion : appliedPromotions.values()) {
            if (!existingPromotion.isCompatibleWith(promotion)) {
                errorMessages.add("This promotion cannot be combined with " + existingPromotion.getCode());
                return false;
            }
        }
        
        // Apply promotion
        appliedPromotions.put(promotion.getCode(), promotion);
        return true;
    }
    
    public boolean removePromotion(String promotionCode) {
        return appliedPromotions.remove(promotionCode) != null;
    }
    
    public void clearPromotions() {
        appliedPromotions.clear();
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Shopping Cart:\n");
        
        for (Product product : products) {
            sb.append("  ").append(product.getName())
              .append(" (").append(product.getQuantity()).append(")")
              .append(" - $").append(String.format("%.2f", product.getSubtotal()))
              .append("\n");
        }
        
        sb.append("\nSubtotal: $").append(String.format("%.2f", getSubtotal()));
        
        if (!appliedPromotions.isEmpty()) {
            sb.append("\nApplied Promotions:");
            for (Promotion promotion : appliedPromotions.values()) {
                sb.append("\n  ").append(promotion.getCode())
                  .append(" - ").append(promotion.getDescription());
            }
            sb.append("\nTotal Discount: $").append(String.format("%.2f", getTotalDiscount()));
        }
        
        sb.append("\nFinal Price: $").append(String.format("%.2f", getFinalPrice()));
        
        return sb.toString();
    }
}
