package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.OrderStatus;
import sfa.order_service.constant.SalesLevelConstant;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "order_entity")
public class OrderEntity extends BaseEntity{
    private int quantity;
    private int price;
    private SalesLevelConstant salesLevel;
    private OrderStatus status;
    private Long productId;
}
