package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.SalesLevel;

@Getter
@Setter
public class InventoryUpdateRequest {
    private SalesLevel salesLevel;
    private Long productId;
    private Long quantitySold;
    private Long clientId;
}
