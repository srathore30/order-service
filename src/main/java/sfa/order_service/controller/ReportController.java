package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import sfa.order_service.constant.SalesLevelConstant;
import sfa.order_service.dto.request.ReportsRequest;
import sfa.order_service.dto.response.ReportsResponse;
import sfa.order_service.service.ReportServices;

import java.text.ParseException;
import java.util.Date;

@RestController
@RequestMapping("/v1/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportServices reportServices;

    @GetMapping("/sales")
    public ResponseEntity<ReportsResponse> getSalesReport(@RequestParam Date startdate, @RequestParam Date endDate, @RequestParam SalesLevelConstant salesLevel) throws ParseException {
        ReportsRequest reportsRequest = new ReportsRequest(startdate, endDate, salesLevel);
        ReportsResponse reportsResponse = reportServices.getSalesReportBetweenDatesAndSalesLevel(reportsRequest);
        return new ResponseEntity<>(reportsResponse, HttpStatus.OK);
    }
}
