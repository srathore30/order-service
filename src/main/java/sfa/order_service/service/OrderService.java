package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.request.OrderRequest;
import sfa.order_service.dto.request.OrderUpdateRequest;
import sfa.order_service.dto.response.OrderResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.dto.response.ProductRes;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.OrderRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    Float finalPrice;

    public OrderResponse createOrder(OrderRequest request) {
        log.info("Creating order: {}", request);
        OrderEntity entity = orderRepository.save(dtoToEntity(request));
        return entityToDto(entity);
    }

    public OrderEntity dtoToEntity(OrderRequest request) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setQuantity(request.getQuantity());
        orderEntity.setSalesLevel(request.getSalesLevel());
        orderEntity.setProductId(request.getProductId());
        return orderEntity;
    }

    public OrderResponse entityToDto(OrderEntity orderEntity) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setStatus(orderEntity.getStatus());
        orderResponse.setOrderId(orderEntity.getId());
        ProductRes productById = productServiceClient.getProductById(orderEntity.getProductId());
            Float gstPercentage = productById.getProductPriceRes().getGstPercentage();
        if(gstPercentage == null){
            gstPercentage = 0f;
        }
        if(orderEntity.getSalesLevel().equals(SalesLevel.RETAILER)){
             finalPrice = productById.getProductPriceRes().getRetailerPrice();
        }else if(orderEntity.getSalesLevel().equals(SalesLevel.WAREHOUSE)){
             finalPrice = productById.getProductPriceRes().getWarehousePrice();
        }else if(orderEntity.getSalesLevel().equals(SalesLevel.STOCKIST)){
             finalPrice = productById.getProductPriceRes().getStockListPrice();
        }else{
            throw new NoSuchElementFoundException(ApiErrorCodes.NOT_FOUND.getErrorCode(), ApiErrorCodes.NOT_FOUND.getErrorMessage());
        }
        orderResponse.setTotalPrice((double) (orderEntity.getQuantity() * finalPrice));

        orderResponse.setGstAmount((double) (orderEntity.getQuantity() * finalPrice * gstPercentage));
        orderResponse.setTotalPriceWithGst(orderEntity.getQuantity() * finalPrice + orderResponse.getGstAmount());
        orderResponse.setOrderCreatedDate(new Date());
        return orderResponse;
    }

    public PaginatedResp<OrderResponse> getOrderById(Long orderId, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> byId = orderRepository.findById(orderId, pageable);
        if (byId.isEmpty()) {
            throw new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage());
        }
        List<OrderResponse> collect = byId.getContent().stream().map(this::entityToDto).collect(Collectors.toList());
        return PaginatedResp.<OrderResponse>builder().totalElements(byId.getTotalElements()).totalPages(byId.getTotalPages()).page(page).content(collect).build();
    }

    public OrderResponse updateOrder(Long orderId, OrderUpdateRequest request) {
        Optional<OrderEntity> byId = orderRepository.findById(orderId);
        if (byId.isEmpty()) {
            throw new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage());
        }
        OrderEntity orderEntity = byId.get();
        orderEntity.setStatus(request.getStatus());
        return entityToDto(orderRepository.save(orderEntity));
    }

}
