package com.retailer.cart.models;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

public class Promotion {
    private String code;
    private String description;
    private double discountPercentage;
    private double discountAmount;
    private boolean isPercentage;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean isCombinableWithOthers;
    private Set<String> incompatiblePromotions;
    
    public Promotion() {
        this.incompatiblePromotions = new HashSet<>();
    }
    
    public Promotion(String code, String description, double discountPercentage, 
                     LocalDate startDate, LocalDate endDate, boolean isCombinableWithOthers) {
        this.code = code;
        this.description = description;
        this.discountPercentage = discountPercentage;
        this.isPercentage = true;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCombinableWithOthers = isCombinableWithOthers;
        this.incompatiblePromotions = new HashSet<>();
    }
    
    public Promotion(String code, String description, double discountAmount, boolean isPercentage,
                     LocalDate startDate, LocalDate endDate, boolean isCombinableWithOthers) {
        this.code = code;
        this.description = description;
        if (isPercentage) {
            this.discountPercentage = discountAmount;
        } else {
            this.discountAmount = discountAmount;
        }
        this.isPercentage = isPercentage;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCombinableWithOthers = isCombinableWithOthers;
        this.incompatiblePromotions = new HashSet<>();
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public double getDiscountPercentage() {
        return discountPercentage;
    }
    
    public void setDiscountPercentage(double discountPercentage) {
        this.discountPercentage = discountPercentage;
        this.isPercentage = true;
    }
    
    public double getDiscountAmount() {
        return discountAmount;
    }
    
    public void setDiscountAmount(double discountAmount) {
        this.discountAmount = discountAmount;
        this.isPercentage = false;
    }
    
    public boolean isPercentage() {
        return isPercentage;
    }
    
    public void setPercentage(boolean percentage) {
        isPercentage = percentage;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
    
    public LocalDate getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
    
    public boolean isCombinableWithOthers() {
        return isCombinableWithOthers;
    }
    
    public void setCombinableWithOthers(boolean combinableWithOthers) {
        isCombinableWithOthers = combinableWithOthers;
    }
    
    public Set<String> getIncompatiblePromotions() {
        return incompatiblePromotions;
    }
    
    public void addIncompatiblePromotion(String promotionCode) {
        this.incompatiblePromotions.add(promotionCode);
    }
    
    public boolean isActive(LocalDate currentDate) {
        return !currentDate.isBefore(startDate) && !currentDate.isAfter(endDate);
    }
    
    public boolean isCompatibleWith(Promotion otherPromotion) {
        if (!this.isCombinableWithOthers || !otherPromotion.isCombinableWithOthers()) {
            return false;
        }
        return !this.incompatiblePromotions.contains(otherPromotion.getCode()) &&
               !otherPromotion.getIncompatiblePromotions().contains(this.code);
    }
    
    public double calculateDiscount(double subtotal) {
        if (isPercentage) {
            return subtotal * (discountPercentage / 100.0);
        } else {
            return Math.min(discountAmount, subtotal);
        }
    }
    
    @Override
    public String toString() {
        return "Promotion{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", discount=" + (isPercentage ? discountPercentage + "%" : "$" + discountAmount) +
                ", active=" + isActive(LocalDate.now()) +
                ", combinable=" + isCombinableWithOthers +
                '}';
    }
}
