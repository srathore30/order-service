package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "transaction")
public class TransactionEntity extends BaseEntity {
    private Double topUpAmount;
    private Long clientId;
}
