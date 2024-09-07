package sfa.order_service.dto.request;

import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.SalesLevel;

@Getter
@Setter
public class OrderRequest {
    private Long productId;
    private int quantity;
    private SalesLevel salesLevel;
}
