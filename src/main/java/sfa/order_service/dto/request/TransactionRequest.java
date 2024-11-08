package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionRequest {
    private Double topUpAmount;
    private Long clientId;
}
