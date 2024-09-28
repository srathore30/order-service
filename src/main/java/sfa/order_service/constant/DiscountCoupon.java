package sfa.order_service.constant;


public enum DiscountCoupon {

    SPRING20("SPRING20", 20),
    SUMMER25("SUMMER25", 25),
    FALL15("FALL15", 15),
    WINTER30("WINTER30", 30);

    private final String couponCode;
    private final double discountAmount;

    DiscountCoupon(String couponCode, double discountAmount) {
        this.couponCode = couponCode;
        this.discountAmount = discountAmount;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    @Override
    public String toString() {
        return couponCode + ": " + discountAmount + "% off";
    }
}
