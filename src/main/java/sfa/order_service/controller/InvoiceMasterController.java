package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.InvoiceMasterReq;
import sfa.order_service.dto.response.InvoiceMasterRes;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.InvoiceMasterService;

@RestController
@RequestMapping("/invoice-master")
@RequiredArgsConstructor
public class InvoiceMasterController {

    private final InvoiceMasterService invoiceMasterService;

    @PostMapping
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG, UserRole.Client, UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<InvoiceMasterRes> create(@RequestBody InvoiceMasterReq invoiceMasterReq) {
        InvoiceMasterRes response = invoiceMasterService.create(invoiceMasterReq);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG, UserRole.Client, UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<InvoiceMasterRes> get() {
        InvoiceMasterRes response = invoiceMasterService.get();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG, UserRole.Client, UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})
    public ResponseEntity<InvoiceMasterRes> update(@RequestBody InvoiceMasterReq invoiceMasterReq) {
        InvoiceMasterRes response = invoiceMasterService.update(invoiceMasterReq);
        return ResponseEntity.ok(response);
    }
}
