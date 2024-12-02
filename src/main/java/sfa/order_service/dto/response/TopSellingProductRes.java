package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TopSellingProductRes {
    Long productId;
    String name;
    int quantitySold;
    Double revenue;
    Double gstAmount;

    String state;
    String region;
    String city;
    BeetRespForOrderDto beetRespForOrderDto;
    OutletRespForOrderDto outletRespForOrderDto;
    ClientFMCGResponse clientFMCGResponse;
}
