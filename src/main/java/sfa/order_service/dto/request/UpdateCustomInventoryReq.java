package sfa.order_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateCustomInventoryReq {
    Long productId;
    Long clientFmcgId;
    Long memberId;
    Integer quantity;
}
