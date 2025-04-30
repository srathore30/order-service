package sfa.order_service.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import sfa.order_service.constant.OutletGrade;
import sfa.order_service.constant.Status;

import java.util.List;


@Getter
@Setter
@Entity
@Table(name = "competitor_activity")
public class CompetitorActivityEntity extends BaseEntity{

    private Long outletId;
    private String agreementImage1;
    private String agreementImage2;
    private String otherImage;

    @Enumerated(EnumType.STRING)
    private OutletGrade outletGrade;

    private String assetType;
    private String assetImage;
    private String assetRemarks;

    private List<String> competitorBrands;
    private List<String> preferredProducts;
    private String scheme;

    @Enumerated(EnumType.STRING)
    private Status status;


}
