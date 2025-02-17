package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.DiscountCoupon;
import sfa.order_service.enums.SalesLevel;

@Getter
@Setter
@Entity
@Table(name = "order_items")
public class OrderItems extends BaseEntity {
    private Long orderId;
    private Long productId;
    private int quantity;
    private Double priceOfUnit;
    @Enumerated(EnumType.STRING)
    private DiscountCoupon discountCoupon;
    private Double amountBeforeDiscount;
    private Double amountAfterDiscount;
    @Enumerated(EnumType.STRING)
    private SalesLevel salesLevel;
}
