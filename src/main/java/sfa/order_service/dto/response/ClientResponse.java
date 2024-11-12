package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClientResponse {
    private String clientCode;
    private String clientFirstName;
    private String clientLastName;
    private String email;
    private Long mobile;
    private Double topUpBalance;
}
