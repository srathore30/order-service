package sfa.order_service.dto.request;

import jakarta.persistence.Entity;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.constant.Status;
import sfa.order_service.entity.BaseEntity;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SampleReq{
    Long memberId;
    Long doctorId;
    Integer quantity;
    Long productId;
}
