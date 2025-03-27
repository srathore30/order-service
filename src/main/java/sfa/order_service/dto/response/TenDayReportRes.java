package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TenDayReportRes {
    List<OrderResponse> orderResponseList = new ArrayList<>();
    List<SampleRes> sampleResList = new ArrayList<>();
}
