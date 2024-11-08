package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionResponse {
    private Double topUpAmount;
    private Long clientId;
}
