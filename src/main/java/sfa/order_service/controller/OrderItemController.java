package sfa.order_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sfa.order_service.constant.UserRole;
import sfa.order_service.dto.request.OrderItemRequest;
import sfa.order_service.dto.response.OrderItemResponse;
import sfa.order_service.interceptor.UserAuthorization;
import sfa.order_service.service.OrderItemService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/order-item")
public class OrderItemController {
    private final OrderItemService orderItemService;

    @PostMapping
    @UserAuthorization(allowedRoles = {UserRole.Client,UserRole.Create_Manager, UserRole.Edit_Manager, UserRole.Delete_Manager, UserRole.View_Manager, UserRole.Manager, UserRole.Reporting_Manager, UserRole.Super_Admin})

    public ResponseEntity<OrderItemResponse> createOrderItem(@RequestBody OrderItemRequest request) {
        return new ResponseEntity<>(orderItemService.createOrderItem(request), HttpStatus.OK);
    }
    }
