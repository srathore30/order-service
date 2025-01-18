package sfa.order_service.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sfa.order_service.constant.OrderType;
import sfa.order_service.constant.TargetStatus;

import java.util.Date;

@Getter
@Setter
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "target")
public class TargetEntity extends BaseEntity{
    private Double targetAmount;
    @Enumerated(EnumType.STRING)
    private OrderType orderType;
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Temporal(TemporalType.DATE)
    private Date endDate;
    @Enumerated(EnumType.STRING)
    private TargetStatus status;
    private Long memberId;
    private Long outletId;
    private Long beetId;
}
