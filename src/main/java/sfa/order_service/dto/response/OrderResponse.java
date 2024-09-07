package sfa.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sfa.order_service.enums.OrderStatus;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    private OrderStatus status;
    private Double totalPrice;
    private Double gstAmount;
    private Double totalPriceWithGst;
    private Date orderCreatedDate;
}

