// File: src/main/java/org/example/util/ValidationUtils.java
package org.example.util;

import java.util.regex.Pattern;

/**
 * Utility class for common validation operations
 */
public class ValidationUtils {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^\\d{10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final Pattern ITEM_CODE_PATTERN = Pattern.compile("^[A-Z0-9]{3,20}$");

    /**
     * Validate phone number format (10 digits)
     */
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * Validate email format
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Validate item code format
     */
    public static boolean isValidItemCode(String itemCode) {
        return itemCode != null && ITEM_CODE_PATTERN.matcher(itemCode).matches();
    }

    /**
     * Check if string is not null or empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && !str.trim().isEmpty();
    }

    /**
     * Validate positive number
     */
    public static boolean isPositiveNumber(String input) {
        try {
            double value = Double.parseDouble(input);
            return value > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Validate non-negative integer
     */
    public static boolean isNonNegativeInteger(String input) {
        try {
            int value = Integer.parseInt(input);
            return value >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Sanitize input string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().replaceAll("[<>\"'&]", "");
    }
}