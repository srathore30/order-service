package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductSalesResponse {
    Long productId;
    String productName;
    Double totalSales;
    String sku;
    String productImageUrl;
}
