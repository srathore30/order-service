package sfa.order_service.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.enums.SalesLevel;

@Getter
@Setter
public class OrderRequest {
    private Long productId;
    private int quantity;
    @Enumerated(EnumType.STRING)
    private SalesLevel salesLevel;
    private Long clientId;
    private Long memberId;
    private Long outletId;
    private Long beetId;
    private OrderMedium orderMedium;
    private OrderCallStatus orderCallStatus;
}
