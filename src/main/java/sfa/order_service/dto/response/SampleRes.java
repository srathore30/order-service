package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SampleRes {
    MemberResponse memberResponse;
    DoctorRes doctorRes;
    Date sampleDate;
    Long id;
    Integer quantity;
    ProductRes productRes;
}
