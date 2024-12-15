package sfa.order_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import sfa.order_service.constant.Status;

import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
public class SalesReturn extends BaseEntity{
    @OneToOne
    OrderEntity orderEntity;
    @Temporal(TemporalType.DATE)
    Date returnDate;
    String reason;
    Integer quantity;
    @Enumerated(EnumType.STRING)
    Status status;
    @Enumerated(EnumType.STRING)
    ReturnStatus returnStatus;
    Long memberId;
    Long clientFmcgId;
}
