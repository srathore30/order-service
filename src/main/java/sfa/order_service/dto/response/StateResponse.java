package sfa.order_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.prism.mr.model.Region;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")

public class StateResponse {
    private Long id;
    private String stateName;
    private RegionResponse regionEntity;
    @JsonIgnore
    private Region region;
}
