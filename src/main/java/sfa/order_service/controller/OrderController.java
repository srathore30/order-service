package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.*;
import sfa.order_service.dto.response.FinalProductPriceResponse;
import sfa.order_service.dto.response.OrderResponse;
import sfa.order_service.dto.response.OrderUpdateResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.OrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest orderRequest, @RequestParam String salesType) {
        return new ResponseEntity<>(orderService.createOrder(orderRequest, salesType), HttpStatus.OK);
    }

    @GetMapping("/orders/{orderId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<OrderResponse>> getOrderById(@PathVariable Long orderId,
                                                                     @RequestParam(defaultValue = "0") int page,
                                                                     @RequestParam(defaultValue = "10") int pageSize,
                                                                     @RequestParam(defaultValue = "createdDate") String sortBy,
                                                                     @RequestParam(defaultValue = "desc") String sortDirection) {
        return new ResponseEntity<>(orderService.getOrderById(orderId, page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }

    @PutMapping("/orders/{orderId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<OrderUpdateResponse> updateOrder(@PathVariable Long orderId, @RequestBody OrderUpdateRequest orderRequest) {
        return new ResponseEntity<>(orderService.updateOrder(orderId, orderRequest), HttpStatus.OK);
    }

    @PostMapping("/orders/pricing/calculate")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<FinalProductPriceResponse> calculateFinalPrice(@RequestBody FinalProductPriceRequest finalProductPriceRequest) {
        return new ResponseEntity<>(orderService.calculateFinalPrice(finalProductPriceRequest), HttpStatus.OK);
    }
    @PutMapping("rechargeClientBalance")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<String> rechargeClientBalance(@RequestBody ClientFMCGUpdateRequest request) {
       return new ResponseEntity<>(orderService.rechargeClientBalance(request), HttpStatus.OK);
    }

    @GetMapping("/getAllOrder/member/{memberId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<OrderResponse>> getAllOrderByMemberId(@PathVariable Long memberId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection) {
        return new ResponseEntity<>(orderService.getAllOrderByMemberId(memberId, page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }

    @GetMapping("/getAllOrderByReportingManagerMembers/{reportingManagerId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<OrderResponse>> getAllOrderByReportingManagerMembers(@PathVariable Long reportingManagerId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection) {
        return new ResponseEntity<>(orderService.getAllOrderByReportingManagerMembers(reportingManagerId, page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }

    @GetMapping("/getAllOrder/client-fmcg/{clientFmcgId}")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<OrderResponse>> getAllOrderByClientFmcgId(@PathVariable Long clientFmcgId, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection) {
        return new ResponseEntity<>(orderService.getAllOrderByClientFmcgId(clientFmcgId, page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }
    @GetMapping("getAllOrderByClientFmcgIdAndSalesLevel")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<PaginatedResp<OrderResponse>> getAllOrderByClientFmcgIdAndSalesLevel(@RequestParam Long clientFmcgId,@RequestParam String salesLevel,@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int pageSize, @RequestParam(defaultValue = "createdDate") String sortBy, @RequestParam(defaultValue = "desc") String sortDirection){
        return new ResponseEntity<>(orderService.getAllOrderByClientFmcgIdAndSalesLevel(clientFmcgId,salesLevel, page, pageSize, sortBy, sortDirection), HttpStatus.OK);
    }

    @PostMapping("/createOrderInBulk")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<List<OrderResponse>> createOrderInBulk(@RequestBody OrderBulkReq orderBulkReq, @RequestParam String salesType){
        List<OrderResponse> orderResponseList = orderService.createOrderInBulk(orderBulkReq, salesType);
        return new ResponseEntity<>(orderResponseList, HttpStatus.OK);
    }

    @PutMapping("/updateOrderInBulk")
    @UserAuthorization(allowedRoles = {UserRole.ClientFMCG,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager,UserRole.View_Manager,UserRole.Manager})
    public ResponseEntity<List<OrderUpdateResponse>> updateOrderInBulk(@RequestBody OrderBulkUpdateRequest orderUpdateRequest){
        List<OrderUpdateResponse> orderResponseList = orderService.updateOrderInBulk(orderUpdateRequest);
        return new ResponseEntity<>(orderResponseList, HttpStatus.OK);
    }
}
