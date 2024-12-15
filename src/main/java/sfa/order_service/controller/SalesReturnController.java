package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.SalesReturnReq;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.dto.response.SalesReturnRes;
import sfa.order_service.entity.ReturnStatus;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.SalesReturnServices;

@RestController
@RequiredArgsConstructor
@RequestMapping("/sales-returns")
public class SalesReturnController {

    private final SalesReturnServices salesReturnServices;

    @PostMapping
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<SalesReturnRes> createSalesReturn(@RequestBody SalesReturnReq salesReturnReq) {
        return new ResponseEntity<>(salesReturnServices.createReturn(salesReturnReq), HttpStatus.CREATED);
    }

    @GetMapping("/returnByOrderId/{orderId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<SalesReturnRes> getSalesReturnByOrderId(@PathVariable Long orderId) {
        return new ResponseEntity<>(salesReturnServices.getReturnByOrderId(orderId), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<SalesReturnRes> getSalesReturnById(@PathVariable Long id) {
        return new ResponseEntity<>(salesReturnServices.getReturnById(id), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<SalesReturnRes> updateSalesReturnById(@PathVariable Long id, @RequestBody SalesReturnReq salesReturnReq) {
        return new ResponseEntity<>(salesReturnServices.updateReturnById(id, salesReturnReq), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<Void> deleteSalesReturn(@PathVariable Long id) {
        salesReturnServices.deleteReturn(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{id}/status")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<Void> updateSalesReturnStatus(@PathVariable Long id, @RequestParam ReturnStatus returnStatus) {
        salesReturnServices.updateReturnStatus(id, returnStatus);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<SalesReturnRes>> getAllSalesReturnsByClientFmcgAndSalesLevel(
            @RequestParam Long clientFmcgId, @RequestParam Long memberId,
            @RequestParam ReturnStatus returnStatus,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "returnDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection) {

        return new ResponseEntity<>(salesReturnServices.getAllReturnByClientFmcgAndSalesLevelAndReturnStatus(
                clientFmcgId, memberId, returnStatus, page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }
}
