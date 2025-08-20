package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class LocationBulkRes {
    List<CityResponse> cityList;
    List<StateResponse> stateList;
    List<RegionResponse> regionList;
    List<ClientFMCGResponse> clientFMCGResponseList;
}
