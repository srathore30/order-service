package sfa.order_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.constant.DiscountCoupon;
import sfa.order_service.enums.SalesLevel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinalProductPriceRequest {
    Long productId;
    int quantity;
    SalesLevel salesLevelConstant;
    DiscountCoupon discountCoupon;
}
