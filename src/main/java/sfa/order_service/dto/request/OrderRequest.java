package sfa.order_service.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.BundleType;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.enums.SalesLevel;

import java.util.Date;

@Getter
@Setter
public class OrderRequest {
    private Long productId;
    private int quantity;
    private Long beetLogId;
    private Long doctorLogId;
    private Long clientLogId;
    @Enumerated(EnumType.STRING)
    private SalesLevel salesLevel;
    private Long clientId;
    private BundleType bundleType;
    private Long memberId;
    private Long outletId;
    private Long beetId;
    private OrderMedium orderMedium;
    private OrderCallStatus orderCallStatus;
    private String remarks;
    private String discountCode;
}
