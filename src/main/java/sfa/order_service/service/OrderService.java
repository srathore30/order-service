package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfa.order_service.constant.SalesLevelConstant;
import sfa.order_service.dto.request.FinalProductPriceRequest;
import sfa.order_service.dto.request.OrderRequest;
import sfa.order_service.dto.request.OrderUpdateRequest;
import sfa.order_service.dto.response.*;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.repo.OrderRepository;
import sfa.order_service.util.CalculateGst;
import sfa.order_service.util.DiscountUtil;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${price.priceBreakDown.url}")
    private final String priceBreakDownUrl;
    @Value("${price.getProduct.url}")
    private final String getProductUrl;

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
        return orderResponse;
    }

    public PaginatedResp<OrderResponse> getOrderById(Long orderId, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> byId = orderRepository.findById(orderId, pageable);
        List<OrderResponse> collect = byId.getContent().stream().map(this::entityToDto).collect(Collectors.toList());
        return PaginatedResp.<OrderResponse>builder().totalElements(byId.getTotalElements()).totalPages(byId.getTotalPages()).page(page).content(collect).build();
    }

    public OrderResponse updateOrder(Long orderId, OrderUpdateRequest request) {
        Optional<OrderEntity> byId = orderRepository.findById(orderId);
        if (byId.isEmpty()) {
            throw new RuntimeException("Order not found");
        }
        OrderEntity orderEntity = byId.get();
        orderEntity.setStatus(request.getStatus());
        return entityToDto(orderRepository.save(orderEntity));
    }

    public FinalProductPriceResponse calculateFinalPrice(FinalProductPriceRequest finalProductPriceRequest){
        ProductPriceRes productPriceRes = restTemplate.getForObject(priceBreakDownUrl + finalProductPriceRequest.getProductId(), ProductPriceRes.class);
        ProductRes productRes = restTemplate.getForObject(getProductUrl + finalProductPriceRequest.getProductId(), ProductRes.class);
        FinalProductPriceResponse finalRes = new FinalProductPriceResponse();
        assert productRes != null;
        if(finalProductPriceRequest.getSalesLevelConstant() == SalesLevelConstant.RETAILER){
            assert productPriceRes != null;
            finalRes.setUnitPrice(productPriceRes.getRetailerPrice());
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            float discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productPriceRes.getRetailerPrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            float discountedPRice  = finalProductPriceRequest.getQuantity() * productPriceRes.getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productPriceRes.getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);

        }
        if(finalProductPriceRequest.getSalesLevelConstant() == SalesLevelConstant.WAREHOUSE){
            assert productPriceRes != null;
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            float discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productPriceRes.getWarehousePrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            float discountedPRice  = finalProductPriceRequest.getQuantity() * productPriceRes.getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productPriceRes.getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);
        }
        if(finalProductPriceRequest.getSalesLevelConstant() == SalesLevelConstant.STOCKIST){
            assert productPriceRes != null;
            finalRes.setQuantity(finalProductPriceRequest.getQuantity());
            finalRes.setProductId(productRes.getProductId());
            finalRes.setMessage("Final price calculated successfully");
            float discount = DiscountUtil.calculateFinalPrice(finalProductPriceRequest.getQuantity() * productPriceRes.getStockListPrice(), finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
            float discountedPRice  = finalProductPriceRequest.getQuantity() * productPriceRes.getRetailerPrice() - discount;
            finalRes.setDiscountApplied(discount);
            finalRes.setSubTotal(discountedPRice);
            finalRes.setGstAmount(CalculateGst.calculateGstAmountFromTotal(discountedPRice, productPriceRes.getGstPercentage()));
            finalRes.setTotalPriceWithGst(finalRes.getGstAmount() + discountedPRice);
        }
        return finalRes;
    }

}
