package sfa.order_service.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.OrderStatus;

@Getter
@Setter
public class OrderUpdateRequest {
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    Long orderId;
}
