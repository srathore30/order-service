package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BeetRespForOrderDto {
    private Long id;
    private String beet;
    private String address;
    private Long postalCode;
    private String state;
    private String city;
}