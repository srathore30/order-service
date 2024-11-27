package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.controller.TransactionController;
import sfa.order_service.dto.request.*;
import sfa.order_service.dto.response.*;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.enums.OrderStatus;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.enums.TransactionType;
import sfa.order_service.exception.InvalidInputException;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.OrderRepository;
import sfa.order_service.repo.TransactionRepository;
import sfa.order_service.utill.CalculateGst;
import sfa.order_service.utill.DiscountUtil;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final ExternalRestService externalRestService;
    private final TransactionController transactionController;

    public String getPriceType(SalesLevel salesLevel) {
        log.info("Get price type for sales level: {}", salesLevel);
        return switch (salesLevel) {
            case RETAILER -> "retailer";
            case WAREHOUSE -> "warehouse";
            case STOCKIST -> "stocklist";
            default ->
                    throw new InvalidInputException(ApiErrorCodes.INVALID_INPUT.getErrorCode(), ApiErrorCodes.INVALID_INPUT.getErrorMessage());
        };
    }

    public Double getProductPrice(Long productId, String priceType) {
        log.info("Get product price with product id: {} and price type: {}", productId, priceType);
        return productServiceClient.getProductPrice(productId, priceType);
    }

    public OrderResponse createOrder(OrderRequest request) {
        String message = "create order";
        log.info("Creating order: {}", request);
        OrderEntity entity = orderRepository.save(dtoToEntity(request));
        log.info("create transaction before order creation");
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setClientId(request.getClientId());
        transactionRequest.setTransactionAmount(finalPrice(request));
        transactionRequest.setTransactionType(TransactionType.DEBIT);
        transactionRequest.setOrderId(entity.getId());
        log.info("create transaction after order creation");
        transactionController.createTransaction(transactionRequest);
        return entityToDto(entity, message);
    }

    public Double finalPrice(OrderRequest request) {
        log.info("Calculate final price for order");
        Double priceOfOrderWithRespectedSalesLevel = getProductPrice(request.getProductId(), getPriceType(request.getSalesLevel()));
        double totalPriceOfOrder = priceOfOrderWithRespectedSalesLevel * request.getQuantity();
        Double gstOnOrder = getProductPrice(request.getProductId(), "gst");
        return totalPriceOfOrder + (totalPriceOfOrder * gstOnOrder) / 100;
    }

    public OrderEntity dtoToEntity(OrderRequest request) {
        log.info("calculate final price for order");
        Double finalPrice = finalPrice(request);
        log.info("Get FMCG-client details for order creation");
        ClientFMCGResponse client = externalRestService.getClient(request.getClientId());
        log.info("check if FMCG-client exists or not");
        if (client == null) {
            throw new InvalidInputException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), ApiErrorCodes.CLIENT_NOT_FOUND.getErrorMessage());
        }
        log.info("check if client has sufficient balance or not");
        if (client.getTopUpBalance() < finalPrice) {
            throw new InvalidInputException(ApiErrorCodes.INSUFFICIENT_BALANCE.getErrorCode(), ApiErrorCodes.INSUFFICIENT_BALANCE.getErrorMessage());
        }
        log.info("Get outlet details for order creation");
        String outletById = externalRestService.getOutletById(request.getOutletId());
        if (outletById.isEmpty()) {
            throw new InvalidInputException(ApiErrorCodes.OUTLET_NOT_FOUND.getErrorCode(), ApiErrorCodes.OUTLET_NOT_FOUND.getErrorMessage());
        }
        log.info("Get beets details for order creation");
        String beetById = externalRestService.getBeetById(request.getBeetId());
        if (beetById.isEmpty()) {
            throw new InvalidInputException(ApiErrorCodes.BEET_NOT_FOUND.getErrorCode(), ApiErrorCodes.BEET_NOT_FOUND.getErrorMessage());
        }
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setClientFmcgId(request.getClientId());
        orderEntity.setQuantity(request.getQuantity());
        orderEntity.setSalesLevel(request.getSalesLevel());
        orderEntity.setProductId(request.getProductId());
        orderEntity.setPrice(finalPrice);
        orderEntity.setOrderMedium(request.getOrderMedium());
        orderEntity.setOrderCallStatus(request.getOrderCallStatus());
        orderEntity.setOrderCreatedDate(new Date());
        orderEntity.setOutletId(request.getOutletId());
        orderEntity.setBeetId(request.getBeetId());
        log.info("Get member details for order creation");
        MemberResponse member = externalRestService.getMember(request.getMemberId());
        log.info("check if member exists or not");
        if (member == null) {
            throw new InvalidInputException(ApiErrorCodes.MEMBER_NOT_FOUND.getErrorCode(), ApiErrorCodes.MEMBER_NOT_FOUND.getErrorMessage());
        }
        orderEntity.setMemberId(request.getMemberId());
        log.info("Updating FMCG-client balance after order creation");
        ClientFMCGUpdateRequest clientFMCGUpdateRequest = new ClientFMCGUpdateRequest();
        clientFMCGUpdateRequest.setId(request.getClientId());
        clientFMCGUpdateRequest.setTopUpBalance(client.getTopUpBalance() - finalPrice);
        clientFMCGUpdateRequest.setClientCode(client.getClientCode());
        clientFMCGUpdateRequest.setCity(client.getCity());
        clientFMCGUpdateRequest.setRegion(client.getRegion());
        clientFMCGUpdateRequest.setEmail(client.getEmail());
        clientFMCGUpdateRequest.setClientFirstName(client.getClientFirstName());
        clientFMCGUpdateRequest.setClientLastName(client.getClientLastName());
        clientFMCGUpdateRequest.setMobile(client.getMobile());
        clientFMCGUpdateRequest.setAddress(client.getAddress());
        clientFMCGUpdateRequest.setState(client.getState());
        externalRestService.updateClientAsync(clientFMCGUpdateRequest);
        log.info("Make request for transaction  table after order creation");
        return orderEntity;
    }

    public String rechargeClientBalance(ClientFMCGUpdateRequest request) {
        log.info("Recharge FMCG-client balance");
        ClientFMCGResponse client = externalRestService.getClient(request.getId());
        ClientFMCGUpdateRequest clientFMCGUpdateRequest = new ClientFMCGUpdateRequest();
        clientFMCGUpdateRequest.setId(request.getId());
        clientFMCGUpdateRequest.setTopUpBalance(client.getTopUpBalance() + request.getTopUpBalance());
        clientFMCGUpdateRequest.setClientCode(request.getClientCode());
        clientFMCGUpdateRequest.setCity(client.getCity());
        clientFMCGUpdateRequest.setRegion(client.getRegion());
        clientFMCGUpdateRequest.setEmail(client.getEmail());
        clientFMCGUpdateRequest.setClientFirstName(client.getClientFirstName());
        clientFMCGUpdateRequest.setClientLastName(client.getClientLastName());
        clientFMCGUpdateRequest.setMobile(client.getMobile());
        clientFMCGUpdateRequest.setAddress(client.getAddress());
        clientFMCGUpdateRequest.setState(client.getState());
        externalRestService.updateClientAsync(clientFMCGUpdateRequest);
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setClientId(request.getId());
        transactionRequest.setTransactionAmount(request.getTopUpBalance());
        transactionRequest.setTransactionType(TransactionType.CREDIT);
        log.info("create transaction after order creation");
        transactionController.createTransaction(transactionRequest);
        return "Recharge successful";
    }

    public OrderResponse entityToDto(OrderEntity orderEntity, String message) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setOrderId(orderEntity.getId());
        orderResponse.setStatus("create order".equals(message) ? OrderStatus.CREATED : orderEntity.getStatus());
        Double gstOnOrder = getProductPrice(orderEntity.getProductId(), "gst");
        orderResponse.setGstAmount(gstOnOrder);
        orderResponse.setOrderMedium(orderEntity.getOrderMedium());
        orderResponse.setOrderCallStatus(orderEntity.getOrderCallStatus());
        orderResponse.setTotalPriceWithGst(orderEntity.getPrice());
        Double priceOfOrderWithRespectedSalesLevel = getProductPrice(orderEntity.getProductId(), getPriceType(orderEntity.getSalesLevel()));
        orderResponse.setTotalPrice(priceOfOrderWithRespectedSalesLevel * orderEntity.getQuantity());
        orderResponse.setOrderCreatedDate(orderEntity.getOrderCreatedDate());
        orderResponse.setClientId(orderEntity.getClientFmcgId());
        MemberResponse member = externalRestService.getMember(orderEntity.getMemberId());
        orderResponse.setMemberId(orderEntity.getMemberId());
        orderResponse.setMemberName(member.getFirstName() + " " + member.getLastName());
        ClientFMCGResponse client = externalRestService.getClient(orderEntity.getClientFmcgId());
        orderResponse.setClientName(client.getClientFirstName() + " " + client.getClientLastName());
        orderResponse.setClientBalanceAmount(client.getTopUpBalance());
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
        log.info("update order status");
        OrderEntity orderEntity = orderRepository.findById(orderId).orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage()));
        orderEntity.setStatus(request.getStatus());
        log.info("Order status updated to {}", request.getStatus());
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


    public PaginatedResp<OrderResponse> getAllOrderByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection){
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllOrderByClientFmcgId");
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgId(clientFmcgId, pageable);
        List<OrderResponse> orderResponseList = orderEntityPage.stream().map(orderEntity -> entityToDto(orderEntity, "Message")).toList();
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, orderResponseList);
    }

    public PaginatedResp<OrderResponse> getAllOrderByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection){
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllOrderByMemberId");
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberId(memberId, pageable);
        List<OrderResponse> orderResponseList = orderEntityPage.stream().map(orderEntity -> entityToDto(orderEntity, "Message")).toList();
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, orderResponseList);
    }

    public PaginatedResp<OrderResponse> getAllOrderByClientFmcgIdAndSalesLevel(Long clientFmcgId, String salesLevel, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllOrderByClientFmcgIdAndSalesLevel");
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndSalesLevel(clientFmcgId, SalesLevel.valueOf(salesLevel), pageable);
        List<OrderResponse> orderResponseList = orderEntityPage.stream().map(orderEntity -> entityToDto(orderEntity, "Message")).toList();
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, orderResponseList);
    }
}
