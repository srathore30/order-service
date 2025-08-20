package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationBulkRes {
    List<CityCustomResponse> cityList;
    List<StateCustomResponse> stateList;
    List<RegionCustomResponse> regionList;
    List<ClientFMCGResponse> clientFMCGResponseList;
}
