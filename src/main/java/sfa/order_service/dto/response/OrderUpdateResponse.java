package sfa.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sfa.order_service.enums.OrderStatus;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderUpdateResponse {
    private Long orderId;
    private OrderStatus status;
    private String message;
}
