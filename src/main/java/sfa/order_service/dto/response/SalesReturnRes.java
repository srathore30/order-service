package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.entity.ReturnStatus;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalesReturnRes {
    OrderResponse orderResponse;
    Long id;
    String reason;
    Integer quantity;
    ReturnStatus returnStatus;
}
