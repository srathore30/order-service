package sfa.order_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.enums.OrderStatus;
import sfa.order_service.enums.SalesLevel;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "order_entity")
public class OrderEntity extends BaseEntity {
    private int quantity;
    private Double price;
    @Enumerated(EnumType.STRING)
    private SalesLevel salesLevel;
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    private String invoiceNumber;
    private Long productId;
    @Temporal(TemporalType.DATE)
    private Date orderCreatedDate;
    @Enumerated(EnumType.STRING)
    private OrderMedium orderMedium;
    @Enumerated(EnumType.STRING)
    private OrderCallStatus orderCallStatus;
    private Long clientFmcgId;
    private Long memberId;
    private Long outletId;
    private Long beetId;
}
