package sfa.order_service.dto.response;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class RegionCustomResponse {
    private Long id;
    private String regionName;
}
