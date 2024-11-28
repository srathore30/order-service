package sfa.order_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.dto.response.OrderUpdateResponse;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderBulkUpdateRequest {
    List<OrderUpdateRequest> orderUpdateRequests;
}
