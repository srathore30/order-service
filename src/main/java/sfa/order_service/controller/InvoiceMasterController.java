package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.dto.request.InvoiceMasterReq;
import sfa.order_service.dto.response.InvoiceMasterRes;
import sfa.order_service.service.InvoiceMasterService;

@RestController
@RequestMapping("/invoice-master")
@RequiredArgsConstructor
public class InvoiceMasterController {

    private final InvoiceMasterService invoiceMasterService;

    @PostMapping
    public ResponseEntity<InvoiceMasterRes> create(@RequestBody InvoiceMasterReq invoiceMasterReq) {
        InvoiceMasterRes response = invoiceMasterService.create(invoiceMasterReq);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<InvoiceMasterRes> get() {
        InvoiceMasterRes response = invoiceMasterService.get();
        return ResponseEntity.ok(response);
    }

    @PutMapping
    public ResponseEntity<InvoiceMasterRes> update(@RequestBody InvoiceMasterReq invoiceMasterReq) {
        InvoiceMasterRes response = invoiceMasterService.update(invoiceMasterReq);
        return ResponseEntity.ok(response);
    }
}
