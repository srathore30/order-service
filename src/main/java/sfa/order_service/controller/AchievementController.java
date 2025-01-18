package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import sfa.order_service.dto.request.AchievementReq;
import sfa.order_service.dto.response.AchievementRes;
import sfa.order_service.service.AchievementService;

@Controller
@RequiredArgsConstructor
public class AchievementController {
    private final AchievementService achievementService;
    @PostMapping("/createAchievement")
    public ResponseEntity<AchievementRes> createAchievement(@RequestBody AchievementReq request){
        return new ResponseEntity<>(achievementService.createAchievement(request), HttpStatus.CREATED);
    }
}
