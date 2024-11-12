package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.TransactionType;

@Entity
@Getter
@Setter
@Table(name = "transaction")
public class TransactionEntity extends BaseEntity {
    private Double transactionAmount;
    private Long clientId;
    private TransactionType transactionType;
    private Long orderId;
}
