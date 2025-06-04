package com.retailer.cart.utils;

import com.retailer.cart.models.Promotion;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Service class to manage promotions for testing purposes.
 * In a real application, this would likely fetch from a database or API.
 */
public class PromotionService {
    private final Map<String, Promotion> promotions;
    
    public PromotionService() {
        this.promotions = new HashMap<>();
        initializePromotions();
    }
    
    private void initializePromotions() {
        // Valid promotions
        LocalDate now = LocalDate.now();
        LocalDate oneMonthAgo = now.minusMonths(1);
        LocalDate oneMonthLater = now.plusMonths(1);
        LocalDate oneWeekAgo = now.minusWeeks(1);
        LocalDate oneWeekLater = now.plusWeeks(1);
        
        // SUMMER25: 25% discount, valid for current period, combinable
        Promotion summer25 = new Promotion(
                "SUMMER25",
                "Summer Collection 25% Off",
                25.0,
                oneMonthAgo,
                oneMonthLater,
                true
        );
        
        // SUMMER10: 10% discount, valid for current period, combinable
        Promotion summer10 = new Promotion(
                "SUMMER10",
                "Summer Special 10% Off",
                10.0,
                oneMonthAgo,
                oneMonthLater,
                true
        );
        
        // NEWCUSTOMER5: 5% discount for new customers, valid for current period, combinable
        Promotion newCustomer5 = new Promotion(
                "NEWCUSTOMER5",
                "New Customer 5% Off",
                5.0,
                oneMonthAgo,
                oneMonthLater,
                true
        );
        
        // SALE30: 30% discount, valid for current period, not combinable
        Promotion sale30 = new Promotion(
                "SALE30",
                "Special Sale 30% Off",
                30.0,
                oneMonthAgo,
                oneMonthLater,
                false
        );
        
        // BUNDLE20: 20% discount for bundles, valid for current period, not combinable
        Promotion bundle20 = new Promotion(
                "BUNDLE20",
                "Bundle Discount 20% Off",
                20.0,
                oneMonthAgo,
                oneMonthLater,
                false
        );
        
        // Invalid promotions
        
        // EXPIRED21: Expired promotion
        Promotion expired21 = new Promotion(
                "EXPIRED21",
                "Expired Promotion 21% Off",
                21.0,
                oneMonthAgo,
                oneWeekAgo,
                true
        );
        
        // SEASONAL22: Not yet active promotion
        Promotion seasonal22 = new Promotion(
                "SEASONAL22",
                "Upcoming Seasonal 22% Off",
                22.0,
                oneWeekLater,
                oneMonthLater,
                true
        );
        
        // Add all promotions to the map
        promotions.put(summer25.getCode(), summer25);
        promotions.put(summer10.getCode(), summer10);
        promotions.put(newCustomer5.getCode(), newCustomer5);
        promotions.put(sale30.getCode(), sale30);
        promotions.put(bundle20.getCode(), bundle20);
        promotions.put(expired21.getCode(), expired21);
        promotions.put(seasonal22.getCode(), seasonal22);
        
        // Add a non-existent promotion code for testing
        promotions.put("INVALID123", null);
    }
    
    public Promotion getPromotionByCode(String code) {
        return promotions.getOrDefault(code, null);
    }
    
    public boolean isValidPromotion(String code) {
        Promotion promotion = getPromotionByCode(code);
        return promotion != null && promotion.isActive(LocalDate.now());
    }
}
