package sfa.order_service.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.TransactionType;

@Getter
@Setter
public class TransactionRequest {
    private Double transactionAmount;
    private Long clientId;
    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;
    private Long orderId;
}
