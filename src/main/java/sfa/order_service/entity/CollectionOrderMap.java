package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.Status;

@Getter
@Setter
@Entity
@Table(name = "collection_order_map")
public class CollectionOrderMap extends BaseEntity{
    private Long collectionId;
    private Long orderId;
    private Double amountForThisOrder;
    private Status status;
}
