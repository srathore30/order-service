package sfa.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sfa.order_service.constant.AchievementStatus;
import sfa.order_service.constant.OrderType;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AchievementRes{
    private Long achievementId;
    private Double salesAmount;
    private OrderType orderType;
    private Date achievementDate;
    private AchievementStatus status;
    private Long memberId;
    private Long beetId;
    private Long outletId;
}
