package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.dto.request.TargetReq;
import sfa.order_service.dto.response.TargetRes;
import sfa.order_service.service.TargetService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/target")
public class TargetController {
    private final TargetService targetService;
    @PostMapping("/create")
    public ResponseEntity<TargetRes> createTarget(@RequestBody TargetReq request) {
        return new ResponseEntity<>(targetService.createTarget(request), HttpStatus.CREATED);
    }

    @PutMapping("/update")
    public ResponseEntity<TargetRes> updateTarget(@PathVariable Long targetId, @RequestBody TargetReq request) {
        return new ResponseEntity<>(targetService.updateTarget(targetId,request), HttpStatus.OK);
    }
}
