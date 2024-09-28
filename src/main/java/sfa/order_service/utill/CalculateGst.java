package sfa.order_service.utill;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CalculateGst {
    public static Double calculateGst(Double gst, Double price){
        return (price * gst) / 100;
    }
    public static Double calculateGstAmountFromTotal(Double totalPrice, Double gstPercentage) {
        return totalPrice - (totalPrice / (1 + (gstPercentage / 100)));
    }
}
