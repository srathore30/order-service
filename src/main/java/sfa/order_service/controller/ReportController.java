package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.ReportsRequest;
import sfa.order_service.dto.response.BeetReportResponse;
import sfa.order_service.dto.response.OutletReportResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.dto.response.ReportsResponse;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.ReportServices;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportServices reportServices;

    @GetMapping("/sales")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<ReportsResponse> getSalesReport(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        ReportsResponse reportsResponse = reportServices.getSalesReportBetweenDatesAndSalesLevel(reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/getBeetOrderReportByMemberIdWithDateFilter/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getBeetOrderReportByMemberIdWithDateFilter(@PathVariable Long memberId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> beetReportResponsePaginatedResp = reportServices.getBeetOrderReportByMemberIdWithDateFilter(memberId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(beetReportResponsePaginatedResp, HttpStatus.OK);
    }

    @GetMapping("/getBeetOrderReportByReportingManagerIdWithDateFilter/{reportingManagerId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getBeetOrderReportByReportingManagerIdWithDateFilter(@PathVariable Long reportingManagerId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> beetReportResponsePaginatedResp = reportServices.getBeetOrderReportByReportingManagerIdWithDateFilter(reportingManagerId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(beetReportResponsePaginatedResp, HttpStatus.OK);
    }

    @GetMapping("/getOutletOrderReportByBeetIdWithDateFilter/{beetId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getOutletOrderReportByBeetIdWithDateFilter(@PathVariable Long beetId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> outletOrderReportByBeetIdWithDateFilter = reportServices.getOutletOrderReportByBeetIdWithDateFilter(beetId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(outletOrderReportByBeetIdWithDateFilter, HttpStatus.OK);
    }
}
