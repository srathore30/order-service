package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.TransactionType;

@Getter
@Setter
public class TransactionResponse {
    private Double topUpAmount;
    private Long clientId;
    private TransactionType transactionType;
}
