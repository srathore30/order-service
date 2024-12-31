package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sfa.order_service.dto.request.DiscountRequest;
import sfa.order_service.dto.response.DiscountResponse;
import sfa.order_service.entity.DiscountEntity;
import sfa.order_service.repo.DiscountRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {
    private final DiscountRepo discountRepo;

    public DiscountEntity dtoToEntity(DiscountRequest request) {
        DiscountEntity discountEntity = new DiscountEntity();
        discountEntity.setDiscountCode(request.getDiscountCode());
        discountEntity.setDescription(request.getDescription());
        discountEntity.setPercentage(request.getPercentage());
        discountEntity.setFixedAmount(request.getFixedAmount());
        discountEntity.setDiscountType(request.getDiscountType());
        discountEntity.setProductId(request.getProductId());
        discountEntity.setOutletId(request.getOutletId());
        discountEntity.setValidFrom(request.getValidFrom());
        discountEntity.setValidTo(request.getValidTo());
        return discountEntity;
    }
    public DiscountResponse entityToDto(DiscountEntity discountEntity) {
        DiscountResponse discountResponse = new DiscountResponse();
        discountResponse.setDiscountCode(discountEntity.getDiscountCode());
        discountResponse.setDescription(discountEntity.getDescription());
        discountResponse.setPercentage(discountEntity.getPercentage());
        discountResponse.setFixedAmount(discountEntity.getFixedAmount());
        discountResponse.setDiscountType(discountEntity.getDiscountType());
        discountResponse.setProductId(discountEntity.getProductId());
        discountResponse.setOutletId(discountEntity.getOutletId());
        discountResponse.setValidFrom(discountEntity.getValidFrom());
        discountResponse.setValidTo(discountEntity.getValidTo());
        return discountResponse;
    }
    public DiscountResponse createDiscount(DiscountRequest request) {
        log.info("create discount");
        DiscountEntity discountEntity = dtoToEntity(request);
        discountRepo.save(discountEntity);
        log.info("discount created successfully");
        return entityToDto(discountEntity);
    }

}
