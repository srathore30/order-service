package sfa.order_service.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.Status;

@Getter
@Setter
@Entity
@Table(name = "competitor_activity_competitor")
public class CompetitorActivityCompetitorEntity extends BaseEntity{


    @ManyToOne
    @JsonBackReference
    private CompetitorActivityEntity activity;
    private String competitorName;

    @Enumerated(EnumType.STRING)
    private Status status;
}
