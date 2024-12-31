package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.DiscountType;

import java.util.Date;
@Getter
@Setter
@Entity
@Table(name = "discount")
public class DiscountEntity extends BaseEntity {
    private String discountCode;
    private String description;
    private Double percentage;
    private Double fixedAmount;
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    private Long productId;
    private Long outletId;
    private Date validFrom;
    private Date validTo;
    private Integer minQuantity;
}
