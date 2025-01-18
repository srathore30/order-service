package sfa.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sfa.order_service.constant.OrderType;
import sfa.order_service.constant.TargetStatus;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TargetRes {
    private Long targetId;
    private Double targetAmount;
    private Date startDate;
    private Date endDate;
    private OrderType orderType;
    private TargetStatus status;
    private Long memberId;
    private Long outletId;
    private Long beetId;
}
