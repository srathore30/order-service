package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.constant.DiscountType;
import sfa.order_service.dto.request.DiscountBulkReq;
import sfa.order_service.dto.request.DiscountRequest;
import sfa.order_service.dto.response.DiscountResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.dto.response.ProductRes;
import sfa.order_service.entity.DiscountEntity;
import sfa.order_service.exception.InvalidInputException;
import sfa.order_service.repo.DiscountRepo;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountService {
    private final DiscountRepo discountRepo;
    private final ProductServiceClient productServiceClient;

    public DiscountEntity dtoToEntity(DiscountRequest request) {
        DiscountEntity discountEntity = new DiscountEntity();
        log.info("Common fields for discount payload");
        discountEntity.setDiscountCode(request.getDiscountCode());
        discountEntity.setDescription(request.getDescription());
        discountEntity.setDiscountType(request.getDiscountType());
        discountEntity.setValidFrom(request.getValidFrom());
        discountEntity.setValidTo(request.getValidTo());
        discountEntity.setProductId(request.getProductId());
        discountEntity.setStatus(request.getStatus());
        discountEntity.setCity(request.getCity());
        discountEntity.setState(request.getState());

        // Conditional fields based on DiscountType
        switch (request.getDiscountType()) {
            case PROMOTIONAL:
                log.info("Setting Fixed Amount for PROMOTIONAL discount");
                discountEntity.setFixedAmount(request.getFixedAmount());
                discountEntity.setPercentage(null);
                discountEntity.setMinQuantity(null);
                discountEntity.setBogoOfferQuantity(null);
                discountEntity.setBogoFreeQuantity(null);
                break;

            case QUANTITY_BASED:
                log.info("Setting MinQuantity & Percentage for QUANTITY_BASED discount");
                discountEntity.setMinQuantity(request.getMinQuantity());
                discountEntity.setPercentage(request.getPercentage());
                discountEntity.setFixedAmount(null);
                discountEntity.setBogoOfferQuantity(null);
                discountEntity.setBogoFreeQuantity(null);
                break;

            case SEASONAL:
                log.info("Setting Percentage & FixedAmount for SEASONAL discount");
                discountEntity.setPercentage(request.getPercentage());
                discountEntity.setFixedAmount(request.getFixedAmount());
                discountEntity.setMinQuantity(null);
                discountEntity.setBogoOfferQuantity(null);
                discountEntity.setBogoFreeQuantity(null);
                break;

            case BOGO:
                log.info("Setting bogoOfferQuantity & bogoFreeQuantity for BOGO discount");
                discountEntity.setBogoOfferQuantity(request.getBogoOfferQuantity());
                discountEntity.setBogoFreeQuantity(request.getBogoFreeQuantity());
                discountEntity.setPercentage(null);
                discountEntity.setFixedAmount(null);
                discountEntity.setMinQuantity(null);
                break;

            case VOLUME_BASED:
                log.info("Setting FixedAmount & MinQuantity for VOLUME_BASED discount");
                discountEntity.setFixedAmount(request.getFixedAmount());
                discountEntity.setMinQuantity(request.getMinQuantity());
                discountEntity.setPercentage(null);
                discountEntity.setBogoOfferQuantity(null);
                discountEntity.setBogoFreeQuantity(null);
                break;

            case LOYALTY:
                log.info("Setting Percentage & FixedAmount for LOYALTY discount");
                discountEntity.setPercentage(request.getPercentage());
                discountEntity.setFixedAmount(request.getFixedAmount());
                discountEntity.setMinQuantity(null);
                discountEntity.setBogoOfferQuantity(null);
                discountEntity.setBogoFreeQuantity(null);
                break;

            default:
                throw new IllegalArgumentException("Unsupported DiscountType: " + request.getDiscountType());
        }

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
        discountResponse.setDiscountId(discountEntity.getId());
        ProductRes product = productServiceClient.getProduct(discountEntity.getProductId());
        discountResponse.setProductName(product.getName());
        discountResponse.setValidFrom(discountEntity.getValidFrom());
        discountResponse.setValidTo(discountEntity.getValidTo());
        discountResponse.setMinQuantity(discountEntity.getMinQuantity());
        discountResponse.setBogoOfferQuantity(discountEntity.getBogoOfferQuantity());
        discountResponse.setBogoFreeQuantity(discountEntity.getBogoFreeQuantity());
        discountResponse.setStatus(discountEntity.getStatus());
        discountResponse.setCity(discountEntity.getCity());
        discountResponse.setState(discountEntity.getState());
        return discountResponse;
    }

    public DiscountResponse createDiscount(DiscountRequest request) {
        log.info("Check if discountCode already exists");
        if (discountRepo.existsByDiscountCode(request.getDiscountCode())) {
            throw new InvalidInputException(ApiErrorCodes.DISCOUNT_CODE_ALREADY_EXIST.getErrorCode(), ApiErrorCodes.DISCOUNT_CODE_ALREADY_EXIST.getErrorMessage());
        }

        // Modify this check to allow BOGO discount type without percentage or fixedAmount
        log.info("Checking if either percentage or fixedAmount is provided, unless discountType is BOGO");
        if (request.getDiscountType() != DiscountType.BOGO) {
            if (request.getPercentage() == null && request.getFixedAmount() == null) {
                throw new IllegalArgumentException("Either percentage or fixedAmount must be provided.");
            }
            log.info("percentage discount is within a valid range (0-100%)");
            if (request.getPercentage() != null && request.getPercentage() > 100) {
                throw new IllegalArgumentException("Percentage cannot exceed 100.");
            }
        }

        log.info("discount dto to entity conversion");
        DiscountEntity discountEntity = dtoToEntity(request);

        log.info("saving discount into master table");
        discountRepo.save(discountEntity);

        log.info("Discount created successfully");
        return entityToDto(discountEntity);
    }

    public PaginatedResp<DiscountResponse> getDiscountDetailsByProductId(Long productId, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("Make pageable data for pagination");
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("Get all discounts by product id");
        Page<DiscountEntity> discountEntities = discountRepo.findAllByProductId(productId, pageable);
        List<DiscountResponse> discountResponses = discountEntities.stream().map(this::entityToDto).toList();
        return new PaginatedResp<>(discountEntities.getTotalElements(), discountEntities.getTotalPages(), page, discountResponses);
    }

    public PaginatedResp<DiscountResponse> getAllDiscounts(int page, int pageSize, String sortBy, String sortDirection) {
        log.info("Retrieve all discounts with pagination and sorting");
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<DiscountEntity> discountEntities = discountRepo.findAll(pageable);
        List<DiscountResponse> discountResponses = discountEntities.stream().map(this::entityToDto).toList();
        return new PaginatedResp<>(discountEntities.getTotalElements(), discountEntities.getTotalPages(), page, discountResponses);
    }

    public DiscountResponse updateDiscountByProductId(Long productId, DiscountRequest discountRequest) {
        log.info("Retrieve discount by product ID: {}", productId);
        Optional<DiscountEntity> optionalDiscount = discountRepo.findByProductId(productId);
        if (optionalDiscount.isEmpty()) {
            throw new InvalidInputException(ApiErrorCodes.DISCOUNT_NOT_FOUND.getErrorCode(), ApiErrorCodes.DISCOUNT_NOT_FOUND.getErrorMessage());
        }

        DiscountEntity existingDiscount = optionalDiscount.get();

        log.info("Update discount details");
        existingDiscount.setDiscountCode(discountRequest.getDiscountCode());
        existingDiscount.setDescription(discountRequest.getDescription());
        existingDiscount.setDiscountType(discountRequest.getDiscountType());
        existingDiscount.setValidFrom(discountRequest.getValidFrom());
        existingDiscount.setValidTo(discountRequest.getValidTo());
        existingDiscount.setStatus(discountRequest.getStatus());

        switch (discountRequest.getDiscountType()) {
            case PROMOTIONAL:
                existingDiscount.setFixedAmount(discountRequest.getFixedAmount());
                existingDiscount.setPercentage(null);
                existingDiscount.setMinQuantity(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;
            case QUANTITY_BASED:
                existingDiscount.setMinQuantity(discountRequest.getMinQuantity());
                existingDiscount.setPercentage(discountRequest.getPercentage());
                existingDiscount.setFixedAmount(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;
            case SEASONAL:
                log.info("Updating SEASONAL discount fields");
                existingDiscount.setPercentage(discountRequest.getPercentage());
                existingDiscount.setFixedAmount(discountRequest.getFixedAmount());
                existingDiscount.setMinQuantity(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;

            case BOGO:
                log.info("Updating BOGO discount fields");
                existingDiscount.setBogoOfferQuantity(discountRequest.getBogoOfferQuantity());
                existingDiscount.setBogoFreeQuantity(discountRequest.getBogoFreeQuantity());
                existingDiscount.setPercentage(null);
                existingDiscount.setFixedAmount(null);
                existingDiscount.setMinQuantity(null);
                break;

            case VOLUME_BASED:
                log.info("Updating VOLUME_BASED discount fields");
                existingDiscount.setFixedAmount(discountRequest.getFixedAmount());
                existingDiscount.setMinQuantity(discountRequest.getMinQuantity());
                existingDiscount.setPercentage(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;

            case LOYALTY:
                log.info("Updating LOYALTY discount fields");
                existingDiscount.setPercentage(discountRequest.getPercentage());
                existingDiscount.setFixedAmount(discountRequest.getFixedAmount());
                existingDiscount.setMinQuantity(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;

            default:
                throw new IllegalArgumentException("Unsupported DiscountType: " + discountRequest.getDiscountType());
        }

        log.info("Saving updated discount details");
        discountRepo.save(existingDiscount);
        return entityToDto(existingDiscount);
    }
    @Transactional
    public List<DiscountResponse> createBulkDiscount(DiscountBulkReq request) {
        log.info("Creating bulk discounts");
        List<DiscountResponse> discountResponseList = new ArrayList<>();

        for (DiscountRequest discountRequest : request.getDiscountRequestList()) {
            log.info("Processing discount with code: {}", discountRequest.getDiscountCode());

            if (discountRepo.existsByDiscountCode(discountRequest.getDiscountCode())) {
                throw new InvalidInputException(ApiErrorCodes.DISCOUNT_CODE_ALREADY_EXIST.getErrorCode(),
                        String.format("Discount code %s already exists", discountRequest.getDiscountCode()));
            }

            if (discountRequest.getDiscountType() != DiscountType.BOGO) {
                if (discountRequest.getPercentage() == null && discountRequest.getFixedAmount() == null) {
                    throw new IllegalArgumentException("Either percentage or fixedAmount must be provided for discountCode: " + discountRequest.getDiscountCode());
                }

                if (discountRequest.getPercentage() != null && discountRequest.getPercentage() > 100) {
                    throw new IllegalArgumentException("Percentage cannot exceed 100 for discountCode: " + discountRequest.getDiscountCode());
                }
            }

            DiscountEntity discountEntity = dtoToEntity(discountRequest);
            discountRepo.save(discountEntity);
            DiscountResponse discountResponse = entityToDto(discountEntity);
            discountResponseList.add(discountResponse);
        }
        log.info("Bulk discount creation completed successfully");
        return discountResponseList;
    }

    @Transactional
    public DiscountResponse updateDiscountById(Long discountId, DiscountRequest discountRequest) {
        log.info("Retrieve discount by ID: {}", discountId);
        Optional<DiscountEntity> optionalDiscount = discountRepo.findById(discountId);
        if (optionalDiscount.isEmpty()) {
            throw new InvalidInputException(ApiErrorCodes.DISCOUNT_NOT_FOUND.getErrorCode(),
                    ApiErrorCodes.DISCOUNT_NOT_FOUND.getErrorMessage());
        }

        DiscountEntity existingDiscount = optionalDiscount.get();

        log.info("Updating discount details for ID: {}", discountId);
        existingDiscount.setDiscountCode(discountRequest.getDiscountCode());
        existingDiscount.setDescription(discountRequest.getDescription());
        existingDiscount.setDiscountType(discountRequest.getDiscountType());
        existingDiscount.setValidFrom(discountRequest.getValidFrom());
        existingDiscount.setValidTo(discountRequest.getValidTo());
        existingDiscount.setStatus(discountRequest.getStatus());

        if (discountRequest.getProductId() != null) {
            existingDiscount.setProductId(discountRequest.getProductId());
        }
        switch (discountRequest.getDiscountType()) {
            case PROMOTIONAL:
                existingDiscount.setFixedAmount(discountRequest.getFixedAmount());
                existingDiscount.setPercentage(discountRequest.getPercentage());
                existingDiscount.setMinQuantity(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;
            case QUANTITY_BASED:
                existingDiscount.setMinQuantity(discountRequest.getMinQuantity());
                existingDiscount.setPercentage(discountRequest.getPercentage());
                existingDiscount.setFixedAmount(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;
            case SEASONAL:
                existingDiscount.setPercentage(discountRequest.getPercentage());
                existingDiscount.setFixedAmount(discountRequest.getFixedAmount());
                existingDiscount.setMinQuantity(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;
            case BOGO:
                existingDiscount.setBogoOfferQuantity(discountRequest.getBogoOfferQuantity());
                existingDiscount.setBogoFreeQuantity(discountRequest.getBogoFreeQuantity());
                existingDiscount.setPercentage(null);
                existingDiscount.setFixedAmount(null);
                existingDiscount.setMinQuantity(null);
                break;
            case VOLUME_BASED:
                existingDiscount.setFixedAmount(discountRequest.getFixedAmount());
                existingDiscount.setMinQuantity(discountRequest.getMinQuantity());
                existingDiscount.setPercentage(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;
            case LOYALTY:
                existingDiscount.setPercentage(discountRequest.getPercentage());
                existingDiscount.setFixedAmount(discountRequest.getFixedAmount());
                existingDiscount.setMinQuantity(null);
                existingDiscount.setBogoOfferQuantity(null);
                existingDiscount.setBogoFreeQuantity(null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported DiscountType: " + discountRequest.getDiscountType());
        }

        log.info("Saving updated discount to the repository");
        discountRepo.save(existingDiscount);

        log.info("Discount updated successfully for ID: {}", discountId);
        return entityToDto(existingDiscount);
    }

}

