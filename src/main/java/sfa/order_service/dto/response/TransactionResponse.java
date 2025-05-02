package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.TransactionType;

import java.util.Date;

@Getter
@Setter
public class TransactionResponse {
    private Double topUpAmount;
    private Long clientId;
    private TransactionType transactionType;
    private Long orderId;
    private Long memberId;
    private Date transactionDate;

}
