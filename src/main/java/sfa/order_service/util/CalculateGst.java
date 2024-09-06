package sfa.order_service.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CalculateGst {
    public static Float calculateGst(Float gst, Float price){
        return (price * gst) / 100;
    }
    public static Float calculateGstAmountFromTotal(int totalPrice, Float gstPercentage) {
        return totalPrice - (totalPrice / (1 + (gstPercentage / 100)));
    }
}
