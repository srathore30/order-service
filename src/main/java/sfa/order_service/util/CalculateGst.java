package sfa.order_service.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CalculateGst {
    public static float calculateGst(float gst, float price){
        return (price * gst) / 100;
    }
    public static float calculateGstAmountFromTotal(float totalPrice, float gstPercentage) {
        return totalPrice - (totalPrice / (1 + (gstPercentage / 100)));
    }
}
