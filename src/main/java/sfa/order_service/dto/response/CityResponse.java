package sfa.order_service.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.Status;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class CityResponse {
    private Long id;
    private String cityName;
    private Long stateId;
    private String cityCode;
    private String cityClass;
    private String stateName;
    private Status cityStatus;
}
