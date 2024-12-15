package sfa.order_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.constant.OrderCallStatus;
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
import sfa.order_service.utill.UniqueIdGenerator;

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

    @Transactional
    public OrderResponse getOrderDetailBySalesTypeById(Long orderId, String salesType){
        Optional<OrderEntity> optionalOrderEntity = orderRepository.findById(orderId);
        if (optionalOrderEntity.isEmpty()) {
            throw new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage());
        }
        OrderResponse orderResponse = new OrderResponse();
        orderResponse = entityToDto(optionalOrderEntity.get(), "Message");
        orderResponse.setMemberResponse(externalRestService.getMember(optionalOrderEntity.get().getMemberId()));
        if(salesType.equalsIgnoreCase("primary")){
            orderResponse.setClientFMCGResponse(externalRestService.getClient(optionalOrderEntity.get().getClientFmcgId()));
        }else {
            orderResponse.setOutletRespForOrderDto(productServiceClient.getOutletForReport(optionalOrderEntity.get().getOutletId()));
            orderResponse.setBeetRespForOrderDto(productServiceClient.getBeetForReport(optionalOrderEntity.get().getBeetId()));
        }
        return orderResponse;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String salesType) {
        String message = "create order";
        log.info("Creating order: {}", request);
        OrderEntity orderEntity = dtoToEntity(request, salesType);
        orderEntity.setInvoiceNumber(UniqueIdGenerator.generateUniqueId());
        OrderEntity entity = orderRepository.save(orderEntity);
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

    @Transactional
    public List<OrderResponse>  createOrderInBulk(OrderBulkReq request, String salesType) {
        List<OrderResponse> orderResponseList = new ArrayList<>();
        String invoiceNumber = UniqueIdGenerator.generateUniqueId();
        log.info("Creating order in bulk");
        for(OrderRequest orderRequest : request.getOrderRequestList()) {
            String message = "create order";
            log.info("Creating order: {}", request);
            OrderEntity orderEntity = dtoToEntity(orderRequest, salesType);
            orderEntity.setInvoiceNumber(invoiceNumber);
            OrderEntity entity = orderRepository.save(orderEntity);
            log.info("create transaction before order creation");
            TransactionRequest transactionRequest = new TransactionRequest();
            transactionRequest.setClientId(orderRequest.getClientId());
            transactionRequest.setTransactionAmount(finalPrice(orderRequest));
            transactionRequest.setTransactionType(TransactionType.DEBIT);
            transactionRequest.setOrderId(entity.getId());
            log.info("create transaction after order creation");
            transactionController.createTransaction(transactionRequest);
            orderResponseList.add(entityToDto(entity, message));
        }
        return orderResponseList;
    }

    public List<OrderResponse> getAllOrderByInvoiceNumber(String invoiceNumber){
        List<OrderEntity> orderEntityList = orderRepository.findByInvoiceNumber(invoiceNumber);
        return orderEntityList.stream().map(orderEntity -> entityToDto(orderEntity, "MSG")).toList();
    }

    public Double finalPrice(OrderRequest request) {
        log.info("Calculate final price for order");
        Double priceOfOrderWithRespectedSalesLevel = getProductPrice(request.getProductId(), getPriceType(request.getSalesLevel()));
        double totalPriceOfOrder = priceOfOrderWithRespectedSalesLevel * request.getQuantity();
        Double gstOnOrder = getProductPrice(request.getProductId(), "gst");
        return totalPriceOfOrder + (totalPriceOfOrder * gstOnOrder) / 100;
    }

    public OrderEntity dtoToEntity(OrderRequest request, String salesType) {
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
        log.info("Get member details for order creation");
        MemberResponse member = externalRestService.getMember(request.getMemberId());
        log.info("check if member exists or not");
        if (member == null) {
            throw new InvalidInputException(ApiErrorCodes.MEMBER_NOT_FOUND.getErrorCode(), ApiErrorCodes.MEMBER_NOT_FOUND.getErrorMessage());
        }
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setClientFmcgId(request.getClientId());
        orderEntity.setQuantity(request.getQuantity());
        orderEntity.setSalesLevel(request.getSalesLevel());
        orderEntity.setOrderCallStatus(OrderCallStatus.Productive);
        orderEntity.setProductId(request.getProductId());
        orderEntity.setMemberId(request.getMemberId());
        orderEntity.setPrice(finalPrice);
        orderEntity.setOrderCreatedDate(new Date());
        orderEntity.setRemarks(request.getRemarks());
        if(salesType.equalsIgnoreCase("secondary")){
            orderEntity.setOrderMedium(request.getOrderMedium());
            orderEntity.setOutletId(request.getOutletId());
            orderEntity.setBeetId(request.getBeetId());
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
        }
        log.info("Updating FMCG-client balance after order creation");
        ClientFMCGUpdateRequest clientFMCGUpdateRequest = new ClientFMCGUpdateRequest();
        clientFMCGUpdateRequest.setId(request.getClientId());
        if(request.getSalesLevel() == SalesLevel.WAREHOUSE){
            clientFMCGUpdateRequest.setTopUpBalance(client.getTopUpBalance() - finalPrice);
        }else{
            clientFMCGUpdateRequest.setTopUpBalance(client.getTopUpBalance());
        }
        clientFMCGUpdateRequest.setClientCode(client.getClientCode());
        clientFMCGUpdateRequest.setCity(client.getCity());
        clientFMCGUpdateRequest.setRegion(client.getRegion());
        clientFMCGUpdateRequest.setEmail(client.getEmail());
        clientFMCGUpdateRequest.setClientFirstName(client.getClientFirstName());
        clientFMCGUpdateRequest.setClientLastName(client.getClientLastName());
        clientFMCGUpdateRequest.setMobile(client.getMobile());
        clientFMCGUpdateRequest.setAddress(client.getAddress());
        clientFMCGUpdateRequest.setState(client.getState());
        clientFMCGUpdateRequest.setUserRoleList(client.getUserRoleList());
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
        orderResponse.setQuantity(orderEntity.getQuantity());
        orderResponse.setProductRes(productServiceClient.getProduct(orderEntity.getProductId()));
        orderResponse.setProductId(orderEntity.getProductId());
        orderResponse.setInvoiceNumber(orderEntity.getInvoiceNumber());
        orderResponse.setOrderCreatedDate(orderEntity.getOrderCreatedDate());
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
        orderResponse.setRemarks(orderEntity.getRemarks());
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
        orderEntity.setRemarks(request.getRemarks());
        log.info("Order status updated to {}", request.getStatus());
        OrderEntity updatedOrder = orderRepository.save(orderEntity);
        OrderUpdateResponse orderResponse = new OrderUpdateResponse();
        orderResponse.setOrderId(updatedOrder.getId());
        orderResponse.setStatus(updatedOrder.getStatus());
        orderResponse.setMessage("Order status updated to delivered!!");
        orderResponse.setRemarks(updatedOrder.getRemarks());
        return orderResponse;
    }
    public List<OrderUpdateResponse> updateOrderInBulk(OrderBulkUpdateRequest orderBulkUpdateRequest) {
        List<OrderUpdateResponse> orderUpdateResponseList = new ArrayList<>();
        for(OrderUpdateRequest orderUpdateRequest : orderBulkUpdateRequest.getOrderUpdateRequests()) {
            log.info("update order status");
            OrderEntity orderEntity = orderRepository.findById(orderUpdateRequest.getOrderId()).orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage()));
            orderEntity.setStatus(orderUpdateRequest.getStatus());
            orderEntity.setRemarks(orderUpdateRequest.getRemarks());
            log.info("Order status updated to {}", orderUpdateRequest.getStatus());
            OrderEntity updatedOrder = orderRepository.save(orderEntity);
            OrderUpdateResponse orderResponse = new OrderUpdateResponse();
            orderResponse.setOrderId(updatedOrder.getId());
            orderResponse.setStatus(updatedOrder.getStatus());
            orderResponse.setMessage("Order status updated to delivered!!");
            orderResponse.setRemarks(updatedOrder.getRemarks());
            orderUpdateResponseList.add(orderResponse);
        }
            return orderUpdateResponseList;
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


    public PaginatedResp<OrderResponse> getAllOrderByClientFmcgId(Long clientFmcgId, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllOrderByClientFmcgId");
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgId(clientFmcgId, pageable);
        List<OrderResponse> orderResponseList = orderEntityPage.stream().map(orderEntity -> entityToDto(orderEntity, "Message")).toList();
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, orderResponseList);
    }

    public PaginatedResp<OrderResponse> getAllOrderByMemberId(Long memberId, int page, int pageSize, String sortBy, String sortDirection) {
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
        log.info("Paged data returned successfully");
        List<OrderResponse> collect = orderEntityPage.stream().map(orderEntity -> entityToDto(orderEntity, "Message")).toList();
        return PaginatedResp.<OrderResponse>builder().totalElements(orderEntityPage.getTotalElements()).totalPages(orderEntityPage.getTotalPages()).page(page).content(collect).build();
    }

    public PaginatedResp<OrdersWithInvoiceGroupingResp> getOrdersGroupedByInvoice(Long clientFmcgId, SalesLevel salesLevel, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, Sort.unsorted());
        Page<String> invoiceNumbersPage = orderRepository.findDistinctInvoiceNumbers(clientFmcgId, salesLevel, pageable);
        List<OrdersWithInvoiceGroupingResp> groupedResponses = new ArrayList<>();
        for(String invoiceNumber : invoiceNumbersPage) {
            List<OrderEntity> orderEntityList = orderRepository.findOrdersByInvoiceNumber(invoiceNumber);
            List<OrderResponse> orderResponseList = orderEntityList.stream().map(orderEntity -> entityToDto(orderEntity, "mg")).toList();
            OrdersWithInvoiceGroupingResp orders = new OrdersWithInvoiceGroupingResp(invoiceNumber, orderResponseList);
            groupedResponses.add(orders);
        }
        return new PaginatedResp<>(invoiceNumbersPage.getTotalElements(), invoiceNumbersPage.getTotalPages(), page, groupedResponses);
    }

    public PaginatedResp<OrderResponse> getAllOrderByReportingManagerMembers(Long memberId, SalesLevel salesLevel,int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllOrderByMemberId");
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(memberId);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMembersIdList(memberIds, salesLevel, pageable);
        List<OrderResponse> orderResponseList = orderEntityPage.stream().map(orderEntity -> entityToDto(orderEntity, "Message")).toList();
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, orderResponseList);
    }
}
