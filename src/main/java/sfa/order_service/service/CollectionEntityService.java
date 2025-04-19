package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.request.CollectionEntityRequest;
import sfa.order_service.dto.request.CollectionEntityStatusUpdateReq;
import sfa.order_service.dto.response.CollectionEntityResponse;
import sfa.order_service.dto.response.MemberGetDto;
import sfa.order_service.entity.CollectionEntity;
import sfa.order_service.enums.CollectionStatus;
import sfa.order_service.exception.BusinessServiceException;
import sfa.order_service.repo.CollectionEntityRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectionEntityService {
    private final CollectionEntityRepository collectionEntityRepository;
    private final ExternalRestService externalRestService;

    public CollectionEntity dtoToEntity(CollectionEntityRequest request) {
        log.info("dto to entity in CollectionEntityService");
        CollectionEntity entity = new CollectionEntity();
        entity.setCollectedAmount(request.getCollectedAmount());
        entity.setOutletId(request.getOutletId());
        entity.setMemberId(request.getMemberId());
        entity.setOrderId(request.getOrderId());
        String receiptNumber = "RCPT-" + System.currentTimeMillis();
        entity.setReceiptNumber(receiptNumber);
        entity.setCollectionDate(request.getCollectionDate());
        entity.setPaymentMode(request.getPaymentMode());
        entity.setTransactionRef(request.getTransactionRef());
        entity.setStatus(request.getStatus());
        return entity;
    }

    public CollectionEntityResponse entityToDto(CollectionEntity entity) {
        log.info("entity to dto in CollectionEntityService");
        CollectionEntityResponse response = new CollectionEntityResponse();
        response.setCollectedAmount(entity.getCollectedAmount());
        response.setOutletId(entity.getOutletId());
        response.setMemberId(entity.getMemberId());
        response.setOrderId(entity.getOrderId());
        response.setReceiptNumber(entity.getReceiptNumber());
        response.setCollectionDate(entity.getCollectionDate());
        response.setPaymentMode(entity.getPaymentMode());
        response.setTransactionRef(entity.getTransactionRef());
        response.setStatus(entity.getStatus());
        log.info("Fetch Member Details By external rest service");
        MemberGetDto member = externalRestService.getMember(entity.getMemberId());
        if (member == null) {
            throw new BusinessServiceException(ApiErrorCodes.MEMBER_NOT_FOUND.getErrorCode(), ApiErrorCodes.MEMBER_NOT_FOUND.getErrorMessage());
        }
        response.setMemberName(member.getFirstName() + " " + member.getLastName());
        log.info("Fetch Outlet Details By external rest service");
        String outletById = externalRestService.getOutletById(entity.getOutletId());
        if (outletById == null) {
            throw new BusinessServiceException(ApiErrorCodes.OUTLET_NOT_FOUND.getErrorCode(), ApiErrorCodes.OUTLET_NOT_FOUND.getErrorMessage());
        }
        response.setOutletName(outletById);
        return response;
    }

    public CollectionEntityResponse createCollection(CollectionEntityRequest request) {
        log.info("create collection in CollectionEntityService");
        CollectionEntity entity = dtoToEntity(request);
        log.info("save collection in CollectionEntityService");
        CollectionEntity savedEntity = collectionEntityRepository.save(entity);
        log.info("entity to dto in CollectionEntityService");
        return entityToDto(savedEntity);
    }

    public CollectionEntityResponse updateCollection(Long collectionId, CollectionEntityRequest request) {

        log.info("update collection in CollectionEntityService");
        collectionEntityRepository.findById(collectionId).orElseThrow(() ->
                new BusinessServiceException(ApiErrorCodes.COLLECTION_NOT_FOUND.getErrorCode(), ApiErrorCodes.COLLECTION_NOT_FOUND.getErrorMessage()));
        CollectionEntity entity = collectionEntityRepository.findById(collectionId).get();
        entity.setCollectedAmount(request.getCollectedAmount());
        entity.setCollectionDate(request.getCollectionDate());
        entity.setPaymentMode(request.getPaymentMode());
        entity.setTransactionRef(request.getTransactionRef());
        entity.setStatus(request.getStatus());
        log.info("save collection in CollectionEntityService");
        CollectionEntity savedEntity = collectionEntityRepository.save(entity);
        log.info("entity to dto in CollectionEntityService");
        return entityToDto(savedEntity);
    }

    public CollectionEntityResponse getCollectionById(Long collectionId) {
        log.info("Under Get collection by id in CollectionEntityService");
        Optional<CollectionEntity> collectionEntityById = collectionEntityRepository.findById(collectionId);
        log.info("If no collection found throw exception in CollectionEntityService");
        if (collectionEntityById.isEmpty()) {
            throw new BusinessServiceException(ApiErrorCodes.COLLECTION_NOT_FOUND.getErrorCode(), ApiErrorCodes.COLLECTION_NOT_FOUND.getErrorMessage());
        }
        log.info("entity to dto in CollectionEntityService");
        return entityToDto(collectionEntityById.get());
    }

    public CollectionEntityResponse updateStatusByCollectionId(Long collectionId, CollectionEntityStatusUpdateReq req) {
        log.info("Under update status by collection id in CollectionEntityService");
        Optional<CollectionEntity> byId = collectionEntityRepository.findById(collectionId);
        if (byId.isEmpty()) {
            throw new BusinessServiceException(ApiErrorCodes.COLLECTION_NOT_FOUND.getErrorCode(), ApiErrorCodes.COLLECTION_NOT_FOUND.getErrorMessage());
        }
        CollectionEntity entity = byId.get();
        entity.setStatus(req.getStatus());
        log.info("save collection in CollectionEntityService");
        CollectionEntity savedEntity = collectionEntityRepository.save(entity);
        log.info("entity to dto in CollectionEntityService");
        return entityToDto(savedEntity);
    }
}

