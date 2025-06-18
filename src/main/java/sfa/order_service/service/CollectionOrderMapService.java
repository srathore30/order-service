package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.constant.Status;
import sfa.order_service.dto.request.CollectionOrderMapRequest;
import sfa.order_service.dto.response.CollectionOrderMapResponse;
import sfa.order_service.dto.response.MemberGetDto;
import sfa.order_service.entity.CollectionEntity;
import sfa.order_service.entity.CollectionOrderMap;
import sfa.order_service.exception.BusinessServiceException;
import sfa.order_service.repo.CollectionEntityRepository;
import sfa.order_service.repo.CollectionOrderMapRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionOrderMapService {
    private final CollectionEntityRepository collectionEntityRepository;
    private final CollectionOrderMapRepository collectionOrderMapRepository;
    private final ExternalRestService externalRestService;

    public CollectionOrderMap dtoToEntity(CollectionOrderMapRequest request) {
        log.info("Under dto to entity in CollectionOrderMapService");
        CollectionOrderMap entity = new CollectionOrderMap();
        entity.setCollectionId(request.getCollectionId());
        entity.setOrderId(request.getOrderId());
        entity.setAmountForThisOrder(request.getAmountForThisOrder());
        entity.setStatus(request.getStatus());
        return entity;
    }

    public CollectionOrderMapResponse entityToDto(CollectionOrderMap entity) {
        log.info("Under entity to dto in CollectionOrderMapService");
        CollectionOrderMapResponse response = new CollectionOrderMapResponse();
        response.setCollectionId(entity.getCollectionId());
        response.setOrderId(entity.getOrderId());
        response.setAmountForThisOrder(entity.getAmountForThisOrder());
        response.setStatus(entity.getStatus());
        log.info("Fetch Collection Entity By Collection Id");
        Optional<CollectionEntity> collectionEntityById = collectionEntityRepository.findById(entity.getCollectionId());
        if (collectionEntityById.isEmpty()) {
            throw new BusinessServiceException(ApiErrorCodes.COLLECTION_NOT_FOUND.getErrorCode(), ApiErrorCodes.COLLECTION_NOT_FOUND.getErrorMessage());
        }
        log.info("Fetch Member Details By external rest service");
        MemberGetDto member = externalRestService.getMember(collectionEntityById.get().getMemberId());
        response.setMemberName(member.getFirstName() + " " + member.getLastName());
        return response;
    }

    public CollectionOrderMapResponse createCollectionOrderMap(CollectionOrderMapRequest request) {
        log.info("Under create collection order map in CollectionOrderMapService");
        CollectionOrderMap collectionOrderMap = dtoToEntity(request);
        CollectionOrderMap save = collectionOrderMapRepository.save(collectionOrderMap);
        return entityToDto(save);
    }

    public CollectionOrderMapResponse updateCollectionOrderMapById(Long id, CollectionOrderMapRequest request) {
        log.info("Under update Collection-Order-Map by orderMapId id in CollectionOrderMapService");
        Optional<CollectionOrderMap> collectionOrderMapById = collectionOrderMapRepository.findById(id);
        if (collectionOrderMapById.isEmpty()) {
            throw new BusinessServiceException(ApiErrorCodes.COLLECTION_ORDER_MAP_NOT_FOUND.getErrorCode(), ApiErrorCodes.COLLECTION_ORDER_MAP_NOT_FOUND.getErrorMessage());
        }
        collectionOrderMapById.get().setAmountForThisOrder(request.getAmountForThisOrder());
        collectionOrderMapById.get().setOrderId(request.getOrderId());
        log.info("save collection order map in CollectionOrderMapService");
        CollectionOrderMap save = collectionOrderMapRepository.save(collectionOrderMapById.get());
        return entityToDto(save);
    }
    public String deleteCollectionOrderMapById(Long id) {
        log.info("Under delete Collection-Order-Map by orderMapId id in CollectionOrderMapService");
        Optional<CollectionOrderMap> collectionOrderMapById = collectionOrderMapRepository.findById(id);
        if (collectionOrderMapById.isEmpty()) {
            throw new BusinessServiceException(ApiErrorCodes.COLLECTION_ORDER_MAP_NOT_FOUND.getErrorCode(), ApiErrorCodes.COLLECTION_ORDER_MAP_NOT_FOUND.getErrorMessage());
        }
        log.info("Collection-Order-Map status is changed as INACTIVE successfully");
        collectionOrderMapById.get().setStatus(Status.Inactive);
        return "Collection-Order-Map deleted successfully";
    }
}
