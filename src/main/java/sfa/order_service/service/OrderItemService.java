package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.request.OrderItemRequest;
import sfa.order_service.dto.request.OrderUpdateRequest;
import sfa.order_service.dto.response.OrderItemResponse;
import sfa.order_service.dto.response.ProductRes;
import sfa.order_service.entity.OrderItems;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.OrderItemsRepository;
import sfa.order_service.utill.DiscountUtil;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderItemService {
    private final OrderItemsRepository orderItemsRepository;
    private final OrderService orderService;
    private final ProductServiceClient productServiceClient;

    public OrderItems dtoToEntity(OrderItemRequest request) {
        log.info("Converting order item dto to entity");
        OrderItems entity = new OrderItems();
        entity.setOrderId(request.getOrderId());
        entity.setProductId(request.getProductId());
        entity.setQuantity(request.getQuantity());
        entity.setDiscountCoupon(request.getDiscountCoupon());
        entity.setSalesLevel(request.getSalesLevel());
        log.info("Get product details");
        ProductRes productRes = productServiceClient.getProduct(request.getProductId());
        log.info("Calculate price details according to Sales Level");
        if (request.getSalesLevel().equals(SalesLevel.STOCKIST)) {
            log.info("Calculate amount after discount applied and save in order item table");
            entity.setAmountAfterDiscount(DiscountUtil.calculateFinalPrice(productRes.getProductPriceRes().getStockListPrice(), request.getDiscountCoupon().getDiscountAmount()));
            log.info("Calculate amount before discount applied and save in order item table");
            entity.setAmountBeforeDiscount((productRes.getProductPriceRes().getStockListPrice()) * request.getQuantity());
            log.info("Calculate price of unit and save in order item table");
            entity.setPriceOfUnit(productRes.getProductPriceRes().getStockListPrice());
        } else if (request.getSalesLevel().equals(SalesLevel.WAREHOUSE)) {
            entity.setAmountAfterDiscount(DiscountUtil.calculateFinalPrice(productRes.getProductPriceRes().getWarehousePrice(), request.getDiscountCoupon().getDiscountAmount()));
            entity.setAmountBeforeDiscount((productRes.getProductPriceRes().getWarehousePrice()) * request.getQuantity());
            entity.setPriceOfUnit(productRes.getProductPriceRes().getWarehousePrice());
        } else if (request.getSalesLevel().equals(SalesLevel.RETAILER)) {
            entity.setAmountAfterDiscount(DiscountUtil.calculateFinalPrice(productRes.getProductPriceRes().getRetailerPrice(), request.getDiscountCoupon().getDiscountAmount()));
            entity.setAmountBeforeDiscount((productRes.getProductPriceRes().getRetailerPrice()) * request.getQuantity());
            entity.setPriceOfUnit(productRes.getProductPriceRes().getRetailerPrice());
        } else {
            throw new NoSuchElementFoundException(ApiErrorCodes.INVALID_SALES_LEVEL.getErrorCode(), ApiErrorCodes.INVALID_SALES_LEVEL.getErrorMessage());
        }
        log.info("Order item details saved in order item table");
        return entity;
    }

    public OrderItemResponse entityToDto(OrderItems entity) {
        log.info("Converting order item entity to response");
        OrderItemResponse response = new OrderItemResponse();
        response.setId(entity.getId());
        response.setOrderId(entity.getOrderId());
        response.setProductId(entity.getProductId());
        response.setQuantity(entity.getQuantity());
        response.setPriceOfUnit(entity.getPriceOfUnit());
        response.setDiscountCoupon(entity.getDiscountCoupon());
        response.setAmountAfterDiscount(entity.getAmountAfterDiscount());
        response.setAmountBeforeDiscount(entity.getAmountBeforeDiscount());
        response.setSalesLevel(entity.getSalesLevel());
        return response;
    }

    public OrderItemResponse createOrderItem(OrderItemRequest request) {
        log.info("Creating order item: {}", request);
        OrderItems orderItems = dtoToEntity(request);
        orderItemsRepository.save(orderItems);
        log.info("Order Item created successfully");
        return entityToDto(orderItems);
    }
}
