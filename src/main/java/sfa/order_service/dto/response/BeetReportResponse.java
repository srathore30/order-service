package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BeetReportResponse {
    Double totalSales;
    Long totalOrder;
    BeetRespForOrderDto beetRespForOrderDto;
}