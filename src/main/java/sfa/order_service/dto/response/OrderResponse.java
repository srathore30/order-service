package sfa.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sfa.order_service.constant.DiscountCoupon;
import sfa.order_service.constant.OrderStatus;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private OrderStatus status;
    private float totalPrice;
    private float gstAmount;
    private float totalPriceWithGst;
    private Date estimatedDeliveryDate;
}

