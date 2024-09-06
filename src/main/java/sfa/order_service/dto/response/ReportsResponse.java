package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.constant.SalesLevelConstant;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportsResponse {
    int totalSales;
    int totalGstCollected;
    int totalOrder;
    List<TopSellingProductRes> topSellingProductList;
}
