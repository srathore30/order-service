package sfa.order_service.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import sfa.order_service.constant.DiscountType;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DiscountRequest {
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
}
