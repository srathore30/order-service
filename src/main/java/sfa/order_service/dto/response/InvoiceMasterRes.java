package sfa.order_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.Configs.PreOrPost;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceMasterRes {
    String code;
    PreOrPost preOrPost;
    Long id;
}
