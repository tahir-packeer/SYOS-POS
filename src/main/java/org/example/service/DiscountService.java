package org.example.service;

import org.example.config.ConfigManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for handling discount calculations and rules
 */
public class DiscountService {
    private static final Logger logger = LoggerFactory.getLogger(DiscountService.class);

    private final ConfigManager configManager;

    public DiscountService() {
        this.configManager = ConfigManager.getInstance();
    }

    /**
     * Calculate automatic discount based on subtotal
     * 
     * @deprecated Automatic discounts are disabled. Use manual discount input only.
     */
    @Deprecated
    public double calculateAutomaticDiscount(double subtotal) {
        // Automatic discounts are disabled - return 0
        return 0.0;
    }

    /**
     * Validate discount amount
     */
    public boolean isValidDiscount(double discount, double subtotal) {
        if (discount < 0) {
            logger.warn("Invalid discount: negative value {}", discount);
            return false;
        }

        if (discount > subtotal) {
            logger.warn("Invalid discount: exceeds subtotal {} > {}", discount, subtotal);
            return false;
        }

        return true;
    }

    /**
     * Get discount percentage for a given discount amount and subtotal
     */
    public double getDiscountPercentage(double discount, double subtotal) {
        if (subtotal <= 0)
            return 0.0;
        return round((discount / subtotal) * 100.0, 1);
    }

    /**
     * Get discount information for display
     */
    public String getDiscountInfo(double subtotal) {
        return "Manual discount only - enter amount when prompted";
    }

    /**
     * Get available discount tiers information
     */
    public String getDiscountTiersInfo() {
        return "Manual discounts only - no automatic tiers available";
    }

    private double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();
        java.math.BigDecimal bd = java.math.BigDecimal.valueOf(value);
        bd = bd.setScale(places, java.math.RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
