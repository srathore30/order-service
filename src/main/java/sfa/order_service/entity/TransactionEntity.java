package sfa.order_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.TransactionType;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "transaction")
public class TransactionEntity extends BaseEntity {
    private Double transactionAmount;
    private Long clientId;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    private Long orderId;
    private Long memeberId;
    @Temporal(TemporalType.DATE)
    private Date transactionDate;

}
