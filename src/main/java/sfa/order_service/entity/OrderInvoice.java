package sfa.order_service.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.enums.SalesLevel;

import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderInvoice extends BaseEntity{
    @OneToMany
    @JsonManagedReference
    List<OrderEntity> orderEntityList;
    @Enumerated(EnumType.STRING)
    SalesLevel salesLevel;
    Date invoiceDate;
    String invoiceNumber;
    Long memberId;
    Long clientFmcgId;
    Long outletId;
    Long beetId;
}
