package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.Status;

@Getter
@Setter
public class CollectionOrderMapRequest {

    private Long collectionId;
    private Long orderId;
    private Double amountForThisOrder;
    private Status status;
}
