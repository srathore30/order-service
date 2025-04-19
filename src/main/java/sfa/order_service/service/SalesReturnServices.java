package sfa.order_service.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.constant.Status;
import sfa.order_service.dto.request.InventoryUpdateRequest;
import sfa.order_service.dto.request.SalesReturnReq;
import sfa.order_service.dto.response.*;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.entity.ReturnStatus;
import sfa.order_service.entity.SalesReturn;
import sfa.order_service.enums.OrderStatus;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.exception.InvalidInputException;
import sfa.order_service.exception.NoSuchElementFoundException;
import sfa.order_service.repo.OrderRepository;
import sfa.order_service.repo.SalesReturnRepo;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SalesReturnServices {
    private final SalesReturnRepo salesReturnRepo;
    private final ProductServiceClient productServiceClient;
    private final ExternalRestService externalRestService;
    private final OrderRepository orderRepository;
    public SalesReturnRes createReturn(SalesReturnReq salesReturnReq){
        log.info("Creating sales return");
        Optional<SalesReturn> salesReturnOptional = salesReturnRepo.findByOrderEntityId(salesReturnReq.getOrderId());
        if(salesReturnOptional.isEmpty()){
            SalesReturn salesReturn = mapToEntity(salesReturnReq);
            SalesReturn saved = salesReturnRepo.save(salesReturn);
            return mapToDto(saved);
        }
        throw new NoSuchElementFoundException(ApiErrorCodes.RETURN_ALREADY_CREATED.getErrorCode(), ApiErrorCodes.RETURN_ALREADY_CREATED.getErrorMessage());
    }

    public SalesReturnRes getReturnByOrderId(Long orderId){
        log.info("fetching sales return with id " + orderId);
        Optional<SalesReturn> salesReturnOptional = salesReturnRepo.findByOrderEntityId(orderId);
        if(salesReturnOptional.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.RETURN_NOT_FOUND.getErrorCode(), ApiErrorCodes.RETURN_NOT_FOUND.getErrorMessage());
        }
        return mapToDto(salesReturnOptional.get());
    }

    public SalesReturnRes getReturnById(Long id){
        log.info("fetching sales return with id " + id);
        Optional<SalesReturn> salesReturnOptional = salesReturnRepo.findById(id);
        if(salesReturnOptional.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.RETURN_NOT_FOUND.getErrorCode(), ApiErrorCodes.RETURN_NOT_FOUND.getErrorMessage());
        }
        return mapToDto(salesReturnOptional.get());
    }
    public SalesReturnRes updateReturnById(Long id, SalesReturnReq  salesReturnReq){
        log.info("updating sales return");
        Optional<SalesReturn> salesReturnOptional = salesReturnRepo.findById(id);
        if(salesReturnOptional.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.RETURN_NOT_FOUND.getErrorCode(), ApiErrorCodes.RETURN_NOT_FOUND.getErrorMessage());
        }
        updateEntityFromDto(salesReturnOptional.get(), salesReturnReq);
        salesReturnRepo.save(salesReturnOptional.get());
        return mapToDto(salesReturnOptional.get());
    }
    public void deleteReturn(Long id){
        Optional<SalesReturn> salesReturnOptional = salesReturnRepo.findById(id);
        if(salesReturnOptional.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.RETURN_NOT_FOUND.getErrorCode(), ApiErrorCodes.RETURN_NOT_FOUND.getErrorMessage());
        }
        salesReturnOptional.get().setStatus(Status.Inactive);
        salesReturnRepo.save(salesReturnOptional.get());
    }
    @Transactional
    public void updateReturnStatus(Long id, ReturnStatus returnStatus){
        log.info("updating sales return status");
        Optional<SalesReturn> salesReturnOptional = salesReturnRepo.findById(id);
        if(salesReturnOptional.isEmpty()){
            throw new NoSuchElementFoundException(ApiErrorCodes.RETURN_NOT_FOUND.getErrorCode(), ApiErrorCodes.RETURN_NOT_FOUND.getErrorMessage());
        }
        salesReturnOptional.get().setReturnStatus(returnStatus);
        salesReturnRepo.save(salesReturnOptional.get());
        if(returnStatus == ReturnStatus.Returned){
            log.info("updateing sales return inventory");
            InventoryUpdateRequest inventoryUpdateRequest = new InventoryUpdateRequest();
            inventoryUpdateRequest.setClientId(salesReturnOptional.get().getClientFmcgId());
            inventoryUpdateRequest.setProductId(salesReturnOptional.get().getOrderEntity().getProductId());
            inventoryUpdateRequest.setSalesLevel(salesReturnOptional.get().getOrderEntity().getSalesLevel());
            inventoryUpdateRequest.setQuantitySold(Long.valueOf(salesReturnOptional.get().getQuantity()));
            externalRestService.updateInventory(salesReturnOptional.get().getClientFmcgId(), salesReturnOptional.get().getOrderEntity().getProductId(), inventoryUpdateRequest);
        }
    }
    public PaginatedResp<SalesReturnRes> getAllReturnByClientFmcgAndSalesLevelAndReturnStatus(Long clientFmcgId, SalesLevel salesLevel, ReturnStatus returnStatus,int page, int pageSize, String sortBy, String sortDirection){
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<SalesReturn> salesReturnPage = salesReturnRepo.findByClientFmcgIdAndSalesLevelAndReturnStatus(clientFmcgId, salesLevel, returnStatus, pageable);
        List<SalesReturnRes> salesReturnResList = salesReturnPage.getContent().stream().filter(salesReturn -> salesReturn.getStatus() == Status.Active).map(this::mapToDto).toList();
        return new PaginatedResp<>(salesReturnPage.getTotalElements(), salesReturnPage.getTotalPages(), page, salesReturnResList);
    }

    public PaginatedResp<SalesReturnRes> getAllReturnByClientFmcgAndSalesLevel(Long clientFmcgId, SalesLevel salesLevel, int page, int pageSize, String sortBy, String sortDirection){
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<SalesReturn> salesReturnPage = salesReturnRepo.findByClientFmcgIdAndSalesLevel(clientFmcgId, salesLevel, pageable);
        List<SalesReturnRes> salesReturnResList = salesReturnPage.getContent().stream().filter(salesReturn -> salesReturn.getStatus() == Status.Active).map(this::mapToDto).toList();
        return new PaginatedResp<>(salesReturnPage.getTotalElements(), salesReturnPage.getTotalPages(), page, salesReturnResList);
    }

    SalesReturnRes mapToDto(SalesReturn salesReturn){
        SalesReturnRes salesReturnRes = new SalesReturnRes();
        salesReturnRes.setOrderResponse(mapToOrderDto(salesReturn.getOrderEntity(), ""));
        salesReturnRes.setReturnStatus(salesReturn.getReturnStatus());
        salesReturnRes.setId(salesReturn.getId());
        salesReturnRes.setSalesLevel(salesReturn.getOrderEntity().getSalesLevel());
        salesReturnRes.setQuantity(salesReturn.getQuantity());
        salesReturnRes.setReason(salesReturn.getReason());
        return salesReturnRes;
    }

    private SalesReturn mapToEntity(SalesReturnReq salesReturnReq){
        OrderEntity orderEntity = orderRepository.findById(salesReturnReq.getOrderId()).get();
        SalesReturn salesReturn = new SalesReturn();
        salesReturn.setOrderEntity(orderEntity);
        salesReturn.setReturnStatus(ReturnStatus.Created);
        salesReturn.setStatus(Status.Active);
        salesReturn.setSalesLevel(orderEntity.getSalesLevel());
        salesReturn.setReturnDate(new Date());
        salesReturn.setClientFmcgId(orderEntity.getClientFmcgId());
        salesReturn.setMemberId(orderEntity.getMemberId());
        salesReturn.setQuantity(salesReturnReq.getQuantity());
        salesReturn.setReason(salesReturnReq.getReason());
        return salesReturn;
    }

    private void updateEntityFromDto(SalesReturn salesReturn, SalesReturnReq salesReturnReq){
        OrderEntity orderEntity = orderRepository.findById(salesReturnReq.getOrderId()).get();
        salesReturn.setOrderEntity(orderEntity);
        salesReturn.setReturnStatus(ReturnStatus.Created);
        salesReturn.setStatus(Status.Active);
        salesReturn.setClientFmcgId(orderEntity.getClientFmcgId());
        salesReturn.setMemberId(orderEntity.getMemberId());
        salesReturn.setQuantity(salesReturnReq.getQuantity());
        salesReturn.setReason(salesReturnReq.getReason());
    }


    private OrderResponse mapToOrderDto(OrderEntity orderEntity, String message) {
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
        MemberGetDto member = externalRestService.getMember(orderEntity.getMemberId());
        orderResponse.setMemberId(orderEntity.getMemberId());
        orderResponse.setMemberName(member.getFirstName() + " " + member.getLastName());
        ClientFMCGResponse client = externalRestService.getClient(orderEntity.getClientFmcgId());
        orderResponse.setClientName(client.getClientFirstName() + " " + client.getClientLastName());
        orderResponse.setClientBalanceAmount(client.getTopUpBalance());
        return orderResponse;
    }

    private Double getProductPrice(Long productId, String priceType) {
        log.info("Get product price with product id: {} and price type: {}", productId, priceType);
        return productServiceClient.getProductPrice(productId, priceType);
    }

    private String getPriceType(SalesLevel salesLevel) {
        log.info("Get price type for sales level: {}", salesLevel);
        return switch (salesLevel) {
            case RETAILER -> "retailer";
            case WAREHOUSE -> "warehouse";
            case STOCKIST -> "stocklist";
            default ->
                    throw new InvalidInputException(ApiErrorCodes.INVALID_INPUT.getErrorCode(), ApiErrorCodes.INVALID_INPUT.getErrorMessage());
        };
    }
}
