package sfa.order_service.util;

public class DiscountUtil {
    public static Double calculateFinalPrice(Double originalPrice, Double discountAmount) {
        if (originalPrice < 0) {
            throw new IllegalArgumentException("Original price must be non-negative.");
        }
        if (discountAmount < 0 || discountAmount > 100) {
            throw new IllegalArgumentException("Discount amount must be between 0 and 100.");
        }
        Double discount = (originalPrice * (discountAmount / 100.0));
        return originalPrice - discount;
    }
}
