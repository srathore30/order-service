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
    double unitPrice;
    double discountApplied;
    double subTotal;
    double gstAmount;
    double totalPriceWithGst;
    String message;

}
