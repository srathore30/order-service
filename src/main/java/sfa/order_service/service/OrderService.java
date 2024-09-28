package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.request.FinalProductPriceRequest;
import sfa.order_service.dto.request.OrderRequest;
import sfa.order_service.dto.request.OrderUpdateRequest;
import sfa.order_service.dto.response.*;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.enums.OrderStatus;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.exception.InvalidInputException;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.OrderRepository;
import sfa.order_service.utill.CalculateGst;
import sfa.order_service.utill.DiscountUtil;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    public String getPriceType(SalesLevel salesLevel) {
        return switch (salesLevel) {
            case RETAILER -> "retailer";
            case WAREHOUSE -> "warehouse";
            case STOCKIST -> "stocklist";
            default ->
                    throw new InvalidInputException(ApiErrorCodes.INVALID_INPUT.getErrorCode(), ApiErrorCodes.INVALID_INPUT.getErrorMessage());
        };
    }

    public Double getProductPrice(Long productId, String priceType) {
        return productServiceClient.getProductPrice(productId, priceType);
    }

    public OrderResponse createOrder(OrderRequest request) {
        String message = "create order";
        log.info("Creating order: {}", request);
        OrderEntity entity = orderRepository.save(dtoToEntity(request));
        return entityToDto(entity, message);
    }

    public OrderEntity dtoToEntity(OrderRequest request) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setQuantity(request.getQuantity());
        orderEntity.setSalesLevel(request.getSalesLevel());
        orderEntity.setProductId(request.getProductId());
        Double priceOfOrderWithRespectedSalesLevel = getProductPrice(request.getProductId(), getPriceType(request.getSalesLevel()));
        Double totalPriceOfOrder = priceOfOrderWithRespectedSalesLevel * request.getQuantity();
        Double gstOnOrder = getProductPrice(request.getProductId(), "gst");
        Double finalPrice = totalPriceOfOrder + gstOnOrder;
        orderEntity.setPrice(finalPrice);
        orderEntity.setOrderCreatedDate(new Date());
        return orderEntity;
    }

    public OrderResponse entityToDto(OrderEntity orderEntity, String message) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(orderEntity.getId());
        orderResponse.setStatus("create order".equals(message) ? OrderStatus.CREATED : orderEntity.getStatus());
        Double gstOnOrder = getProductPrice(orderEntity.getProductId(), "gst");
        orderResponse.setGstAmount(gstOnOrder);
        orderResponse.setTotalPriceWithGst(orderEntity.getPrice());
        orderResponse.setTotalPrice(orderEntity.getPrice() - gstOnOrder);
        orderResponse.setOrderCreatedDate(orderEntity.getOrderCreatedDate());
        return orderResponse;
    }

    public PaginatedResp<OrderResponse> getOrderById(Long orderId, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderById = orderRepository.findById(orderId, pageable);
        if (orderById.isEmpty()) {
            throw new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage());
        }
        List<OrderResponse> collect = orderById.getContent().stream().map(orderEntity -> entityToDto(orderEntity, "")).collect(Collectors.toList());
        return PaginatedResp.<OrderResponse>builder().totalElements(orderById.getTotalElements()).totalPages(orderById.getTotalPages()).page(page).content(collect).build();
    }

    public OrderUpdateResponse updateOrder(Long orderId, OrderUpdateRequest request) {
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage()));
        orderEntity.setStatus(request.getStatus());
        OrderEntity updatedOrder = orderRepository.save(orderEntity);
        OrderUpdateResponse orderResponse = new OrderUpdateResponse();
        orderResponse.setOrderId(updatedOrder.getId());
        orderResponse.setStatus(updatedOrder.getStatus());
        orderResponse.setMessage("Order status updated to delivered!!");
        return orderResponse;
    }

    public FinalProductPriceResponse calculateFinalPrice(FinalProductPriceRequest finalProductPriceRequest) {
        ProductRes productRes = productServiceClient.getProduct(finalProductPriceRequest.getProductId());
        FinalProductPriceResponse finalRes = new FinalProductPriceResponse();
        assert productRes != null;
        if (finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.RETAILER) {
            finalRes.setUnitPrice(productRes.getProductPriceRes().getRetailerPrice());
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            double discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getRetailerPrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            double discountedPRice = finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productRes.getProductPriceRes().getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);

        }
        if (finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.WAREHOUSE) {
            finalRes.setUnitPrice(productRes.getProductPriceRes().getRetailerPrice());
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            double discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getWarehousePrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            double discountedPRice = finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productRes.getProductPriceRes().getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);
        }
        if (finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.STOCKIST) {
            finalRes.setUnitPrice(productRes.getProductPriceRes().getRetailerPrice());
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            double discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getStockListPrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            double discountedPRice = finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productRes.getProductPriceRes().getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);
        }
        return finalRes;
    }
}
