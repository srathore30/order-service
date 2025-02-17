package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.CityType;

@Getter
@Setter
public class MemberGetCityDto {
    private Long id;
    private String cityName;
    private String cityCode;
    private String cityClass;
    private CityType cityType;
}
