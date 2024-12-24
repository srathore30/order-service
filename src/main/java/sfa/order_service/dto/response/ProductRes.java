package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.constant.BundleType;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRes {
    String name;
    String sku;
    Double bundleSize;
    String imageUrl;
    String unitOfMeasurement;
    Long productId;
    ProductPriceRes productPriceRes;
}
