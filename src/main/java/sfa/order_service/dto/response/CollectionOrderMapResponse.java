package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.Status;

@Getter
@Setter
public class CollectionOrderMapResponse {

    private Long collectionId;
    private Long orderId;
    private Double amountForThisOrder;
    private Status status;
    private String memberName;
}
