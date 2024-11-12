package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.TransactionType;

@Getter
@Setter
public class TransactionRequest {
    private Double transactionAmount;
    private Long clientId;
    private TransactionType transactionType;
}
