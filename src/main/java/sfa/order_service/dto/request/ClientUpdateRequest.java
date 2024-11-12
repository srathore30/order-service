package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientUpdateRequest {
    private Long id;
    private Double topUpBalance;
    private String clientCode;
}
