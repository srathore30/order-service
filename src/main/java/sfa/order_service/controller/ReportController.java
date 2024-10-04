package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.ReportsRequest;
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
    @UserAuthorization(allowedRoles = {UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<ReportsResponse> getSalesReport(@RequestParam LocalDateTime startDate, @RequestParam LocalDateTime endDate, @RequestParam SalesLevel salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startDate, endDate, salesLevel);
        ReportsResponse reportsResponse = reportServices.getSalesReportBetweenDatesAndSalesLevel(reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }
}
