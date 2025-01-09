package sfa.order_service.dto.request;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
    private Date validFrom;
    private Date validTo;
    private Integer minQuantity;
    private Integer bogoOfferQuantity;  // e.g., Buy 2
    private Integer bogoFreeQuantity;   // e.g., Get 1
}
