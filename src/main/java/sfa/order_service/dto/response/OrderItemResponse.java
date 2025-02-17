package sfa.order_service.dto.response;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.DiscountCoupon;
import sfa.order_service.enums.SalesLevel;

@Getter
@Setter
public class OrderItemResponse {
    private Long id;
    private Long orderId;
    private Long productId;
    private int quantity;
    private Double priceOfUnit;
    @Enumerated(EnumType.STRING)
    private DiscountCoupon discountCoupon;
    private Double amountBeforeDiscount;
    private Double amountAfterDiscount;
    @Enumerated(EnumType.STRING)
    private SalesLevel salesLevel;
}
