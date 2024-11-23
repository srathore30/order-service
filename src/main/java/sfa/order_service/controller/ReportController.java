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
@RequestMapping("reports")
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

    @GetMapping("/getAllProductiveOrderByEachOutletByMemberId/{member}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllProductiveOrderByEachOutletByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllProductiveOrderByEachOutletByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllNonProductiveOrderByEachOutletByMemberId/{member}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllNonProductiveOrderByEachOutletByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllNonProductiveOrderByEachOutletByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOnCallOrderByEachOutletByMemberId/{member}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOnCallOrderByEachOutletByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOnCallOrderByEachOutletByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOnSiteOrderByEachOutletByMemberId/{member}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOnSiteOrderByEachOutletByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOnSiteOrderByEachOutletByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllProductiveOrderByEachBeetByMemberId/{member}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllProductiveOrderByEachBeetByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllProductiveOrderByEachBeetByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllNonProductiveOrderByEachBeetByMemberId/{member}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllNonProductiveOrderByEachBeetByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllNonProductiveOrderByEachBeetByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOnCallOrderByEachBeetByMemberId/{member}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOnCallOrderByEachBeetByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOnCallOrderByEachBeetByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOnSiteOrderByEachBeetByMemberId/{member}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOnSiteOrderByEachBeetByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOnSiteOrderByEachBeetByMemberId(memberId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }


    @GetMapping("/getAllProductiveOrderByEachOutletByClientFmcgId/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllProductiveOrderByEachOutletByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllProductiveOrderByEachOutletByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllNonProductiveOrderByEachOutletByClientFmcgId/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllNonProductiveOrderByEachOutletByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllNonProductiveOrderByEachOutletByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOnCallOrderByEachOutletByClientFmcgId/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOnCallOrderByEachOutletByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOnCallOrderByEachOutletByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOnSiteOrderByEachOutletByClientFmcgId/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOnSiteOrderByEachOutletByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOnSiteOrderByEachOutletByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllProductiveOrderByEachBeetByClientFmcgId/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllProductiveOrderByEachBeetByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllProductiveOrderByEachBeetByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllNonProductiveOrderByEachBeetByClientFmcgId/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllNonProductiveOrderByEachBeetByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllNonProductiveOrderByEachBeetByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOnCallOrderByEachBeetByClientFmcgId/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOnCallOrderByEachBeetByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOnCallOrderByEachBeetByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOnSiteOrderByEachBeetByClientFmcgId/{clientFmcgId}")
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOnSiteOrderByEachBeetByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOnSiteOrderByEachBeetByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

}
