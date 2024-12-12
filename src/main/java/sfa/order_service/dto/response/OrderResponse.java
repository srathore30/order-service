package sfa.order_service.dto.response;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.enums.OrderStatus;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private Long orderId;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private Double totalPrice;
    private Long productId;
    private Integer quantity;
    private Double gstAmount;
    private String invoiceNumber;
    private OrderMedium orderMedium;
    private OrderCallStatus orderCallStatus;
    private Double totalPriceWithGst;
    private Date orderCreatedDate;
    private Long clientId;
    private Long memberId;
    private String clientName;
    private Double clientBalanceAmount;
    private String memberName;
    private BeetRespForOrderDto beetRespForOrderDto;
    private OutletRespForOrderDto outletRespForOrderDto;
    private ClientFMCGResponse clientFMCGResponse;
    private MemberResponse memberResponse;
}
