package sfa.order_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.CollectionStatus;
import sfa.order_service.enums.PaymentMode;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "collections")
public class CollectionEntity extends BaseEntity{
    private Double collectedAmount;
    private Long outletId;
    private Long memberId;
    private Long orderId;
    private String receiptNumber;
    @Temporal(TemporalType.DATE)
    private Date collectionDate;
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;
    private String transactionRef;
    @Enumerated(EnumType.STRING)
    private CollectionStatus status;
}
