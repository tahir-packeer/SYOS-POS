package org.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DiscountService
 */
public class DiscountServiceTest {

    private DiscountService discountService;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService();
    }

    @Test
    void testCalculateAutomaticDiscount() {
        // Automatic discounts are disabled - all should return 0
        assertEquals(0.0, discountService.calculateAutomaticDiscount(500), 0.01);
        assertEquals(0.0, discountService.calculateAutomaticDiscount(1000), 0.01);
        assertEquals(0.0, discountService.calculateAutomaticDiscount(2000), 0.01);
        assertEquals(0.0, discountService.calculateAutomaticDiscount(5000), 0.01);
        assertEquals(0.0, discountService.calculateAutomaticDiscount(10000), 0.01);
    }

    @Test
    void testIsValidDiscount() {
        // Valid discounts
        assertTrue(discountService.isValidDiscount(0.0, 1000));
        assertTrue(discountService.isValidDiscount(100.0, 1000));
        assertTrue(discountService.isValidDiscount(1000.0, 1000));

        // Invalid discounts
        assertFalse(discountService.isValidDiscount(-10.0, 1000));
        assertFalse(discountService.isValidDiscount(1500.0, 1000));
    }

    @Test
    void testGetDiscountPercentage() {
        assertEquals(0.0, discountService.getDiscountPercentage(0.0, 1000), 0.1);
        assertEquals(10.0, discountService.getDiscountPercentage(100.0, 1000), 0.1);
        assertEquals(50.0, discountService.getDiscountPercentage(500.0, 1000), 0.1);
        assertEquals(100.0, discountService.getDiscountPercentage(1000.0, 1000), 0.1);
    }

    @Test
    void testGetDiscountInfo() {
        String info = discountService.getDiscountInfo(1000);
        assertTrue(info.contains("Manual discount only"));
    }
}
