package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.constant.Status;
import sfa.order_service.dto.request.CompetitorActivityCompetitorRequest;
import sfa.order_service.dto.response.CompetitorActivityCompetitorResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.entity.CompetitorActivityCompetitorEntity;
import sfa.order_service.entity.CompetitorActivityEntity;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.CompetitorActivityCompetitorRepo;
import sfa.order_service.repo.CompetitorActivityRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompetitorActivityCompetitorService {

    private final CompetitorActivityCompetitorRepo competitorRepo;
    private final CompetitorActivityRepo activityRepo;

    public CompetitorActivityCompetitorResponse create(CompetitorActivityCompetitorRequest request) {
        log.info("Creating CompetitorActivityCompetitor for activityId: {}", request.getActivityId());

        CompetitorActivityCompetitorEntity entity = mapToEntity(request);
        CompetitorActivityCompetitorEntity saved = competitorRepo.save(entity);

        log.info("CompetitorActivityCompetitor created with ID: {}", saved.getId());
        return mapToDto(saved);
    }

    public CompetitorActivityCompetitorResponse getById(Long id) {
        log.info("Fetching CompetitorActivityCompetitor with ID: {}", id);
        CompetitorActivityCompetitorEntity entity = competitorRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.COMPETITOR_ACTIVITY_COMPETITOR_NOT_FOUND.getErrorCode(),ApiErrorCodes.COMPETITOR_ACTIVITY_COMPETITOR_NOT_FOUND.getErrorMessage()));
        return mapToDto(entity);
    }

    public PaginatedResp<CompetitorActivityCompetitorResponse> getAll(int page, int size, String sortBy, String sortDirection) {
        log.info("Fetching all CompetitorActivityCompetitor data with pagination: page={}, size={}", page, size);

        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CompetitorActivityCompetitorEntity> pageData = competitorRepo.findAll(pageable);

        List<CompetitorActivityCompetitorResponse> content = pageData.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new PaginatedResp<>(
                pageData.getTotalElements(),
                pageData.getTotalPages(),
                page,
                content
        );
    }


    public CompetitorActivityCompetitorResponse update(Long id, CompetitorActivityCompetitorRequest request) {
        log.info("Updating CompetitorActivityCompetitor with ID: {}", id);
        CompetitorActivityCompetitorEntity entity = competitorRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.COMPETITOR_ACTIVITY_COMPETITOR_NOT_FOUND.getErrorCode(),ApiErrorCodes.COMPETITOR_ACTIVITY_COMPETITOR_NOT_FOUND.getErrorMessage()));
        updateEntityFromDto(entity, request);
        CompetitorActivityCompetitorEntity updated = competitorRepo.save(entity);
        return mapToDto(updated);
    }

    public void delete(Long id) {
        log.info("Deleting CompetitorActivityCompetitor with ID: {}", id);
        CompetitorActivityCompetitorEntity entity = competitorRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.COMPETITOR_ACTIVITY_COMPETITOR_NOT_FOUND.getErrorCode(),ApiErrorCodes.COMPETITOR_ACTIVITY_COMPETITOR_NOT_FOUND.getErrorMessage()));
        entity.setStatus(Status.Inactive);
        competitorRepo.save(entity);
    }

    private CompetitorActivityCompetitorEntity mapToEntity(CompetitorActivityCompetitorRequest request) {
        CompetitorActivityEntity activity = activityRepo.findById(request.getActivityId())
                .orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorCode(),ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorMessage()));

        CompetitorActivityCompetitorEntity entity = new CompetitorActivityCompetitorEntity();
        entity.setActivity(activity);
        entity.setCompetitorName(request.getCompetitorName());
        return entity;
    }

    private CompetitorActivityCompetitorResponse mapToDto(CompetitorActivityCompetitorEntity entity) {
        CompetitorActivityCompetitorResponse dto = new CompetitorActivityCompetitorResponse();
        dto.setId(entity.getId());
        dto.setActivityId(entity.getActivity().getId());
        dto.setCompetitorName(entity.getCompetitorName());
        return dto;
    }

    private void updateEntityFromDto(CompetitorActivityCompetitorEntity entity, CompetitorActivityCompetitorRequest request) {
        if (request.getActivityId() != null) {
            CompetitorActivityEntity activity = activityRepo.findById(request.getActivityId())
                    .orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorCode(),ApiErrorCodes.COMPETITOR_ACTIVITY_NOT_FOUND.getErrorMessage()));
            entity.setActivity(activity);
        }
        entity.setCompetitorName(request.getCompetitorName());
    }


}
