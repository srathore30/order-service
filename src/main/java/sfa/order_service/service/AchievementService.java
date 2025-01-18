package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sfa.order_service.dto.request.AchievementReq;
import sfa.order_service.dto.response.AchievementRes;
import sfa.order_service.entity.AchievementEntity;
import sfa.order_service.repo.AchievementRepo;

@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementService {
    private final AchievementRepo achievementRepo;
    public AchievementEntity dtoToEntity(AchievementReq request){
        log.info("Dto to entity in Achievement-Service");
        AchievementEntity entity=new AchievementEntity();
        entity.setAchievementDate(request.getAchievementDate());
        entity.setStatus(request.getStatus());
        entity.setSalesAmount(request.getSalesAmount());
        entity.setOrderType(request.getOrderType());
        entity.setMemberId(request.getMemberId());
        entity.setBeetId(request.getBeetId());
        entity.setOutletId(request.getOutletId());
        log.info("Entity created successfully in Achievement-Service");
        return entity;
    }
    public AchievementRes entityToDto(AchievementEntity entity){
        log.info("Entity to dto in Achievement-Service");
        AchievementRes res=new AchievementRes();
        res.setAchievementDate(entity.getAchievementDate());
        res.setStatus(entity.getStatus());
        res.setSalesAmount(entity.getSalesAmount());
        res.setOrderType(entity.getOrderType());
        res.setMemberId(entity.getMemberId());
        res.setBeetId(entity.getBeetId());
        res.setOutletId(entity.getOutletId());
        log.info("Dto created successfully in Achievement-Service");
        return res;
    }
    public AchievementRes createAchievement(AchievementReq request){
        log.info("Create achievement in Achievement-Service");
        AchievementEntity entity=dtoToEntity(request);
        AchievementEntity save = achievementRepo.save(entity);
        log.info("Achievement created successfully in Achievement-Service");
        return entityToDto(save);
    }
}
