package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.CityType;

@Getter
@Setter
public class BeetRespForOrderDto {
    private Long id;
    private String beet;
    private String address;
    private Long postalCode;
    private String state;
    private CityType cityType;
    private Long regionId;
    private Long stateId;
    private Long cityId;
    private String city;
}