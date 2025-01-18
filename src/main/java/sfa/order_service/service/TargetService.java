package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.request.TargetReq;
import sfa.order_service.dto.response.TargetRes;
import sfa.order_service.entity.TargetEntity;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.TargetRepo;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TargetService {
    private final TargetRepo targetRepo;
     public TargetEntity dtoToEntity(TargetReq request){
         log.info("Dto to entity in Target-Service");
         TargetEntity entity=new TargetEntity();
         entity.setTargetAmount(request.getTargetAmount());
         entity.setStartDate(request.getStartDate());
         entity.setEndDate(request.getEndDate());
         entity.setOrderType(request.getOrderType());
         entity.setStatus(request.getStatus());
         entity.setMemberId(request.getMemberId());
         entity.setOutletId(request.getOutletId());
         entity.setBeetId(request.getBeetId());
         log.info("Dto to entity in Target-Service done");
         return entity;
     }
     public TargetRes entityToDto(TargetEntity entity){
         log.info("Entity to dto in Target-Service");
         TargetRes targetRes=new TargetRes();
         targetRes.setTargetAmount(entity.getTargetAmount());
         targetRes.setStartDate(entity.getStartDate());
         targetRes.setEndDate(entity.getEndDate());
         targetRes.setOrderType(entity.getOrderType());
         targetRes.setStatus(entity.getStatus());
         targetRes.setMemberId(entity.getMemberId());
         targetRes.setOutletId(entity.getOutletId());
         targetRes.setBeetId(entity.getBeetId());
         log.info("Entity to dto in Target-Service done");
         return targetRes;
     }
     public TargetRes createTarget(TargetReq request) {
         log.info("Create Target in Target-Service");
         TargetEntity targetEntity = dtoToEntity(request);
         targetRepo.save(targetEntity);
         log.info("Create Target in Target-Service done");
         return entityToDto(targetEntity);
     }

    public TargetRes updateTarget(Long targetId, TargetReq request) {
         log.info("Update Target in Target-Service");
        Optional<TargetEntity> byId = targetRepo.findById(targetId);
        if(byId.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.TARGET_NOT_FOUND.getErrorCode(), ApiErrorCodes.TARGET_NOT_FOUND.getErrorMessage());
        }
        TargetEntity targetEntity = byId.get();
        targetEntity.setTargetAmount(request.getTargetAmount());
        targetEntity.setStartDate(request.getStartDate());
        targetEntity.setEndDate(request.getEndDate());
        targetEntity.setOrderType(request.getOrderType());
        targetEntity.setStatus(request.getStatus());
        targetEntity.setMemberId(request.getMemberId());
        targetEntity.setOutletId(request.getOutletId());
        targetEntity.setBeetId(request.getBeetId());
        log.info("Update Target in Target-Service done");
        return entityToDto(targetRepo.save(targetEntity));
    }
}
