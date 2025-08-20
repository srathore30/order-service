package sfa.order_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
public class StateCustomResponse {
    private Long id;
    private String stateName;
}
