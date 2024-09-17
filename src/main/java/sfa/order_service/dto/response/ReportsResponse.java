package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReportsResponse {
    Double totalSales;
    Double totalGstCollected;
    int totalOrder;
    List<TopSellingProductRes> topSellingProductList;
}
