package sfa.order_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.CityType;
import sfa.order_service.constant.Status;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class CityCustomResponse {
    private Long id;
    private String cityName;
    private Long stateId;
    private String cityCode;
    private String cityClass;
    private String stateName;
    private Status cityStatus;
    private CityType cityType;
}
