package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OutletRespForOrderDto {
    private Long id;
    private String outletName;
    private String outletType;
    private String ownerName;
}
