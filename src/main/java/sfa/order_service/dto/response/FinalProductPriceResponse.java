package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FinalProductPriceResponse {
    Long productId;
    int quantity;
    float unitPrice;
    float discountApplied;
    float subTotal;
    float gstAmount;
    float totalPriceWithGst;
    String message;

}
