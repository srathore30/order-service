package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.dto.request.OrderRequest;
import sfa.order_service.dto.request.OrderUpdateRequest;
import sfa.order_service.dto.response.OrderResponse;
import sfa.order_service.dto.response.OrderUpdateResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.service.OrderService;

@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/orders")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        return new ResponseEntity<>(orderService.createOrder(orderRequest), HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<PaginatedResp<OrderResponse>> getOrderById(@PathVariable Long orderId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                                     @RequestParam(defaultValue = "createdDate") String sortBy,
                                                                     @RequestParam(defaultValue = "desc") String sortDirection) {
        return new ResponseEntity<>(orderService.getOrderById(orderId,page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }
    @PutMapping("/orders/{orderId}")
    public ResponseEntity<OrderUpdateResponse> updateOrder(@PathVariable Long orderId, @RequestBody OrderUpdateRequest orderRequest) {
        return new ResponseEntity<>(orderService.updateOrder(orderId,orderRequest), HttpStatus.OK);
    }
}
