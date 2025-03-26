package sfa.order_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.Configs.PreOrPost;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class InvoiceMaster extends BaseEntity{
    String code;
    @Enumerated(EnumType.STRING)
    PreOrPost preOrPost;
    Integer currentSerialNumber;
}
