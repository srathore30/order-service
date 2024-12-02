package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.ReportsRequest;
import sfa.order_service.dto.response.*;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.ReportServices;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportServices reportServices;

    @GetMapping("/sales")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<ReportsResponse> getSalesReport(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        ReportsResponse reportsResponse = reportServices.getSalesReportBetweenDatesAndSalesLevel(reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/getBeetOrderReportByMemberIdWithDateFilter/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.Client, UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getBeetOrderReportByMemberIdWithDateFilter(@PathVariable Long memberId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> beetReportResponsePaginatedResp = reportServices.getBeetOrderReportByMemberIdWithDateFilter(memberId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(beetReportResponsePaginatedResp, HttpStatus.OK);
    }

    @GetMapping("/getBeetOrderReportByReportingManagerIdWithDateFilter/{reportingManagerId}")
    @UserAuthorization(allowedRoles = {UserRole.Client, UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getBeetOrderReportByReportingManagerIdWithDateFilter(@PathVariable Long reportingManagerId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> beetReportResponsePaginatedResp = reportServices.getBeetOrderReportByReportingManagerIdWithDateFilter(reportingManagerId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(beetReportResponsePaginatedResp, HttpStatus.OK);
    }

    @GetMapping("/getOutletOrderReportByBeetIdWithDateFilter/{beetId}")
    @UserAuthorization(allowedRoles = {UserRole.Client, UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getOutletOrderReportByBeetIdWithDateFilter(@PathVariable Long beetId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> outletOrderReportByBeetIdWithDateFilter = reportServices.getOutletOrderReportByBeetIdWithDateFilter(beetId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(outletOrderReportByBeetIdWithDateFilter, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachOutletByMemberIdByProductiveStatus/{member}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOrderByEachOutletByMemberIdByProductiveStatus(@PathVariable Long memberId, @RequestParam OrderCallStatus orderCallStatus, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOrderByEachOutletByMemberIdByProductiveStatus(memberId, orderCallStatus, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachOutletByMemberIdByOrderMedium/{member}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOrderByEachOutletByMemberIdByOrderMedium(@PathVariable Long memberId, @RequestParam OrderMedium orderMedium, @RequestParam OrderCallStatus orderCallStatus, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOrderByEachOutletByMemberIdByOrderMedium(memberId, orderMedium, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachBeetByMemberIdByProductiveStatus/{member}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOrderByEachBeetByMemberIdByProductiveStatus(@PathVariable Long memberId, @RequestParam OrderCallStatus orderCallStatus, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOrderByEachBeetByMemberIdByProductiveStatus(memberId, orderCallStatus, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachBeetByMemberIdByOrderMedium/{member}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOrderByEachBeetByMemberIdByOrderMedium(@PathVariable Long memberId, @RequestParam OrderMedium orderMedium,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOrderByEachBeetByMemberIdByOrderMedium(memberId, orderMedium, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }


    @GetMapping("/getAllOrderByEachOutletByClientFmcgIdByProductiveStatus/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOrderByEachOutletByClientFmcgIdByProductiveStatus(@PathVariable Long clientFmcgId, @RequestParam OrderCallStatus orderCallStatus,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOrderByEachOutletByClientFmcgIdByProductiveStatus(clientFmcgId, orderCallStatus, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachOutletByClientFmcgIdByOrderMedium/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllByEachOutletByClientFmcgIdByOrderMedium(@PathVariable Long clientFmcgId, @RequestParam OrderMedium orderMedium,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOrderByEachOutletByClientFmcgIdByOrderMedium(clientFmcgId, orderMedium,page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachBeetByClientFmcgIdByProductiveStatus/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOrderByEachBeetByClientFmcgIdByProductiveStatus(@PathVariable Long clientFmcgId,@RequestParam OrderCallStatus orderCallStatus,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOrderByEachBeetByClientFmcgIdByProductiveStatus(clientFmcgId, orderCallStatus,page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachBeetByClientFmcgIdByOrderMedium/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOrderByEachBeetByClientFmcgIdByOrderMedium(@PathVariable Long clientFmcgId, @RequestParam OrderMedium orderMedium,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOrderByEachBeetByClientFmcgIdByOrderMedium(clientFmcgId, orderMedium,page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

}
