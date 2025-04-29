package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.response.CompetitorActivityResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.entity.CompetitorActivityEntity;
import sfa.order_service.dto.request.CompetitorActivityRequest;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.CompetitorActivityRepo;
import sfa.order_service.constant.Status;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompetitorActivityService {

    private final CompetitorActivityRepo competitorActivityRepo;

    public CompetitorActivityResponse create(CompetitorActivityRequest request) {
        log.info("Creating CompetitorActivity for outletId: {}", request.getOutletId());
        CompetitorActivityEntity entity = mapToEntity(request);
        CompetitorActivityEntity saved = competitorActivityRepo.save(entity);
        log.info("CompetitorActivity created with ID: {}", saved.getId());
        return mapToDto(saved);
    }

    public CompetitorActivityResponse getById(Long id) {
        log.info("Fetching CompetitorActivity with ID: {}", id);
        CompetitorActivityEntity entity = competitorActivityRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorCode(),ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorMessage()));
        return mapToDto(entity);
    }

    public PaginatedResp<CompetitorActivityResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching all CompetitorActivity data with pagination: page={}, size={}", page, size);
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CompetitorActivityEntity> pageData = competitorActivityRepo.findAll(pageable);

        List<CompetitorActivityResponse> content = pageData.getContent().stream()
                .filter(activity -> activity.getStatus() == Status.Active)
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PaginatedResp<>(pageData.getTotalElements(), pageData.getTotalPages(), page, content);
    }

    public CompetitorActivityResponse update(Long id, CompetitorActivityRequest request) {
        log.info("Updating CompetitorActivity with ID: {}", id);
        CompetitorActivityEntity entity = competitorActivityRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorCode(),ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorMessage()));

        updateEntityFromDto(entity, request);
        CompetitorActivityEntity updated = competitorActivityRepo.save(entity);
        return mapToDto(updated);
    }

    public void delete(Long id) {
        log.info("Deleting CompetitorActivity with ID: {}", id);
        CompetitorActivityEntity entity = competitorActivityRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorCode(),ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorMessage()));

        entity.setStatus(Status.Inactive);
        competitorActivityRepo.save(entity);
        log.info("Successfully deleted CompetitorActivity with ID: {}", id);
    }

    private CompetitorActivityEntity mapToEntity(CompetitorActivityRequest request) {
        CompetitorActivityEntity entity = new CompetitorActivityEntity();
        entity.setOutletId(request.getOutletId());
        entity.setAgreementImage1(request.getAgreementImage1());
        entity.setAgreementImage2(request.getAgreementImage2());
        entity.setOtherImage(request.getOtherImage());
        entity.setOutletGrade(request.getOutletGrade());
        entity.setAssetType(request.getAssetType());
        entity.setAssetImage(request.getAssetImage());
        entity.setAssetRemarks(request.getAssetRemarks());
        entity.setCompetitorBrands(request.getCompetitorBrands());
        entity.setPreferredProducts(request.getPreferredProducts());
        entity.setScheme(request.getScheme());
        return entity;
    }

    private CompetitorActivityResponse mapToDto(CompetitorActivityEntity entity) {
        CompetitorActivityResponse response = new CompetitorActivityResponse();
        response.setId(entity.getId());
        response.setOutletId(entity.getOutletId());
        response.setAgreementImage1(entity.getAgreementImage1());
        response.setAgreementImage2(entity.getAgreementImage2());
        response.setOtherImage(entity.getOtherImage());
        response.setOutletGrade(entity.getOutletGrade());
        response.setAssetType(entity.getAssetType());
        response.setAssetImage(entity.getAssetImage());
        response.setAssetRemarks(entity.getAssetRemarks());
        response.setCompetitorBrands(entity.getCompetitorBrands());
        response.setPreferredProducts(entity.getPreferredProducts());
        response.setScheme(entity.getScheme());
        return response;
    }

    private void updateEntityFromDto(CompetitorActivityEntity entity, CompetitorActivityRequest request) {
        entity.setOutletId(request.getOutletId());
        entity.setAgreementImage1(request.getAgreementImage1());
        entity.setAgreementImage2(request.getAgreementImage2());
        entity.setOtherImage(request.getOtherImage());
        entity.setOutletGrade(request.getOutletGrade());
        entity.setAssetType(request.getAssetType());
        entity.setAssetImage(request.getAssetImage());
        entity.setAssetRemarks(request.getAssetRemarks());
        entity.setCompetitorBrands(request.getCompetitorBrands());
        entity.setPreferredProducts(request.getPreferredProducts());
        entity.setScheme(request.getScheme());
    }

}
