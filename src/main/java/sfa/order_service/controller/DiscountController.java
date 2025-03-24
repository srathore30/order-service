package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.DiscountBulkReq;
import sfa.order_service.dto.request.DiscountRequest;
import sfa.order_service.dto.response.DiscountResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.DiscountService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discount")
public class DiscountController {
    private final DiscountService discountService;

    @PostMapping("createDiscount")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<DiscountResponse> createDiscount(@RequestBody DiscountRequest request) {
        return new ResponseEntity<>(discountService.createDiscount(request), HttpStatus.CREATED);
    }

    @GetMapping("getDiscountDetailsByProductId")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<PaginatedResp<DiscountResponse>> getDiscountDetailsByProductId(@RequestParam Long productId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection) {
        return new ResponseEntity<>(discountService.getDiscountDetailsByProductId(productId, page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }

    @PostMapping("/discounts/bulk")
    public ResponseEntity<List<DiscountResponse>> createBulkDiscount(@RequestBody DiscountBulkReq bulkRequest) {
        List<DiscountResponse> responses = discountService.createBulkDiscount(bulkRequest);
        return new ResponseEntity<>(responses,HttpStatus.OK);
    }

    @GetMapping("/getAll")
    public ResponseEntity<PaginatedResp<DiscountResponse>> getAllDiscounts(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "ASC") String sortDirection) {
        PaginatedResp<DiscountResponse> response = discountService.getAllDiscounts(page, pageSize, sortBy, sortDirection);
        return new ResponseEntity<>(response,HttpStatus.OK);
    }

    @PutMapping("/updateDiscountByProductId/{productId}")
    public ResponseEntity<DiscountResponse> updateDiscountByProductId(@PathVariable Long productId, @RequestBody DiscountRequest discountRequest) {
        DiscountResponse updatedDiscount = discountService.updateDiscountByProductId(productId, discountRequest);
        return new ResponseEntity<>(updatedDiscount,HttpStatus.OK);
    }
}
