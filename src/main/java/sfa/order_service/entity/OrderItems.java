package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItems extends BaseEntity {
    private Long orderId;
    private Long productId;
    private int quantity;
    private Double price;
    private Double discountApplied;;
    private Double totalAmount;
}
