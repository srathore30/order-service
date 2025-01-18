package sfa.order_service.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.AchievementStatus;
import sfa.order_service.constant.OrderType;

import java.util.Date;

@Entity
@Getter
@Setter
@Table(name = "achievement")
public class AchievementEntity extends BaseEntity{
    private Double salesAmount;
    private OrderType orderType;
    @Temporal(TemporalType.DATE)
    private Date achievementDate;
    @Enumerated(EnumType.STRING)
    private AchievementStatus status;
    private Long memberId;
    private Long beetId;
    private Long outletId;
}
