package sfa.order_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.constant.DiscountCoupon;
import sfa.order_service.constant.SalesLevelConstant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinalProductPriceRequest {
    Long productId;
    int quantity;
    SalesLevelConstant salesLevelConstant;
    DiscountCoupon discountCoupon;
}
