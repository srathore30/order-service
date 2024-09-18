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
import sfa.order_service.dto.response.FinalProductPriceResponse;
import sfa.order_service.dto.response.OrderResponse;
import sfa.order_service.dto.response.OrderUpdateResponse;
import sfa.order_service.dto.response.PaginatedResp;
import sfa.order_service.dto.response.ProductRes;
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
        return orderEntity;
    }

    public OrderResponse entityToDto(OrderEntity orderEntity, String message) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(orderEntity.getId());
        orderResponse.setStatus("create order".equals(message) ? OrderStatus.CREATED : orderEntity.getStatus());

        Double gstPercentage = productServiceClient.getProductPrice(orderEntity.getProductId(), "gst");
        Double finalPrice = switch (orderEntity.getSalesLevel()) {
            case RETAILER -> productServiceClient.getProductPrice(orderEntity.getProductId(), "retailer");
            case WAREHOUSE -> productServiceClient.getProductPrice(orderEntity.getProductId(), "warehouse");
            case STOCKIST -> productServiceClient.getProductPrice(orderEntity.getProductId(), "stocklist");
            default -> throw new InvalidInputException(ApiErrorCodes.INVALID_INPUT.getErrorCode(), ApiErrorCodes.INVALID_INPUT.getErrorMessage());
        };

        double totalPrice = orderEntity.getQuantity() * finalPrice;
        double gstAmount = (totalPrice * gstPercentage) / 100;
        double totalPriceWithGst = totalPrice + gstAmount;

        orderResponse.setTotalPrice(totalPrice);
        orderResponse.setGstAmount(gstAmount);
        orderResponse.setTotalPriceWithGst(totalPriceWithGst);
        orderResponse.setOrderCreatedDate(new Date());
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

    public FinalProductPriceResponse calculateFinalPrice(FinalProductPriceRequest finalProductPriceRequest){
        ProductRes productRes = productServiceClient.getProduct(finalProductPriceRequest.getProductId());
        FinalProductPriceResponse finalRes = new FinalProductPriceResponse();
        assert productRes != null;
        if(finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.RETAILER){
            finalRes.setUnitPrice(productRes.getProductPriceRes().getRetailerPrice());
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            double discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getRetailerPrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            double discountedPRice  = finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productRes.getProductPriceRes().getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);

        }
        if(finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.WAREHOUSE){
            finalRes.setUnitPrice(productRes.getProductPriceRes().getRetailerPrice());
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            double discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getWarehousePrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            double discountedPRice  = finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productRes.getProductPriceRes().getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);
        }
        if(finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.STOCKIST){
            finalRes.setUnitPrice(productRes.getProductPriceRes().getRetailerPrice());
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            double discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getStockListPrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            double discountedPRice  = finalProductPriceRequest.getQuantity() * productRes.getProductPriceRes().getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productRes.getProductPriceRes().getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);
        }
        return finalRes;
    }}
