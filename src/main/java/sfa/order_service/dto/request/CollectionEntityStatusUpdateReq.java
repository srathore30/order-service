package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.CollectionStatus;
@Getter
@Setter
public class CollectionEntityStatusUpdateReq {
    private CollectionStatus status;
}
