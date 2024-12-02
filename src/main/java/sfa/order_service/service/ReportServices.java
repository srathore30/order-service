package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.dto.request.ReportsRequest;
import sfa.order_service.dto.response.*;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.repo.OrderRepository;
import sfa.order_service.utill.CalculateGst;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServices {
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;

    public ReportsResponse getSalesReportBetweenDatesAndSalesLevel(ReportsRequest reportsRequest){
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevel(reportsRequest.getStartDate(),reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant());
        if (orderEntityList.isEmpty()){
            throw new RuntimeException("no records found");
        }
        double totalGst = 0D;
        Double totalSales = 0D;
        int totalOrder = 0;
        ReportsResponse reportsResponse = new ReportsResponse();
        List<TopSellingProductRes> topSellingProductRes = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList){
            ProductRes productRes = productServiceClient.getProduct(orderEntity.getProductId());
            ProductPriceRes productPriceRes = productRes.getProductPriceRes();
            if (productPriceRes != null) {
                totalGst += CalculateGst.calculateGstAmountFromTotal(orderEntity.getPrice(), productPriceRes.getGstPercentage());
                totalSales += orderEntity.getPrice();
                totalOrder += orderEntity.getQuantity();
                Double totalSaleByProduct = 0D;
                List<OrderEntity> orderListByProductId = orderRepository.findByProductId(orderEntity.getProductId());
                for(OrderEntity order : orderListByProductId){
                    totalSaleByProduct += order.getPrice();
                }
                topSellingProductRes.add(new TopSellingProductRes(orderEntity.getProductId(), productRes.getName(), orderListByProductId.size() - 1,  totalSaleByProduct, productPriceRes.getGstPercentage()));
            }
        }
        reportsResponse.setTotalSales(totalSales);
        reportsResponse.setTotalOrder(totalOrder);
        reportsResponse.setTotalGstCollected(totalGst);
        reportsResponse.setTopSellingProductList(topSellingProductRes);
        return reportsResponse;
    }


    //Member Report
    public PaginatedResp<BeetReportResponse> getBeetOrderReportByMemberIdWithDateFilter(Long memberId, Date startDate, Date endDate, int page, int pageSize, String sortBy, String sortDirection){
        Map<Long, Double> beetOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findAllByOrderCreatedDateBetweenAndMemberId(startDate, endDate, memberId,pageable);
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for(OrderEntity order : orderEntityPage.getContent()){
            beetOrderMap.put(order.getBeetId(), beetOrderMap.getOrDefault(order.getBeetId(), 0.0) + order.getPrice());
        }
        Set<Long> beetIds = beetOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(beetIds);
        for (Map.Entry<Long, Double> entry : beetOrderMap.entrySet()){
            for(BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList){
                if(Objects.equals(beetRespForOrderDto.getId(), entry.getKey())){
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalSales(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalSales).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }
    public PaginatedResp<BeetReportResponse> getBeetOrderReportByReportingManagerIdWithDateFilter(Long reportingManagerId, Date startDate, Date endDate, int page, int pageSize, String sortBy, String sortDirection){
        Map<Long, Double> beetOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(reportingManagerId);
        Page<OrderEntity> orderEntityPage = orderRepository.findOrdersByDateRangeAndMembers(startDate, endDate, memberIds,pageable);
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for(OrderEntity order : orderEntityPage.getContent()){
            beetOrderMap.put(order.getBeetId(), beetOrderMap.getOrDefault(order.getBeetId(), 0.0) + order.getPrice());
        }
        Set<Long> beetIds = beetOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(beetIds);
        for (Map.Entry<Long, Double> entry : beetOrderMap.entrySet()){
            for(BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList){
                if(Objects.equals(beetRespForOrderDto.getId(), entry.getKey())){
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalSales(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalSales).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }
    public PaginatedResp<OutletReportResponse> getOutletOrderReportByBeetIdWithDateFilter(Long beetId, Date startDate, Date endDate, int page, int pageSize, String sortBy, String sortDirection){
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findAllByOrderCreatedDateBetweenAndBeetId(startDate, endDate, beetId, pageable);
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for(OrderEntity order : orderEntityPage.getContent()){
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()){
            for(OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList){
                if(Objects.equals(outletRespForOrderDto.getId(), entry.getKey())){
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalSales(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalSales).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }


    public PaginatedResp<OutletReportResponse> getAllOrderByEachOutletByMemberIdByProductiveStatus(Long memberId, OrderCallStatus orderCallStatus, int page, int pageSize, String sortBy, String sortDirection){
        log.info("inside of getAllProductiveOrderByEachOutletByMemberId function in report controller");
        Map<Long, Integer> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberIdAndOrderCallStatus(memberId, orderCallStatus, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0) + 1);
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Integer> entry : outletOrderMap.entrySet()){
            for(OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList){
                if(Objects.equals(outletRespForOrderDto.getId(), entry.getKey())){
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalOrder(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }
    public PaginatedResp<OutletReportResponse> getAllOrderByEachOutletByMemberIdByOrderMedium(Long memberId, OrderMedium orderMedium,int page, int pageSize, String sortBy, String sortDirection){
        log.info("inside of getAllProductiveOrderByEachOutletByMemberId function in report controller");
        Map<Long, Integer> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberIdAndOrderMedium(memberId, orderMedium, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0) + 1);
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Integer> entry : outletOrderMap.entrySet()){
            for(OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList){
                if(Objects.equals(outletRespForOrderDto.getId(), entry.getKey())){
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalOrder(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }
    public PaginatedResp<BeetReportResponse> getAllOrderByEachBeetByMemberIdByProductiveStatus(Long memberId, OrderCallStatus orderCallStatus, int page, int pageSize, String sortBy, String sortDirection){
        log.info("inside of getAllProductiveOrderByEachBeetByMemberId function in report controller");
        Map<Long, Integer> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberIdAndOrderCallStatus(memberId, orderCallStatus, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getBeetId(), outletOrderMap.getOrDefault(order.getBeetId(), 0) + 1);
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Integer> entry : outletOrderMap.entrySet()){
            for(BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList){
                if(Objects.equals(beetRespForOrderDto.getId(), entry.getKey())){
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalOrder(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }
    public PaginatedResp<BeetReportResponse> getAllOrderByEachBeetByMemberIdByOrderMedium(Long memberId, OrderMedium orderMedium, int page, int pageSize, String sortBy, String sortDirection){
        log.info("inside of getAllProductiveOrderByEachBeetByMemberId function in report controller");
        Map<Long, Integer> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberIdAndOrderMedium(memberId, orderMedium, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getBeetId(), outletOrderMap.getOrDefault(order.getBeetId(), 0) + 1);
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Integer> entry : outletOrderMap.entrySet()){
            for(BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList){
                if(Objects.equals(beetRespForOrderDto.getId(), entry.getKey())){
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalOrder(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }

    //Client Fmcg reports
    public PaginatedResp<OutletReportResponse> getAllOrderByEachOutletByClientFmcgIdByProductiveStatus(Long clientFmcgId, OrderCallStatus orderCallStatus,int page, int pageSize, String sortBy, String sortDirection){
        log.info("inside of getAllProductiveOrderByEachOutletByMemberId function in report controller");
        Map<Long, Integer> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndOrderCallStatus(clientFmcgId, orderCallStatus, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0) + 1);
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Integer> entry : outletOrderMap.entrySet()){
            for(OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList){
                if(Objects.equals(outletRespForOrderDto.getId(), entry.getKey())){
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalOrder(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }
    public PaginatedResp<OutletReportResponse> getAllOrderByEachOutletByClientFmcgIdByOrderMedium(Long clientFmcgId, OrderMedium orderMedium,int page, int pageSize, String sortBy, String sortDirection){
        log.info("inside of getAllProductiveOrderByEachOutletByMemberId function in report controller");
        Map<Long, Integer> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndOrderMedium(clientFmcgId, orderMedium, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0) + 1);
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Integer> entry : outletOrderMap.entrySet()){
            for(OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList){
                if(Objects.equals(outletRespForOrderDto.getId(), entry.getKey())){
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalOrder(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }
    public PaginatedResp<BeetReportResponse> getAllOrderByEachBeetByClientFmcgIdByProductiveStatus(Long clientFmcgId, OrderCallStatus orderCallStatus,int page, int pageSize, String sortBy, String sortDirection){
        log.info("inside of getAllProductiveOrderByEachBeetByMemberId function in report controller");
        Map<Long, Integer> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndOrderCallStatus(clientFmcgId, orderCallStatus, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getBeetId(), outletOrderMap.getOrDefault(order.getBeetId(), 0) + 1);
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Integer> entry : outletOrderMap.entrySet()){
            for(BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList){
                if(Objects.equals(beetRespForOrderDto.getId(), entry.getKey())){
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalOrder(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }
    public PaginatedResp<BeetReportResponse> getAllOrderByEachBeetByClientFmcgIdByOrderMedium(Long clientFmcgId, OrderMedium orderMedium,int page, int pageSize, String sortBy, String sortDirection){
        log.info("inside of getAllProductiveOrderByEachBeetByMemberId function in report controller");
        Map<Long, Integer> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndOrderMedium(clientFmcgId, orderMedium, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getBeetId(), outletOrderMap.getOrDefault(order.getBeetId(), 0) + 1);
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Integer> entry : outletOrderMap.entrySet()){
            for(BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList){
                if(Objects.equals(beetRespForOrderDto.getId(), entry.getKey())){
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalOrder(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }

}