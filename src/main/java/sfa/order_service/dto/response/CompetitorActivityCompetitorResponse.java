package sfa.order_service.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorActivityCompetitorResponse {
    private Long id;
    private Long activityId;
    private String competitorName;
}
