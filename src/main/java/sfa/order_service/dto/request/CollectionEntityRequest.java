package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.CollectionStatus;
import sfa.order_service.enums.PaymentMode;
import java.util.Date;

@Getter
@Setter
public class CollectionEntityRequest {
    private Double collectedAmount;
    private Long outletId;
    private Long memberId;
    private Long orderId;
    private Date collectionDate;
    private PaymentMode paymentMode;
    private String transactionRef;
    private CollectionStatus status;
}
