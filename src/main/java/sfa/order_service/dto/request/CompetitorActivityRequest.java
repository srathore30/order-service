package sfa.order_service.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import sfa.order_service.constant.OutletGrade;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompetitorActivityRequest {

    private Long outletId;
    private String agreementImage1;
    private String agreementImage2;
    private String otherImage;

    private OutletGrade outletGrade;

    private String assetType;
    private String assetImage;
    private String assetRemarks;

    private List<String> competitorBrands;
    private List<String> preferredProducts;
    private String scheme;
}
