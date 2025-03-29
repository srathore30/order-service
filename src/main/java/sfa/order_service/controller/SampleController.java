package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.SampleReq;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.dto.response.SampleRes;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.SampleServices;

import java.util.List;

@RestController
@RequestMapping("/samples")
@RequiredArgsConstructor
@Slf4j
public class SampleController {
    private final SampleServices sampleServices;

    @PostMapping
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<SampleRes> createSample(@RequestBody SampleReq sampleReq) {
        log.info("Creating a sample: {}", sampleReq);
        SampleRes response = sampleServices.createSample(sampleReq);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<SampleRes> updateSample(@PathVariable Long id, @RequestBody SampleReq sampleReq) {
        log.info("Updating sample with ID: {}", id);
        SampleRes response = sampleServices.updateSample(id, sampleReq);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<SampleRes> getSampleById(@PathVariable Long id) {
        log.info("Fetching sample with ID: {}", id);
        SampleRes response = sampleServices.getSampleById(id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<Void> deleteSample(@PathVariable Long id) {
        log.info("Deleting sample with ID: {}", id);
        sampleServices.deleteSample(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/bulk")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<SampleRes>> createSamplesInBulk(@RequestBody List<SampleReq> sampleReqList) {
        log.info("Creating samples in bulk: {}", sampleReqList.size());
        List<SampleRes> response = sampleServices.createSampleInBulk(sampleReqList);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/manager/{managerId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<SampleRes>> getSamplesByManager(
            @PathVariable Long managerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        log.info("Fetching samples for manager with ID: {}", managerId);
        PaginatedResp<SampleRes> response = sampleServices.getAllSampleByReportingManagerMembers(managerId, page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/member/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<SampleRes>> getSamplesByMember(
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        log.info("Fetching samples for member with ID: {}", memberId);
        PaginatedResp<SampleRes> response = sampleServices.getAllSampleByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<SampleRes>> getAllSamples(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "createdDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        log.info("Fetching all samples");
        PaginatedResp<SampleRes> response = sampleServices.getAllSamples(page, pageSize, sortBy, sortDirection);
        return ResponseEntity.ok(response);
    }
}
