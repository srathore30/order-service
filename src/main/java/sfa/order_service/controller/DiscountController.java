package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.dto.request.DiscountRequest;
import sfa.order_service.dto.response.DiscountResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.service.DiscountService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/discount")
public class DiscountController {
    private final DiscountService discountService;
    @PostMapping("createDiscount")
    public ResponseEntity<DiscountResponse> createDiscount(@RequestBody DiscountRequest request) {
        return new ResponseEntity<>(discountService.createDiscount(request), HttpStatus.CREATED);
    }
    @GetMapping("getDiscountDetailsByProductId")
    public ResponseEntity<PaginatedResp<DiscountResponse>> getDiscountDetailsByProductId(@RequestParam Long productId,
                                                                                         @RequestParam(defaultValue = "0") int page,
                                                                                         @RequestParam(defaultValue = "10") int pageSize,
                                                                                         @RequestParam(defaultValue = "createdDate") String sortBy,
                                                                                         @RequestParam(defaultValue = "desc") String sortDirection) {
        return new ResponseEntity<>(discountService.getDiscountDetailsByProductId(productId, page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }


}
