package sfa.order_service.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.Configs.PreOrPost;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.controller.TransactionController;
import sfa.order_service.dto.request.*;
import sfa.order_service.dto.response.*;
import sfa.order_service.entity.*;
import sfa.order_service.enums.OrderStatus;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.enums.TransactionType;
import sfa.order_service.exception.BusinessServiceException;
import sfa.order_service.exception.InvalidInputException;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.*;
import sfa.order_service.utill.CalculateGst;
import sfa.order_service.utill.DiscountUtil;
import sfa.order_service.utill.UniqueIdGenerator;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {
    private final DiscountRepo discountRepo;
    private final InvoiceMasterRepo invoiceMasterRepo;
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final ExternalRestService externalRestService;
    private final TransactionController transactionController;
    private final TransactionRepository transactionRepository;
    private final OrderInvoicesRepo orderInvoicesRepo;
    private final DiscountRepo discountRepository;

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
    public OrderResponse getOrderDetailBySalesTypeById(Long orderId, String salesType) {
        Optional<OrderEntity> optionalOrderEntity = orderRepository.findById(orderId);
        if (optionalOrderEntity.isEmpty()) {
            throw new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage());
        }
        OrderResponse orderResponse = new OrderResponse();
        orderResponse = entityToDto(optionalOrderEntity.get(), "Message");
        orderResponse.setMemberResponse(externalRestService.getMember(optionalOrderEntity.get().getMemberId()));
        if (salesType.equalsIgnoreCase("primary")) {
            orderResponse.setClientFMCGResponse(externalRestService.getClient(optionalOrderEntity.get().getClientFmcgId()));
        } else {
            orderResponse.setOutletRespForOrderDto(productServiceClient.getOutletForReport(optionalOrderEntity.get().getOutletId()));
            orderResponse.setBeetRespForOrderDto(productServiceClient.getBeetForReport(optionalOrderEntity.get().getBeetId()));
        }
        return orderResponse;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request, String salesType) {
        String message = "create order";
        List<InvoiceMaster> invoiceMastersList = invoiceMasterRepo.findAll();
        if(invoiceMastersList.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.NOT_FOUND.getErrorCode(), "Invoice master not created");
        }
        String invoiceNumber = "";
        InvoiceMaster invoiceMaster = invoiceMastersList.get(0);
        int currentSerialNumber = invoiceMaster.getCurrentSerialNumber() + 1;
        invoiceMaster.setCurrentSerialNumber(currentSerialNumber);
        invoiceMasterRepo.save(invoiceMaster);
        int currentYear = LocalDate.now().getYear();
        if (invoiceMaster.getPreOrPost() == PreOrPost.Pre) {
            invoiceNumber = invoiceMaster.getCode() + currentSerialNumber + currentYear;
        } else {
            invoiceNumber = currentSerialNumber + currentYear + invoiceMaster.getCode();
        }
        log.info("Creating order: {}", request);
        OrderEntity orderEntity = dtoToEntity(request, salesType);
        OrderInvoice orderInvoice = new OrderInvoice();
        orderInvoice.setInvoiceDate(new Date());
        orderInvoice.setOutletId(request.getOutletId());
        orderInvoice.setBeetId(request.getBeetId());
        orderInvoice.setSalesLevel(request.getSalesLevel());
        orderInvoice.setMemberId(request.getMemberId());
        orderInvoice.setClientFmcgId(request.getClientId());
        orderInvoice.setInvoiceNumber(invoiceNumber);
        OrderInvoice generatedInvoice = orderInvoicesRepo.save(orderInvoice);
        orderEntity.setOrderInvoice(generatedInvoice);
        orderEntity.setInvoiceNumber(invoiceNumber);
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
    public List<OrderResponse> createOrderInBulk(OrderBulkReq request, String salesType) {
        List<OrderResponse> orderResponseList = new ArrayList<>();
        List<InvoiceMaster> invoiceMastersList = invoiceMasterRepo.findAll();
        if(invoiceMastersList.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.NOT_FOUND.getErrorCode(), "Invoice master not created");
        }

        String invoiceNumber;
        InvoiceMaster invoiceMaster = invoiceMastersList.get(0);
        int currentSerialNumber = invoiceMaster.getCurrentSerialNumber() + 1;
        invoiceMaster.setCurrentSerialNumber(currentSerialNumber);
        invoiceMasterRepo.save(invoiceMaster);
        int currentYear = LocalDate.now().getYear();
        if (invoiceMaster.getPreOrPost() == PreOrPost.Pre) {
            invoiceNumber = invoiceMaster.getCode() + currentSerialNumber + currentYear;
        } else {
            invoiceNumber = currentSerialNumber + currentYear + invoiceMaster.getCode();
        }
        OrderInvoice orderInvoice = new OrderInvoice();
        orderInvoice.setInvoiceDate(new Date());
        orderInvoice.setOutletId(request.getOrderRequestList().get(0).getOutletId());
        orderInvoice.setBeetId(request.getOrderRequestList().get(0).getBeetId());
        orderInvoice.setSalesLevel(request.getOrderRequestList().get(0).getSalesLevel());
        orderInvoice.setMemberId(request.getOrderRequestList().get(0).getMemberId());
        orderInvoice.setClientFmcgId(request.getOrderRequestList().get(0).getClientId());
        orderInvoice.setInvoiceNumber(invoiceNumber);
        OrderInvoice generatedInvoice = orderInvoicesRepo.save(orderInvoice);
        log.info("Creating order in bulk");
        for (OrderRequest orderRequest : request.getOrderRequestList()) {
            String message = "create order";
            log.info("Creating order: {}", request);
            OrderEntity orderEntity = dtoToEntity(orderRequest, salesType);
            orderEntity.setOrderInvoice(generatedInvoice);
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
    @Transactional
    public List<OrderResponse> createOrderInBulkWithInventoryUpdate(OrderBulkReq request, String salesType) {
        List<OrderResponse> orderResponseList = new ArrayList<>();
        List<InvoiceMaster> invoiceMastersList = invoiceMasterRepo.findAll();
        if(invoiceMastersList.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.NOT_FOUND.getErrorCode(), "Invoice master not created");
        }

        String invoiceNumber;
        InvoiceMaster invoiceMaster = invoiceMastersList.get(0);
        int currentSerialNumber = invoiceMaster.getCurrentSerialNumber() + 1;
        invoiceMaster.setCurrentSerialNumber(currentSerialNumber);
        invoiceMasterRepo.save(invoiceMaster);
        int currentYear = LocalDate.now().getYear();
        if (invoiceMaster.getPreOrPost() == PreOrPost.Pre) {
            invoiceNumber = invoiceMaster.getCode() + currentSerialNumber + currentYear;
        } else {
            invoiceNumber = currentSerialNumber + currentYear + invoiceMaster.getCode();
        }
        OrderInvoice orderInvoice = new OrderInvoice();
        orderInvoice.setInvoiceDate(new Date());
        orderInvoice.setOutletId(request.getOrderRequestList().get(0).getOutletId());
        orderInvoice.setBeetId(request.getOrderRequestList().get(0).getBeetId());
        orderInvoice.setSalesLevel(request.getOrderRequestList().get(0).getSalesLevel());
        orderInvoice.setMemberId(request.getOrderRequestList().get(0).getMemberId());
        orderInvoice.setClientFmcgId(request.getOrderRequestList().get(0).getClientId());
        orderInvoice.setInvoiceNumber(invoiceNumber);
        OrderInvoice generatedInvoice = orderInvoicesRepo.save(orderInvoice);
        log.info("Creating order in bulk");
        for (OrderRequest orderRequest : request.getOrderRequestList()) {
            String message = "create order";
            log.info("Creating order: {}", request);
            OrderEntity orderEntity = dtoToEntity(orderRequest, salesType);
            orderEntity.setOrderInvoice(generatedInvoice);
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
            InventoryUpdateRequest inventoryUpdateRequest = new InventoryUpdateRequest();
            inventoryUpdateRequest.setQuantitySold((long) orderRequest.getQuantity());
            inventoryUpdateRequest.setSalesLevel(orderRequest.getSalesLevel());
            inventoryUpdateRequest.setClientId(orderRequest.getClientId());
            externalRestService.updateInventory(orderEntity.getClientFmcgId(), orderEntity.getProductId(), inventoryUpdateRequest);
        }
        return orderResponseList;
    }

    public List<OrderResponse> getAllOrderByInvoiceNumber(String invoiceNumber) {
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
        log.info("Calculate final price for order");
        Double finalPrice = finalPrice(request);
        Double discountAmount = 0.0;
        ClientFMCGResponse clientFMCGResponse = externalRestService.getClient(request.getClientId());
        log.info("Fetch and apply applicable discounts");
        if (request.getDiscountCode() != null) {
            List<DiscountEntity> discounts = discountRepo.findByDiscountCodeAndProductId(request.getDiscountCode(), request.getProductId());
            if (!discounts.isEmpty()) {
                for (DiscountEntity discount : discounts) {
                    log.info("Check discount validity");
                    Date orderDate = new Date();
                    Date validFrom = discount.getValidFrom();
                    Date validTo = discount.getValidTo();
                    if (!(orderDate.after(validFrom) && orderDate.before(validTo))) {
                        continue;
                    }
                    log.info("Applying discount: " + discount.getDescription());
                    switch (discount.getDiscountType()) {
                        case PROMOTIONAL:
                            if (discount.getPercentage() != null) {
                                discountAmount += (finalPrice * discount.getPercentage()) / 100;
                            } else if (discount.getFixedAmount() != null) {
                                discountAmount += discount.getFixedAmount();
                            }
                            break;
                        case QUANTITY_BASED:
                            if (request.getQuantity() >= discount.getMinQuantity()) {
                                discountAmount += discount.getFixedAmount();
                            }
                            break;
                        case SEASONAL:
                            if (discount.getPercentage() != null) {
                                discountAmount += (finalPrice * discount.getPercentage()) / 100;
                            }
                            break;

                        case BOGO:
                            log.info("Applying BOGO (Buy One Get One) discount");
                            if (request.getQuantity() >= discount.getBogoOfferQuantity()) {
                                int freeItems = (request.getQuantity() / discount.getBogoOfferQuantity()) * discount.getBogoFreeQuantity();
                                discountAmount += (finalPrice * freeItems);
                            }
                            break;

                        case VOLUME_BASED:
                            log.info("Applying Volume-based discount");
                            if (request.getQuantity() >= discount.getMinQuantity()) {
                                discountAmount += discount.getFixedAmount();
                            }
                            break;

                        case LOYALTY:
                            log.info("Applying Loyalty discount");
                            if (request.getQuantity() > 0) {
                                discountAmount += (finalPrice * discount.getPercentage()) / 100;
                            }
                            break;
                        default:
                            log.warn("Unknown discount type: " + discount.getDiscountType());
                    }
                }
            } else {
                log.warn("No applicable discounts found for code: " + request.getDiscountCode());
            }
        }

        log.info("Calculate price after applying discounts");
        Double priceAfterDiscount = finalPrice - discountAmount;
        log.info("Validate member");
        MemberGetDto member = externalRestService.getMember(request.getMemberId());
        if (member == null) {
            throw new InvalidInputException(ApiErrorCodes.MEMBER_NOT_FOUND.getErrorCode(), ApiErrorCodes.MEMBER_NOT_FOUND.getErrorMessage());
        }
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setClientFmcgId(request.getClientId());
        orderEntity.setQuantity(request.getQuantity());
        orderEntity.setSalesLevel(request.getSalesLevel());
        orderEntity.setBeetLogId(request.getBeetLogId());
        orderEntity.setClientLogId(request.getClientLogId());
        orderEntity.setDoctorLogId(request.getDoctorLogId());
        orderEntity.setBundleType(request.getBundleType());
        orderEntity.setOrderCallStatus(OrderCallStatus.Productive);
        orderEntity.setProductId(request.getProductId());
        orderEntity.setMemberId(request.getMemberId());
        orderEntity.setPrice(finalPrice);
        orderEntity.setRegionId(clientFMCGResponse.getRegion());
        orderEntity.setStateId(clientFMCGResponse.getState());
        orderEntity.setCityId(clientFMCGResponse.getCity());
        orderEntity.setPriceAfterDiscount(priceAfterDiscount);
        orderEntity.setOrderCreatedDate(new Date());
        orderEntity.setRemarks(request.getRemarks());

        if (salesType.equalsIgnoreCase("secondary")) {
            orderEntity.setOrderMedium(request.getOrderMedium());
            orderEntity.setOutletId(request.getOutletId());
            orderEntity.setBeetId(request.getBeetId());
            log.info("Validate outlet");
            String outletById = externalRestService.getOutletById(request.getOutletId());
            if (outletById.isEmpty()) {
                throw new InvalidInputException(ApiErrorCodes.OUTLET_NOT_FOUND.getErrorCode(), ApiErrorCodes.OUTLET_NOT_FOUND.getErrorMessage());
            }
            log.info("Validate beet");
            String beetById = externalRestService.getBeetById(request.getBeetId());
            if (beetById.isEmpty()) {
                throw new InvalidInputException(ApiErrorCodes.BEET_NOT_FOUND.getErrorCode(), ApiErrorCodes.BEET_NOT_FOUND.getErrorMessage());
            }
        }

        if (salesType.equalsIgnoreCase("primary")) {
            log.info("Validate FMCG client");
            ClientFMCGResponse client = externalRestService.getClient(request.getClientId());
            if (client == null) {
                throw new InvalidInputException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), ApiErrorCodes.CLIENT_NOT_FOUND.getErrorMessage());
            }
            if (client.getTopUpBalance() < finalPrice) {
                throw new InvalidInputException(ApiErrorCodes.INSUFFICIENT_BALANCE.getErrorCode(), ApiErrorCodes.INSUFFICIENT_BALANCE.getErrorMessage());
            }
            log.info("Update FMCG client balance asynchronously");
            ClientFMCGUpdateRequest clientFMCGUpdateRequest = new ClientFMCGUpdateRequest();
            clientFMCGUpdateRequest.setId(request.getClientId());
            clientFMCGUpdateRequest.setTopUpBalance(client.getTopUpBalance());
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

        }
        log.info("Order creation process completed successfully");
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
        if (orderEntity.getOutletId() != null) {
            log.info("fetch details from SFA Outlet controller");
            orderResponse.setOutletRespForOrderDto(productServiceClient.getOutletForReport(orderEntity.getOutletId()));
        }
        if (orderEntity.getBeetId() != null) {
            log.info("fetch details from SFA ");
            orderResponse.setBeetRespForOrderDto(productServiceClient.getBeetForReport(orderEntity.getBeetId()));
        }
        orderResponse.setBundleType(orderEntity.getBundleType());
        orderResponse.setClientCityName(productServiceClient.getCityNameById(orderEntity.getCityId()));
        orderResponse.setQuantity(orderEntity.getQuantity());
        orderResponse.setProductRes(productServiceClient.getProduct(orderEntity.getProductId()));
        orderResponse.setProductId(orderEntity.getProductId());
        orderResponse.setBeetLogId(orderEntity.getBeetLogId());
        orderResponse.setClientLogId(orderEntity.getClientLogId());
        orderResponse.setDoctorLogId(orderEntity.getDoctorLogId());
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
        MemberGetDto member = externalRestService.getMember(orderEntity.getMemberId());
        orderResponse.setMemberId(orderEntity.getMemberId());
        orderResponse.setMemberResponse(member);
        orderResponse.setMemberName(member.getFirstName() + " " + member.getLastName());
        ClientFMCGResponse client = externalRestService.getClient(orderEntity.getClientFmcgId());
        orderResponse.setClientName(client.getClientFirstName() + " " + client.getClientLastName());
        orderResponse.setClientBalanceAmount(client.getTopUpBalance());
        orderResponse.setDiscountCode(orderEntity.getDiscountCode());
        orderResponse.setPriceAfterDiscount(orderEntity.getPriceAfterDiscount());
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
        if (request.getStatus() == OrderStatus.DELIVERED && orderEntity.getStatus() != OrderStatus.DELIVERED) {
            ClientFMCGResponse client = externalRestService.getClient(orderEntity.getClientFmcgId());
            ClientFMCGUpdateRequest clientFMCGUpdateRequest = new ClientFMCGUpdateRequest();
            clientFMCGUpdateRequest.setId(orderEntity.getClientFmcgId());
            if (orderEntity.getSalesLevel() == SalesLevel.WAREHOUSE) {
                clientFMCGUpdateRequest.setTopUpBalance(client.getTopUpBalance() - orderEntity.getPrice());
            } else {
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
            orderEntity.setStatus(OrderStatus.DELIVERED);
            orderEntity.setRemarks(request.getRemarks());
            if (request.getBundleType() !=  null) {
                orderEntity.setBundleType(request.getBundleType());
            }
            log.info("Order status updated to {}", request.getStatus());
            OrderEntity updatedOrder = orderRepository.save(orderEntity);
            OrderUpdateResponse orderResponse = new OrderUpdateResponse();
            orderResponse.setOrderId(updatedOrder.getId());
            orderResponse.setStatus(updatedOrder.getStatus());
            orderResponse.setTotalPrice(orderEntity.getPrice());
            orderResponse.setTotalPriceWithGst(entityToDto(orderEntity, "MSG").getTotalPriceWithGst());
            orderResponse.setQuantity(String.valueOf(updatedOrder.getQuantity()));
            orderResponse.setMessage("Order status updated to delivered!!");
            orderResponse.setRemarks(updatedOrder.getRemarks());
            return orderResponse;
        } else {
            orderEntity.setStatus(request.getStatus());
            if (request.getBundleType() !=  null) {
                orderEntity.setBundleType(request.getBundleType());
            }
            orderEntity.setRemarks(request.getRemarks());
            log.info("Order status updated to {}", request.getStatus());
            OrderEntity updatedOrder = orderRepository.save(orderEntity);
            OrderUpdateResponse orderResponse = new OrderUpdateResponse();
            orderResponse.setOrderId(updatedOrder.getId());
            orderResponse.setQuantity(String.valueOf(updatedOrder.getQuantity()));
            orderResponse.setStatus(updatedOrder.getStatus());
            orderResponse.setTotalPrice(entityToDto(orderEntity, "MSG").getTotalPrice());
            orderResponse.setTotalPriceWithGst(entityToDto(orderEntity, "MSG").getTotalPriceWithGst());
            orderResponse.setMessage("Order status updated to delivered!!");
            orderResponse.setRemarks(updatedOrder.getRemarks());
            return orderResponse;
        }

    }

    @Transactional
    public OrderUpdateResponse updateOrderQuantity(OrderUpdateRequest orderUpdateRequest) {
        OrderEntity orderEntity = orderRepository.findById(orderUpdateRequest.getOrderId()).orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage()));
        orderEntity.setStatus(orderUpdateRequest.getStatus());
        if (orderUpdateRequest.getBundleType() !=  null) {
            orderEntity.setBundleType(orderUpdateRequest.getBundleType());
        }
        orderEntity.setQuantity(orderUpdateRequest.getQuantity());
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setQuantity(orderUpdateRequest.getQuantity());
        orderRequest.setProductId(orderEntity.getProductId());
        orderRequest.setSalesLevel(orderEntity.getSalesLevel());
        orderEntity.setPrice(finalPrice(orderRequest));
        orderRepository.save(orderEntity);
        OrderResponse orderResponse = entityToDto(orderEntity, "MSG");
        OrderUpdateResponse orderUpdateResponse = new OrderUpdateResponse();
        orderUpdateResponse.setQuantity(String.valueOf(orderEntity.getQuantity()));
        orderUpdateResponse.setStatus(orderEntity.getStatus());
        orderUpdateResponse.setTotalPrice(orderResponse.getTotalPrice());
        orderUpdateResponse.setOrderId(orderEntity.getId());
        orderUpdateResponse.setRemarks(orderEntity.getRemarks());
        orderUpdateResponse.setTotalPriceWithGst(orderResponse.getTotalPriceWithGst());
        TransactionEntity transactionEntity = transactionRepository.findByClientIdAndOrderId(orderEntity.getClientFmcgId(), orderUpdateRequest.getOrderId());
        transactionEntity.setTransactionAmount(orderEntity.getPrice());
        transactionRepository.save(transactionEntity);
        return orderUpdateResponse;
    }

    public List<OrderUpdateResponse> updateOrderInBulk(OrderBulkUpdateRequest orderBulkUpdateRequest) {
        List<OrderUpdateResponse> orderUpdateResponseList = new ArrayList<>();
        for (OrderUpdateRequest orderUpdateRequest : orderBulkUpdateRequest.getOrderUpdateRequests()) {
            log.info("update order status");
            OrderEntity orderEntity = orderRepository.findById(orderUpdateRequest.getOrderId()).orElseThrow(() -> new NoSuchElementFoundException(ApiErrorCodes.ORDER_NOT_FOUND.getErrorCode(), ApiErrorCodes.ORDER_NOT_FOUND.getErrorMessage()));
            if (orderUpdateRequest.getBundleType() !=  null) {
                orderEntity.setBundleType(orderUpdateRequest.getBundleType());
            }
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
        assert productRes != null;

        FinalProductPriceResponse finalRes = new FinalProductPriceResponse();
        double unitPrice;

        if (finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.RETAILER) {
            unitPrice = productRes.getProductPriceRes().getRetailerPrice();
        } else if (finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.WAREHOUSE) {
            unitPrice = productRes.getProductPriceRes().getWarehousePrice();
        } else if (finalProductPriceRequest.getSalesLevelConstant() == SalesLevel.STOCKIST) {
            unitPrice = productRes.getProductPriceRes().getStockListPrice();
        } else {
            throw new IllegalArgumentException("Unsupported sales level: " + finalProductPriceRequest.getSalesLevelConstant());
        }

        int quantity = finalProductPriceRequest.getQuantity();
        double totalPrice = unitPrice * quantity;
        double discount = DiscountUtil.calculateFinalPrice(totalPrice, finalProductPriceRequest.getDiscountCoupon().getDiscountAmount());
        double subTotal = totalPrice - discount;
        double gstAmount = CalculateGst.calculateGstAmountFromTotal(subTotal, productRes.getProductPriceRes().getGstPercentage());
        double totalPriceWithGst = subTotal + gstAmount;

        finalRes.setUnitPrice(unitPrice);
        finalRes.setQuantity(quantity);
        finalRes.setProductId(productRes.getProductId());
        finalRes.setMessage("Final price calculated successfully");
        finalRes.setDiscountApplied(discount);
        finalRes.setSubTotal(subTotal);
        finalRes.setGstAmount(gstAmount);
        finalRes.setTotalPriceWithGst(totalPriceWithGst);
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
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderInvoice> orderInvoicePage = orderInvoicesRepo.findByClientFmcgIdAndSalesLevel(clientFmcgId, salesLevel, pageable);
        List<OrdersWithInvoiceGroupingResp> groupedResponses = new ArrayList<>();
        for (OrderInvoice orderInvoice : orderInvoicePage.getContent()) {
            List<OrderEntity> orderEntityList = orderRepository.findOrdersByInvoiceNumber(orderInvoice.getInvoiceNumber());
            List<OrderResponse> orderResponseList = orderEntityList.stream().map(orderEntity -> entityToDto(orderEntity, "mg")).toList();
            OrdersWithInvoiceGroupingResp orders = new OrdersWithInvoiceGroupingResp(orderInvoice.getInvoiceNumber(), orderResponseList);
            groupedResponses.add(orders);
        }
        return new PaginatedResp<>(orderInvoicePage.getTotalElements(), orderInvoicePage.getTotalPages(), page, groupedResponses);
    }

    public PaginatedResp<OrdersWithInvoiceGroupingResp> getOrdersGroupedByInvoiceByReportingManagerId(Long reportingManagerId, SalesLevel salesLevel, boolean isManagerSaleIncluded, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
         Pageable pageable = PageRequest.of(page, pageSize, sort);
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(reportingManagerId);
        if (isManagerSaleIncluded) {
            memberIds.add(reportingManagerId);
            Page<OrderInvoice> orderInvoicePage = orderInvoicesRepo.findByReportingManagerMembersAndSalesLevel(salesLevel, memberIds, pageable);
            List<OrdersWithInvoiceGroupingResp> groupedResponses = new ArrayList<>();
            for (OrderInvoice orderInvoice : orderInvoicePage.getContent()) {
                List<OrderEntity> orderEntityList = orderRepository.findOrdersByInvoiceNumber(orderInvoice.getInvoiceNumber());
                List<OrderResponse> orderResponseList = orderEntityList.stream().map(orderEntity -> entityToDto(orderEntity, "mg")).toList();
                OrdersWithInvoiceGroupingResp orders = new OrdersWithInvoiceGroupingResp(orderInvoice.getInvoiceNumber(), orderResponseList);
                groupedResponses.add(orders);
            }
            return new PaginatedResp<>(orderInvoicePage.getTotalElements(), orderInvoicePage.getTotalPages(), page, groupedResponses);
        } else {
            Page<OrderInvoice> orderInvoicePage = orderInvoicesRepo.findByReportingManagerMembersAndSalesLevel(salesLevel, memberIds, pageable);
            List<OrdersWithInvoiceGroupingResp> groupedResponses = new ArrayList<>();
            for (OrderInvoice orderInvoice : orderInvoicePage.getContent()) {
                List<OrderEntity> orderEntityList = orderRepository.findOrdersByInvoiceNumber(orderInvoice.getInvoiceNumber());
                List<OrderResponse> orderResponseList = orderEntityList.stream().map(orderEntity -> entityToDto(orderEntity, "mg")).toList();
                OrdersWithInvoiceGroupingResp orders = new OrdersWithInvoiceGroupingResp(orderInvoice.getInvoiceNumber(), orderResponseList);
                groupedResponses.add(orders);
            }
            return new PaginatedResp<>(orderInvoicePage.getTotalElements(), orderInvoicePage.getTotalPages(), page, groupedResponses);
        }
    }

    public PaginatedResp<OrderResponse> getAllOrderByReportingManagerMembers(Long memberId, SalesLevel salesLevel, int page, int pageSize, String sortBy, String sortDirection) {
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        log.info("inside of getAllOrderByMemberId");
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(memberId);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMembersIdList(memberIds, salesLevel, pageable);
        List<OrderResponse> orderResponseList = orderEntityPage.stream().map(orderEntity -> entityToDto(orderEntity, "Message")).toList();
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, orderResponseList);
    }
}
