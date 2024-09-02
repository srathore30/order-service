package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.enums.OrderStatus;
import sfa.order_service.enums.SalesLevel;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "order_entity")
public class OrderEntity extends BaseEntity{
    private int quantity;
    private Double price;
    private SalesLevel salesLevel;
    private OrderStatus status;
    private Long productId;
    private Date orderCreatedDate;
}
