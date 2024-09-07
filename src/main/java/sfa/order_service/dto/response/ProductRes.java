package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRes {
    String name;
    String sku;
    String unitOfMeasurement;
    Long productId;
    ProductPriceRes productPriceRes;
}
