package sfa.order_service.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SampleInventoryResponse {
    private Long id;
    private Long productId;
    private Integer sampleQuantity;
    private MemberResponse memberRes;
    private ProductRes productRes;
}
