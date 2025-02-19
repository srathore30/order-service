package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.ReportsRequest;
import sfa.order_service.dto.response.*;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.ReportServices;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportServices reportServices;

    @GetMapping("/sales")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<ReportsResponse> getSalesReport(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        ReportsResponse reportsResponse = reportServices.getSalesReportBetweenDatesAndSalesLevel(reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/getBeetOrderReportByMemberIdWithDateFilter/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getBeetOrderReportByMemberIdWithDateFilter(@PathVariable Long memberId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> beetReportResponsePaginatedResp = reportServices.getBeetOrderReportByMemberIdWithDateFilter(memberId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(beetReportResponsePaginatedResp, HttpStatus.OK);
    }

    @GetMapping("/getBeetOrderReportByReportingManagerIdWithDateFilter/{reportingManagerId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getBeetOrderReportByReportingManagerIdWithDateFilter(@PathVariable Long reportingManagerId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> beetReportResponsePaginatedResp = reportServices.getBeetOrderReportByReportingManagerIdWithDateFilter(reportingManagerId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(beetReportResponsePaginatedResp, HttpStatus.OK);
    }

    @GetMapping("/getOutletOrderReportByBeetIdWithDateFilter/{beetId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getOutletOrderReportByBeetIdWithDateFilter(@PathVariable Long beetId, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate, @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> outletOrderReportByBeetIdWithDateFilter = reportServices.getOutletOrderReportByBeetIdWithDateFilter(beetId, startDate, endDate, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(outletOrderReportByBeetIdWithDateFilter, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachOutletByMemberIdByProductiveStatus/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOrderByEachOutletByMemberIdByProductiveStatus(@PathVariable Long memberId, @RequestParam OrderCallStatus orderCallStatus, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOrderByEachOutletByMemberIdByProductiveStatus(memberId, orderCallStatus, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachOutletByMemberIdByOrderMedium/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOrderByEachOutletByMemberIdByOrderMedium(@PathVariable Long memberId, @RequestParam OrderMedium orderMedium, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOrderByEachOutletByMemberIdByOrderMedium(memberId, orderMedium, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachBeetByMemberIdByProductiveStatus/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOrderByEachBeetByMemberIdByProductiveStatus(@PathVariable Long memberId, @RequestParam OrderCallStatus orderCallStatus, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOrderByEachBeetByMemberIdByProductiveStatus(memberId, orderCallStatus, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachBeetByMemberIdByOrderMedium/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOrderByEachBeetByMemberIdByOrderMedium(@PathVariable Long memberId, @RequestParam OrderMedium orderMedium,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOrderByEachBeetByMemberIdByOrderMedium(memberId, orderMedium, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }


    @GetMapping("/getAllOrderByEachOutletByClientFmcgIdByProductiveStatus/{clientFmcgId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllOrderByEachOutletByClientFmcgIdByProductiveStatus(@PathVariable Long clientFmcgId, @RequestParam OrderCallStatus orderCallStatus,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOrderByEachOutletByClientFmcgIdByProductiveStatus(clientFmcgId, orderCallStatus, page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachOutletByClientFmcgIdByOrderMedium/{clientFmcgId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<OutletReportResponse>> getAllByEachOutletByClientFmcgIdByOrderMedium(@PathVariable Long clientFmcgId, @RequestParam OrderMedium orderMedium,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<OutletReportResponse> report = reportServices.getAllOrderByEachOutletByClientFmcgIdByOrderMedium(clientFmcgId, orderMedium,page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachBeetByClientFmcgIdByProductiveStatus/{clientFmcgId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOrderByEachBeetByClientFmcgIdByProductiveStatus(@PathVariable Long clientFmcgId,@RequestParam OrderCallStatus orderCallStatus,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOrderByEachBeetByClientFmcgIdByProductiveStatus(clientFmcgId, orderCallStatus,page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByEachBeetByClientFmcgIdByOrderMedium/{clientFmcgId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<BeetReportResponse>> getAllOrderByEachBeetByClientFmcgIdByOrderMedium(@PathVariable Long clientFmcgId, @RequestParam OrderMedium orderMedium,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        PaginatedResp<BeetReportResponse> report = reportServices.getAllOrderByEachBeetByClientFmcgIdByOrderMedium(clientFmcgId, orderMedium,page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(report, HttpStatus.OK);
    }

    @GetMapping("/overall-sales/byDateAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<OrderResponse>> findOverallSalesByDateAndSalesLevel(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<OrderResponse> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevel(reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }
    @GetMapping("/overall-sales/region/byDateAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<OrderResponse>> findOverallSalesByDateAndSalesLevelAndRegion(@RequestParam Long regionId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<OrderResponse> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelAndRegion(reportsRequest, regionId);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/overall-sales/state/byDateAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<OrderResponse>> findOverallSalesByDateAndSalesLevelAndState(@RequestParam Long stateId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<OrderResponse> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelAndState(reportsRequest, stateId);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/overall-sales/city/byDateAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<OrderResponse>> findOverallSalesByDateAndSalesLevelAndCity(@RequestParam Long cityId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<OrderResponse> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelAndCity(reportsRequest, cityId);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/overall-sales/byDateAndSalesLevelAndOutletId/{outletId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<OrderResponse>> findOverallSalesByDateAndSalesLevelAndOutletId(@PathVariable Long outletId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<OrderResponse> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelAndOutletId(outletId, reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/overall-sales/byDateAndSalesLevelAndClientFmcgId/{clientFmcgId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<OrderResponse>> findOverallSalesByDateAndSalesLevelAndClientFmcgId(@PathVariable Long clientFmcgId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<OrderResponse> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelAndClientFmcgId(clientFmcgId, reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/overall-sales/graph/byDateAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<SalesResForGraph>> findOverallSalesByDateAndSalesLevelForGraph(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<SalesResForGraph> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelForGraph(reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }
    @GetMapping("/overall-sales/graph/region/byDateAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<SalesResForGraph>> findOverallSalesByDateAndSalesLevelForGraphAndRegion(@RequestParam Long regionId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<SalesResForGraph> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelForGraphForRegion(reportsRequest, regionId);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/overall-sales/graph/state/byDateAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<SalesResForGraph>> findOverallSalesByDateAndSalesLevelForGraphAndState(@RequestParam Long stateId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<SalesResForGraph> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelForGraphForState(reportsRequest, stateId);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }

    @GetMapping("/overall-sales/graph/city/byDateAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<List<SalesResForGraph>> findOverallSalesByDateAndSalesLevelForGraphAndCity(@RequestParam Long cityId, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        List<SalesResForGraph> reportsResponse = reportServices.findOverallSalesByDateAndSalesLevelForGraphForCity(reportsRequest, cityId);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }


}
