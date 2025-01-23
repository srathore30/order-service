package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.constant.BundleType;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SampleRes {
    MemberResponse memberResponse;
    DoctorRes doctorRes;
    OutletRespForOrderDto outletRespForOrderDto;
    ClientFMCGResponse clientFMCGResponse;
    Date sampleDate;
    Long id;
    BundleType bundleType;
    Integer quantity;
    ProductRes productRes;
}
