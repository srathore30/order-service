package sfa.order_service.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.Configs.PreOrPost;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceMasterReq {
    String code;
    Integer currentSerialNumber;
    PreOrPost preOrPost;
}
