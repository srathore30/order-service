package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.entity.ReturnStatus;
import sfa.order_service.enums.SalesLevel;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalesReturnRes {
    OrderResponse orderResponse;
    Long id;
    SalesLevel salesLevel;
    String reason;
    Integer quantity;
    ReturnStatus returnStatus;
}
