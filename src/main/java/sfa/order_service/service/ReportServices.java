package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.constant.OrderCallStatus;
import sfa.order_service.constant.OrderMedium;
import sfa.order_service.dto.request.ReportsRequest;
import sfa.order_service.dto.response.*;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.entity.SamplesEntity;
import sfa.order_service.enums.SalesLevel;
import sfa.order_service.exception.InvalidInputException;
import sfa.order_service.repo.OrderRepository;
import sfa.order_service.repo.SamplesRepo;
import sfa.order_service.utill.CalculateGst;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServices {
    private final OrderRepository orderRepository;
    private final ProductServiceClient productServiceClient;
    private final ExternalRestService externalRestService;
    private final SamplesRepo samplesRepo;

    public ReportsResponse getSalesReportBetweenDatesAndSalesLevel(ReportsRequest reportsRequest) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant());
        if (orderEntityList.isEmpty()) {
            return new ReportsResponse(0D, 0D, 0, Collections.emptyList());
        }
        double totalGst = 0D;
        Double totalSales = 0D;
        int totalOrder = 0;
        ReportsResponse reportsResponse = new ReportsResponse();
        List<Long> productIds = new ArrayList<>();
        List<Long> clientFmcgIds = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            if(!productIds.contains(orderEntity.getProductId())) {
                productIds.add(orderEntity.getProductId());
            }
            if(!clientFmcgIds.contains(orderEntity.getClientFmcgId())) {
                clientFmcgIds.add(orderEntity.getClientFmcgId());
            }
        }
        List<ProductRes> productResList = externalRestService.getAllProductByIds(productIds);
        LocationBulkRes locationBulkRes = externalRestService.getLocationBulkRes(clientFmcgIds);
        List<TopSellingProductRes> topSellingProductRes = new ArrayList<>();
        if (reportsRequest.getSalesLevelConstant() == SalesLevel.WAREHOUSE) {
            for (OrderEntity orderEntity : orderEntityList) {
                ProductRes productRes = getProductResFromListById(orderEntity.getProductId(), productResList);
                ProductPriceRes productPriceRes = productRes.getProductPriceRes();
                if (productPriceRes != null) {
                    ClientFMCGResponse clientFMCGResponse = getClientFmcgResFromListById(orderEntity.getClientFmcgId(), locationBulkRes.getClientFMCGResponseList());
                    String stateName = getStateNameResFromListById(clientFMCGResponse.getState(), locationBulkRes.getStateList()).getStateName();
                    String cityName = getCityNameResFromListById(clientFMCGResponse.getCity(), locationBulkRes.getCityList()).getCityName();
                    String regionName = getRegionNameResFromListById(clientFMCGResponse.getRegion(), locationBulkRes.getRegionList()).getRegionName();
                    totalGst += CalculateGst.calculateGstAmountFromTotal(orderEntity.getPrice(), productPriceRes.getGstPercentage());
                    totalSales += orderEntity.getPrice();
                    totalOrder += 1;
                    Double totalSaleByProduct = 0D;
                    List<OrderEntity> orderListByProductId = orderRepository.findByProductId(orderEntity.getProductId());
                    for (OrderEntity order : orderListByProductId) {
                        totalSaleByProduct += order.getPrice();
                    }
                    TopSellingProductRes resp = new TopSellingProductRes();
                    resp.setName(productRes.getName());
                    resp.setCity(cityName);
                    resp.setState(stateName);
                    resp.setRegion(regionName);
                    resp.setGstAmount(productPriceRes.getGstPercentage());
                    resp.setProductId(orderEntity.getProductId());
                    resp.setQuantitySold(orderListByProductId.size());
                    resp.setRevenue(totalSaleByProduct);
                    resp.setClientFMCGResponse(clientFMCGResponse);
                    topSellingProductRes.add(resp);
                }
            }
        }
        else if (reportsRequest.getSalesLevelConstant() == SalesLevel.STOCKIST || reportsRequest.getSalesLevelConstant() == SalesLevel.RETAILER) {
            List<BeetRespForOrderDto> beetRespForOrderDtoList;
            List<OutletRespForOrderDto> outletRespForOrderDtoList;
            Set<Long> outletIds = new HashSet<>();
            Set<Long> beetIds = new HashSet<>();
            for (OrderEntity orderEntity : orderEntityList) {
                outletIds.add(orderEntity.getOutletId());
                beetIds.add(orderEntity.getBeetId());
            }
            beetRespForOrderDtoList = productServiceClient.getBeets(beetIds);
            outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
            for (OrderEntity orderEntity : orderEntityList) {
                ProductRes productRes = getProductResFromListById(orderEntity.getProductId(), productResList);
                ProductPriceRes productPriceRes = productRes.getProductPriceRes();
                if (productPriceRes != null) {
                    ClientFMCGResponse clientFMCGResponse = getClientFmcgResFromListById(orderEntity.getClientFmcgId(), locationBulkRes.getClientFMCGResponseList());
                    String stateName = getStateNameResFromListById(clientFMCGResponse.getState(), locationBulkRes.getStateList()).getStateName();
                    String cityName = getCityNameResFromListById(clientFMCGResponse.getCity(), locationBulkRes.getCityList()).getCityName();
                    String regionName = getRegionNameResFromListById(clientFMCGResponse.getRegion(), locationBulkRes.getRegionList()).getRegionName();
                    BeetRespForOrderDto beetRespForOrderDto = getBeetResFromListById(orderEntity.getBeetId(), beetRespForOrderDtoList);
                    OutletRespForOrderDto outletRespForOrderDto = getOutletResFromListById(orderEntity.getOutletId(), outletRespForOrderDtoList);
                    totalGst += CalculateGst.calculateGstAmountFromTotal(orderEntity.getPrice(), productPriceRes.getGstPercentage());
                    totalSales += orderEntity.getPrice();
                    totalOrder += 1;
                    Double totalSaleByProduct = 0D;
                    List<OrderEntity> orderListByProductId = orderRepository.findByProductId(orderEntity.getProductId());
                    for (OrderEntity order : orderListByProductId) {
                        totalSaleByProduct += order.getPrice();
                    }
                    TopSellingProductRes resp = new TopSellingProductRes();
                    resp.setName(productRes.getName());
                    resp.setCity(cityName);
                    resp.setState(stateName);
                    resp.setRegion(regionName);
                    resp.setGstAmount(productPriceRes.getGstPercentage());
                    resp.setProductId(orderEntity.getProductId());
                    resp.setQuantitySold(orderListByProductId.size());
                    resp.setRevenue(totalSaleByProduct);
                    resp.setClientFMCGResponse(clientFMCGResponse);
                    resp.setBeetRespForOrderDto(beetRespForOrderDto);
                    resp.setOutletRespForOrderDto(outletRespForOrderDto);
                    topSellingProductRes.add(resp);
                }
            }
        }
        reportsResponse.setTotalSales(totalSales);
        reportsResponse.setTotalOrder(totalOrder);
        reportsResponse.setTotalGstCollected(totalGst);
        reportsResponse.setTopSellingProductList(topSellingProductRes);
        return reportsResponse;
    }

    private ProductRes getProductResFromListById(Long id, List<ProductRes> productResList){
        for(ProductRes productRes : productResList){
            if(Objects.equals(productRes.getProductId(), id)){
                return productRes;
            }
        }
        return new ProductRes();
    }
    private ClientFMCGResponse getClientFmcgResFromListById(Long id, List<ClientFMCGResponse> clientFMCGResponseList){
        for(ClientFMCGResponse clientFMCGResponse : clientFMCGResponseList){
            if(Objects.equals(clientFMCGResponse.getId(), id)){
                return clientFMCGResponse;
            }
        }
        return new ClientFMCGResponse();
    }

    private CityCustomResponse getCityNameResFromListById(Long id, List<CityCustomResponse> cityResponseList){
        for(CityCustomResponse resp : cityResponseList){
            if(Objects.equals(resp.getId(), id)){
                return resp;
            }
        }
        return new CityCustomResponse();
    }
    private StateCustomResponse getStateNameResFromListById(Long id, List<StateCustomResponse> stateResponseList){
        for(StateCustomResponse resp : stateResponseList){
            if(Objects.equals(resp.getId(), id)){
                return resp;
            }
        }
        return new StateCustomResponse();
    }
    private RegionCustomResponse getRegionNameResFromListById(Long id, List<RegionCustomResponse> regionResponseList){
        for(RegionCustomResponse resp : regionResponseList){
            if(Objects.equals(resp.getId(), id)){
                return resp;
            }
        }
        return new RegionCustomResponse();
    }
    private BeetRespForOrderDto getBeetResFromListById(Long id, List<BeetRespForOrderDto> beetRespForOrderDtoList){
        for(BeetRespForOrderDto resp : beetRespForOrderDtoList){
            if(Objects.equals(resp.getId(), id)){
                return resp;
            }
        }
        return new BeetRespForOrderDto();
    }
    private OutletRespForOrderDto getOutletResFromListById(Long id, List<OutletRespForOrderDto> outletRespForOrderDtoList){
        for(OutletRespForOrderDto resp : outletRespForOrderDtoList){
            if(Objects.equals(resp.getId(), id)){
                return resp;
            }
        }
        return new OutletRespForOrderDto();
    }

    public TenDayReportRes getLastTenDaysOrderByOutletIdAndMemberId(Long memberId, Long outletId, SalesLevel salesLevel) {
        Date endDate = new Date();
        LocalDate localDate = LocalDate.now().minusDays(10);
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date startDate = Date.from(instant);
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndMemberIdAndOutletIdAndSalesLevel(startDate, endDate, memberId, outletId, salesLevel);
        List<SamplesEntity> samplesEntityList = samplesRepo.findAllBySampleDateBetweenAndMemberIdAndOutletId(startDate, endDate, memberId, outletId);
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        List<SampleRes> sampleResList = new ArrayList<>();
        for (SamplesEntity samplesEntity : samplesEntityList) {
            SampleRes sampleRes = mapToSampleRes(samplesEntity);
            sampleResList.add(sampleRes);
        }
        return new TenDayReportRes(orderResponseList, sampleResList);
    }

    public List<SampleRes> getLastTenDaysSampleByDoctorIdAndMemberId(Long memberId, Long doctorId) {
        Date endDate = new Date();
        LocalDate localDate = LocalDate.now().minusDays(10);
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date startDate = Date.from(instant);
        List<SamplesEntity> samplesEntityList = samplesRepo.findAllBySampleDateBetweenAndMemberIdAndDoctorId(startDate, endDate, memberId, doctorId);
        List<SampleRes> sampleResList = new ArrayList<>();
        for (SamplesEntity samplesEntity : samplesEntityList) {
            SampleRes sampleRes = mapToSampleRes(samplesEntity);
            sampleResList.add(sampleRes);
        }
        return sampleResList;
    }

    public TenDayReportRes getLastTenDaysOrderByStockistAndMemberId(Long memberId, Long clientId, SalesLevel salesLevel) {
        Date endDate = new Date();
        LocalDate localDate = LocalDate.now().minusDays(10);
        Instant instant = localDate.atStartOfDay(ZoneId.systemDefault()).toInstant();
        Date startDate = Date.from(instant);
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndMemberIdAndClientFmcgIdAndSalesLevel(startDate, endDate, memberId, clientId, salesLevel);
        List<SamplesEntity> samplesEntityList = samplesRepo.findAllBySampleDateBetweenAndMemberIdAndClientFmcgId(startDate, endDate, memberId, clientId);
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        List<SampleRes> sampleResList = new ArrayList<>();
        for (SamplesEntity samplesEntity : samplesEntityList) {
            SampleRes sampleRes = mapToSampleRes(samplesEntity);
            sampleResList.add(sampleRes);
        }
        return new TenDayReportRes(orderResponseList, sampleResList);
    }

    public List<OrderResponse> findOverallSalesByDateAndSalesLevel(ReportsRequest reportsRequest) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant());
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }

    public List<OrderResponse> byDateAndSalesLevelAndMemberId(ReportsRequest reportsRequest, Long memberId) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevelAndMemberId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), memberId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }

    public List<OrderResponse> byDateAndSalesLevelAndReportingManagerId(ReportsRequest reportsRequest, Long managerId) {
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(managerId);
        List<OrderEntity> orderEntityList = orderRepository.findOrdersByStartDateAndEndDateAndMembersAndSalesLevel(reportsRequest.getStartDate(),reportsRequest.getSalesLevelConstant(), reportsRequest.getEndDate(),  memberIds);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }

    public List<SalesResForGraph> findOverallSalesByDateAndSalesLevelForGraph(ReportsRequest reportsRequest) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant());

        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToCustomOrderResponse1(orderEntity);
            orderResponseList.add(orderResponse);
        }
        Map<YearMonth, Double> monthlySalesMap = new HashMap<>();

        for (OrderResponse orderRes : orderResponseList) {
            LocalDate orderDate = ((java.sql.Date) orderRes.getOrderCreatedDate()).toLocalDate();
            YearMonth yearMonth = YearMonth.from(orderDate);

            monthlySalesMap.put(yearMonth, monthlySalesMap.getOrDefault(yearMonth, 0.0) + orderRes.getTotalPriceWithGst());
        }
        return monthlySalesMap.entrySet().stream().map(entry -> new SalesResForGraph(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public OrderResponse mapToCustomOrderResponse1(OrderEntity orderEntity) {
        OrderResponse orderResponse = new OrderResponse();
        orderResponse.setTotalPriceWithGst(orderEntity.getPrice());
        orderResponse.setOrderCreatedDate(orderEntity.getOrderCreatedDate());
        return orderResponse;
    }

    public List<SalesResForGraph> findOverallSalesByDateAndSalesLevelForGraphForState(ReportsRequest reportsRequest, Long stateId) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndSalesLevelAndStateId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), stateId);

        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToCustomOrderResponse1(orderEntity);
            orderResponseList.add(orderResponse);
        }
        Map<YearMonth, Double> monthlySalesMap = new HashMap<>();

        for (OrderResponse orderRes : orderResponseList) {
            LocalDate orderDate = ((java.sql.Date) orderRes.getOrderCreatedDate()).toLocalDate();
            YearMonth yearMonth = YearMonth.from(orderDate);

            monthlySalesMap.put(yearMonth, monthlySalesMap.getOrDefault(yearMonth, 0.0) + orderRes.getTotalPriceWithGst());
        }
        return monthlySalesMap.entrySet().stream().map(entry -> new SalesResForGraph(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public List<SalesResForGraph> findOverallSalesByDateAndSalesLevelForGraphForCity(ReportsRequest reportsRequest, Long cityId) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndSalesLevelAndCityId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), cityId);

        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToCustomOrderResponse1(orderEntity);
            orderResponseList.add(orderResponse);
        }
        Map<YearMonth, Double> monthlySalesMap = new HashMap<>();

        for (OrderResponse orderRes : orderResponseList) {
            LocalDate orderDate = ((java.sql.Date) orderRes.getOrderCreatedDate()).toLocalDate();
            YearMonth yearMonth = YearMonth.from(orderDate);

            monthlySalesMap.put(yearMonth, monthlySalesMap.getOrDefault(yearMonth, 0.0) + orderRes.getTotalPriceWithGst());
        }
        return monthlySalesMap.entrySet().stream().map(entry -> new SalesResForGraph(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public List<SalesResForGraph> findOverallSalesByDateAndSalesLevelForGraphForRegion(ReportsRequest reportsRequest, Long regionId) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndSalesLevelAndRegionId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), regionId);

        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToCustomOrderResponse1(orderEntity);
            orderResponseList.add(orderResponse);
        }
        Map<YearMonth, Double> monthlySalesMap = new HashMap<>();

        for (OrderResponse orderRes : orderResponseList) {
            LocalDate orderDate = ((java.sql.Date) orderRes.getOrderCreatedDate()).toLocalDate();
            YearMonth yearMonth = YearMonth.from(orderDate);

            monthlySalesMap.put(yearMonth, monthlySalesMap.getOrDefault(yearMonth, 0.0) + orderRes.getTotalPriceWithGst());
        }
        return monthlySalesMap.entrySet().stream().map(entry -> new SalesResForGraph(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public List<SalesResForGraph> findOverallSalesByDateAndSalesLevelForGraphForManager(ReportsRequest reportsRequest, Long managerId) {
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(managerId);
        List<OrderEntity> orderEntityList = orderRepository.findOrdersByDateRangeAndMembersAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getSalesLevelConstant(), reportsRequest.getEndDate(), memberIds);

        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToCustomOrderResponse1(orderEntity);
            orderResponseList.add(orderResponse);
        }
        Map<YearMonth, Double> monthlySalesMap = new HashMap<>();

        for (OrderResponse orderRes : orderResponseList) {
            LocalDate orderDate = ((java.sql.Date) orderRes.getOrderCreatedDate()).toLocalDate();
            YearMonth yearMonth = YearMonth.from(orderDate);

            monthlySalesMap.put(yearMonth, monthlySalesMap.getOrDefault(yearMonth, 0.0) + orderRes.getTotalPriceWithGst());
        }
        return monthlySalesMap.entrySet().stream().map(entry -> new SalesResForGraph(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public List<SalesResForGraph> findOverallSalesByDateAndSalesLevelForGraphForStateForManager(ReportsRequest reportsRequest, Long stateId, Long managerId) {
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(managerId);
        List<OrderEntity> orderEntityList = orderRepository.findOrdersByDateRangeAndMembersAndSalesLevelAndStateId(reportsRequest.getStartDate(), reportsRequest.getSalesLevelConstant(), reportsRequest.getEndDate(), stateId, memberIds);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToCustomOrderResponse1(orderEntity);
            orderResponseList.add(orderResponse);
        }
        Map<YearMonth, Double> monthlySalesMap = new HashMap<>();

        for (OrderResponse orderRes : orderResponseList) {
            LocalDate orderDate = ((java.sql.Date) orderRes.getOrderCreatedDate()).toLocalDate();
            YearMonth yearMonth = YearMonth.from(orderDate);

            monthlySalesMap.put(yearMonth, monthlySalesMap.getOrDefault(yearMonth, 0.0) + orderRes.getTotalPriceWithGst());
        }
        return monthlySalesMap.entrySet().stream().map(entry -> new SalesResForGraph(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public List<SalesResForGraph> findOverallSalesByDateAndSalesLevelForGraphForCityForManager(ReportsRequest reportsRequest, Long cityId, Long managerId) {
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(managerId);
        List<OrderEntity> orderEntityList = orderRepository.findOrdersByDateRangeAndMembersAndSalesLevelAndCityId(reportsRequest.getStartDate(), reportsRequest.getSalesLevelConstant(), reportsRequest.getEndDate(), cityId, memberIds);

        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToCustomOrderResponse1(orderEntity);
            orderResponseList.add(orderResponse);
        }
        Map<YearMonth, Double> monthlySalesMap = new HashMap<>();

        for (OrderResponse orderRes : orderResponseList) {
            LocalDate orderDate = ((java.sql.Date) orderRes.getOrderCreatedDate()).toLocalDate();
            YearMonth yearMonth = YearMonth.from(orderDate);

            monthlySalesMap.put(yearMonth, monthlySalesMap.getOrDefault(yearMonth, 0.0) + orderRes.getTotalPriceWithGst());
        }
        return monthlySalesMap.entrySet().stream().map(entry -> new SalesResForGraph(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public List<SalesResForGraph> findOverallSalesByDateAndSalesLevelForGraphForRegionForManager(ReportsRequest reportsRequest, Long regionId, Long managerId) {
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(managerId);
        List<OrderEntity> orderEntityList = orderRepository.findOrdersByDateRangeAndMembersAndSalesLevelAndRegionId(reportsRequest.getStartDate(), reportsRequest.getSalesLevelConstant(), reportsRequest.getEndDate(), regionId, memberIds);

        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToCustomOrderResponse1(orderEntity);
            orderResponseList.add(orderResponse);
        }
        Map<YearMonth, Double> monthlySalesMap = new HashMap<>();

        for (OrderResponse orderRes : orderResponseList) {
            LocalDate orderDate = ((java.sql.Date) orderRes.getOrderCreatedDate()).toLocalDate();
            YearMonth yearMonth = YearMonth.from(orderDate);

            monthlySalesMap.put(yearMonth, monthlySalesMap.getOrDefault(yearMonth, 0.0) + orderRes.getTotalPriceWithGst());
        }
        return monthlySalesMap.entrySet().stream().map(entry -> new SalesResForGraph(entry.getKey(), entry.getValue())).collect(Collectors.toList());
    }

    public List<OrderResponse> findOverallSalesByDateAndSalesLevelAndRegion(ReportsRequest reportsRequest, Long regionId) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndSalesLevelAndRegionId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), regionId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }

    public List<OrderResponse> findOverallSalesByDateAndSalesLevelAndState(ReportsRequest reportsRequest, Long stateId) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndSalesLevelAndStateId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), stateId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }

    public List<OrderResponse> findOverallSalesByDateAndSalesLevelAndCity(ReportsRequest reportsRequest, Long cityId) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndSalesLevelAndCityId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), cityId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }

    public List<OrderResponse> findOverallSalesByDateAndSalesLevelAndOutletId(Long outletId, ReportsRequest reportsRequest) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevelAndOutletId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), outletId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }

    public List<OrderResponse> findOverallSalesByDateAndSalesLevelAndClientFmcgId(Long clientFmcgId, ReportsRequest reportsRequest) {
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevelAndClientFmcgId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), clientFmcgId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        List<OrderResponse> orderResponseList = new ArrayList<>();
        for (OrderEntity orderEntity : orderEntityList) {
            OrderResponse orderResponse = mapToOrderResponse(orderEntity);
            orderResponseList.add(orderResponse);
        }
        return orderResponseList;
    }

    //Member Report
    public PaginatedResp<BeetReportResponse> getBeetOrderReportByMemberIdWithDateFilter(Long memberId, Date startDate, Date endDate, int page, int pageSize, String sortBy, String sortDirection) {
        Map<Long, Double> beetOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findAllByOrderCreatedDateBetweenAndMemberId(startDate, endDate, memberId, pageable);

        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            if (order.getSalesLevel() != SalesLevel.WAREHOUSE) {
                beetOrderMap.put(order.getBeetId(), beetOrderMap.getOrDefault(order.getBeetId(), 0.0) + order.getPrice());
            }
        }
        Set<Long> beetIds = beetOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(beetIds);
        for (Map.Entry<Long, Double> entry : beetOrderMap.entrySet()) {
            for (BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList) {
                if (Objects.equals(beetRespForOrderDto.getId(), entry.getKey())) {
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalSales(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponse.setTotalOrder((long) orderRepository.findAllByOrderCreatedDateBetweenAndMemberIdAndBeetId(startDate, endDate, memberId, entry.getKey()).size());
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalSales).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }

    public PaginatedResp<BeetReportResponse> getBeetOrderReportByReportingManagerIdWithDateFilter(Long reportingManagerId, Date startDate, Date endDate, int page, int pageSize, String sortBy, String sortDirection) {
        Map<Long, Double> beetOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(reportingManagerId);
        Page<OrderEntity> orderEntityPage = orderRepository.findOrdersByDateRangeAndMembers(startDate, endDate, memberIds, pageable);
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            if (order.getSalesLevel() != SalesLevel.WAREHOUSE) {
                beetOrderMap.put(order.getBeetId(), beetOrderMap.getOrDefault(order.getBeetId(), 0.0) + order.getPrice());
            }
        }
        Set<Long> beetIds = beetOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(beetIds);
        for (Map.Entry<Long, Double> entry : beetOrderMap.entrySet()) {
            for (BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList) {
                if (Objects.equals(beetRespForOrderDto.getId(), entry.getKey())) {
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalSales(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponse.setTotalOrder((long) orderRepository.findOrdersByDateRangeAndMembersAndBeetId(startDate, endDate, memberIds, entry.getKey()).size());
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalSales).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }

    public PaginatedResp<OutletReportResponse> getOutletOrderReportByBeetIdWithDateFilter(Long beetId, Date startDate, Date endDate, int page, int pageSize, String sortBy, String sortDirection) {
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findAllByOrderCreatedDateBetweenAndBeetId(startDate, endDate, beetId, pageable);
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            if (order.getSalesLevel() != SalesLevel.WAREHOUSE) {
                outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0.0) + order.getPrice());
            }
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList) {
                if (Objects.equals(outletRespForOrderDto.getId(), entry.getKey())) {
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalSales(entry.getValue());
                    outletReportResponse.setTotalOrder((long) orderRepository.findAllByOrderCreatedDateBetweenAndOutletId(startDate, endDate, entry.getKey()).size());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalSales).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }

    public PaginatedResp<OutletReportResponse> getAllOrderByEachOutletByMemberIdByProductiveStatus(Long memberId, OrderCallStatus orderCallStatus, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("inside of getAllProductiveOrderByEachOutletByMemberId function in report controller");
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberIdAndOrderCallStatusAndSalesLevelNot(memberId, orderCallStatus, SalesLevel.WAREHOUSE, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList) {
                if (Objects.equals(outletRespForOrderDto.getId(), entry.getKey())) {
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalOrder(orderRepository.countByOutletIdAndMemberIdAndOrderCallStatus(entry.getKey(), memberId, orderCallStatus));
                    outletReportResponse.setTotalSales(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }

    public PaginatedResp<OutletReportResponse> getAllOrderByEachOutletByMemberIdByOrderMedium(Long memberId, OrderMedium orderMedium, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("inside of getAllProductiveOrderByEachOutletByMemberId function in report controller");
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberIdAndOrderMediumAndSalesLevelNot(memberId, orderMedium, SalesLevel.WAREHOUSE, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList) {
                if (Objects.equals(outletRespForOrderDto.getId(), entry.getKey())) {
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalOrder(orderRepository.countByOutletIdAndMemberIdAndOrderMedium(entry.getKey(), memberId, orderMedium));
                    outletReportResponse.setTotalSales(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }

    public PaginatedResp<BeetReportResponse> getAllOrderByEachBeetByMemberIdByProductiveStatus(Long memberId, OrderCallStatus orderCallStatus, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("inside of getAllProductiveOrderByEachBeetByMemberId function in report controller");
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberIdAndOrderCallStatusAndSalesLevelNot(memberId, orderCallStatus, SalesLevel.WAREHOUSE, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getBeetId(), outletOrderMap.getOrDefault(order.getBeetId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList) {
                if (Objects.equals(beetRespForOrderDto.getId(), entry.getKey())) {
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalOrder(orderRepository.countByBeetIdAndMemberIdAndOrderCallStatus(entry.getKey(), memberId, orderCallStatus));
                    beetReportResponse.setTotalSales(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }

    public PaginatedResp<BeetReportResponse> getAllOrderByEachBeetByMemberIdByOrderMedium(Long memberId, OrderMedium orderMedium, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("inside of getAllProductiveOrderByEachBeetByMemberId function in report controller");
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByMemberIdAndOrderMediumAndSalesLevelNot(memberId, orderMedium, SalesLevel.WAREHOUSE, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getBeetId(), outletOrderMap.getOrDefault(order.getBeetId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList) {
                if (Objects.equals(beetRespForOrderDto.getId(), entry.getKey())) {
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalOrder(orderRepository.countByBeetIdAndMemberIdAndOrderMedium(entry.getKey(), memberId, orderMedium));
                    beetReportResponse.setTotalSales(entry.getValue());
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
    public PaginatedResp<OutletReportResponse> getAllOrderByEachOutletByClientFmcgIdByProductiveStatus(Long clientFmcgId, OrderCallStatus orderCallStatus, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("inside of getAllProductiveOrderByEachOutletByMemberId function in report controller");
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndOrderCallStatusAndSalesLevelNot(clientFmcgId, orderCallStatus, SalesLevel.WAREHOUSE, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList) {
                if (Objects.equals(outletRespForOrderDto.getId(), entry.getKey())) {
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalOrder(orderRepository.countByOutletIdAndClientFmcgIdAndOrderCallStatus(entry.getKey(), clientFmcgId, orderCallStatus));
                    outletReportResponse.setTotalSales(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }

    public PaginatedResp<OutletReportResponse> getAllOrderByEachOutletByClientFmcgIdByOrderMedium(Long clientFmcgId, OrderMedium orderMedium, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("inside of getAllProductiveOrderByEachOutletByMemberId function in report controller");
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndOrderMediumAndSalesLevelNot(clientFmcgId, orderMedium, SalesLevel.WAREHOUSE, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<OutletReportResponse> outletReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getOutletId(), outletOrderMap.getOrDefault(order.getOutletId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<OutletRespForOrderDto> outletRespForOrderDtoList = productServiceClient.getOutlets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList) {
                if (Objects.equals(outletRespForOrderDto.getId(), entry.getKey())) {
                    OutletReportResponse outletReportResponse = new OutletReportResponse();
                    outletReportResponse.setTotalOrder(orderRepository.countByOutletIdAndClientFmcgIdAndOrderMedium(entry.getKey(), clientFmcgId, orderMedium));
                    outletReportResponse.setTotalSales(entry.getValue());
                    outletReportResponse.setOutletRespForOrderDto(outletRespForOrderDto);
                    outletReportResponsesList.add(outletReportResponse);
                    break;
                }
            }
        }
        outletReportResponsesList.sort(Comparator.comparingDouble(OutletReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, outletReportResponsesList);
    }

    public PaginatedResp<BeetReportResponse> getAllOrderByEachBeetByClientFmcgIdByProductiveStatus(Long clientFmcgId, OrderCallStatus orderCallStatus, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("inside of getAllProductiveOrderByEachBeetByMemberId function in report controller");
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndOrderCallStatusAndSalesLevelNot(clientFmcgId, orderCallStatus, SalesLevel.WAREHOUSE, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getBeetId(), outletOrderMap.getOrDefault(order.getBeetId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList) {
                if (Objects.equals(beetRespForOrderDto.getId(), entry.getKey())) {
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalOrder(orderRepository.countByBeetIdAndClientFmcgIdAndOrderCallStatus(entry.getKey(), clientFmcgId, orderCallStatus));
                    beetReportResponse.setTotalSales(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }

    public PaginatedResp<BeetReportResponse> getAllOrderByEachBeetByClientFmcgIdByOrderMedium(Long clientFmcgId, OrderMedium orderMedium, int page, int pageSize, String sortBy, String sortDirection) {
        log.info("inside of getAllProductiveOrderByEachBeetByMemberId function in report controller");
        Map<Long, Double> outletOrderMap = new HashMap<>();
        Sort sort = sortDirection.equalsIgnoreCase(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<OrderEntity> orderEntityPage = orderRepository.findByClientFmcgIdAndOrderMediumAndSalesLevelNot(clientFmcgId, orderMedium, SalesLevel.WAREHOUSE, pageable);
        log.info("api called findByMemberIdAndOrderCallStatus");
        List<BeetReportResponse> beetReportResponsesList = new ArrayList<>();
        for (OrderEntity order : orderEntityPage.getContent()) {
            outletOrderMap.put(order.getBeetId(), outletOrderMap.getOrDefault(order.getBeetId(), 0.0) + order.getPrice());
        }
        Set<Long> outletIds = outletOrderMap.keySet();
        List<BeetRespForOrderDto> beetRespForOrderDtoList = productServiceClient.getBeets(outletIds);
        log.info("going for loop");
        for (Map.Entry<Long, Double> entry : outletOrderMap.entrySet()) {
            for (BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList) {
                if (Objects.equals(beetRespForOrderDto.getId(), entry.getKey())) {
                    BeetReportResponse beetReportResponse = new BeetReportResponse();
                    beetReportResponse.setTotalOrder(orderRepository.countByBeetIdAndClientFmcgIdAndOrderMedium(entry.getKey(), clientFmcgId, orderMedium));
                    beetReportResponse.setTotalSales(entry.getValue());
                    beetReportResponse.setBeetRespForOrderDto(beetRespForOrderDto);
                    beetReportResponsesList.add(beetReportResponse);
                    break;
                }
            }
        }
        beetReportResponsesList.sort(Comparator.comparingDouble(BeetReportResponse::getTotalOrder).reversed());
        return new PaginatedResp<>(orderEntityPage.getTotalElements(), orderEntityPage.getTotalPages(), page, beetReportResponsesList);
    }

    public OrderResponse mapToOrderResponse(OrderEntity orderEntity) {
        OrderResponse orderResponse = new OrderResponse();
        if (orderEntity.getOutletId() == null) {
            orderEntity.setOutletId(0L);
        }
        CombineRes combineRes = externalRestService.getCombineResForBeetAndOutletAndMemberAndClient(orderEntity.getOutletId(), orderEntity.getMemberId(), orderEntity.getClientFmcgId());
        orderResponse.setBeetRespForOrderDto(combineRes.getBeetRespForOrderDto());
        orderResponse.setOutletRespForOrderDto(combineRes.getOutletRespForOrderDto());
        orderResponse.setOrderId(orderEntity.getId());
//        if (orderEntity.getOutletId() != null) {
//            log.info("fetch details from SFA Outlet controller");
//            orderResponse.setOutletRespForOrderDto(productServiceClient.getOutletForReport(orderEntity.getOutletId()));
//        }
//        if (orderEntity.getBeetId() != null) {
//            log.info("fetch details from SFA ");
//            orderResponse.setBeetRespForOrderDto(productServiceClient.getBeetForReport(orderEntity.getBeetId()));
//        }
        orderResponse.setBundleType(orderEntity.getBundleType());
        orderResponse.setQuantity(orderEntity.getQuantity());
        orderResponse.setProductRes(productServiceClient.getProduct(orderEntity.getProductId()));
        orderResponse.setProductId(orderEntity.getProductId());
        orderResponse.setInvoiceNumber(orderEntity.getInvoiceNumber());
        orderResponse.setOrderCreatedDate(orderEntity.getOrderCreatedDate());
        orderResponse.setOrderMedium(orderEntity.getOrderMedium());
        orderResponse.setOrderCallStatus(orderEntity.getOrderCallStatus());
        orderResponse.setTotalPriceWithGst(orderEntity.getPrice());
        Double priceOfOrderWithRespectedSalesLevel = getProductPrice(orderResponse.getProductRes(), getPriceType(orderEntity.getSalesLevel()));
        orderResponse.setTotalPrice(priceOfOrderWithRespectedSalesLevel * orderEntity.getQuantity());
        orderResponse.setOrderCreatedDate(orderEntity.getOrderCreatedDate());
        orderResponse.setClientId(orderEntity.getClientFmcgId());
        orderResponse.setRemarks(orderEntity.getRemarks());
        MemberGetDto member = combineRes.getMemberGetDto();
        orderResponse.setMemberId(orderEntity.getMemberId());
        orderResponse.setMemberName(member.getFirstName() + " " + member.getLastName());
        ClientFMCGResponse client = combineRes.getClientFMCGResponse();
        orderResponse.setClientName(client.getClientFirstName() + " " + client.getClientLastName());
        orderResponse.setClientBalanceAmount(client.getTopUpBalance());
        orderResponse.setDiscountCode(orderEntity.getDiscountCode());
        orderResponse.setMemberResponse(member);
        orderResponse.setPriceAfterDiscount(orderEntity.getPriceAfterDiscount());
        return orderResponse;
    }

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

    public Double getProductPrice(ProductRes productRes, String priceType) {
//        log.info("Get product price with product id: {} and price type: {}", productId, priceType);
//        return productServiceClient.getProductPrice(productId, priceType);
        return switch (priceType.toLowerCase()) {
            case "warehouse" -> productRes.getProductPriceRes().getWarehousePrice();
            case "stocklist" -> productRes.getProductPriceRes().getStockListPrice();
            case "retailer" -> productRes.getProductPriceRes().getRetailerPrice();
            case "gst" -> productRes.getProductPriceRes().getGstPercentage();
            default ->
                    throw new InvalidInputException(ApiErrorCodes.INVALID_INPUT.getErrorCode(), ApiErrorCodes.INVALID_INPUT.getErrorMessage());
        };
    }

    private SampleRes mapToSampleRes(SamplesEntity sample) {
        SampleRes sampleRes = new SampleRes();
        sampleRes.setId(sample.getId());
        sampleRes.setBundleType(sample.getBundleType());
        sampleRes.setSampleDate(sample.getSampleDate());
        sampleRes.setProductRes(productServiceClient.getProduct(sample.getProductId()));
        sampleRes.setQuantity(sample.getQuantity());
        sampleRes.setMemberResponse(externalRestService.getMember(sample.getMemberId()));
        if (sample.getDoctorId() != null) {
            sampleRes.setDoctorRes(externalRestService.getDoctor(sample.getDoctorId()));
        } if (sample.getClientFmcgId() != null) {
            sampleRes.setClientFMCGResponse(externalRestService.getClient(sample.getClientFmcgId()));
        } if (sample.getOutletId() != null) {
            sampleRes.setOutletRespForOrderDto(externalRestService.getOutletByIdWithResp(sample.getOutletId()));
        }
        return sampleRes;
    }

    public List<MemberSalesResponse> totalSalesByDateAndSalesLevelWithGroupByMembers(ReportsRequest reportsRequest) {
        log.info("Get overall sales by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant());
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by MemberId and calculate Total Sales per member");
        Map<Long, Double> memberSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getMemberId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        return memberSalesMap.entrySet().stream().map(entry -> buildMemberSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(MemberSalesResponse::getTotalSales).reversed()) // Sort by total sales DESC
                .collect(Collectors.toList());
    }

    private MemberSalesResponse buildMemberSalesResponse(Long memberId, Double totalSales) {
        log.info("Get member with member id: {}", memberId);
        MemberGetDto member = externalRestService.getMember(memberId);
        log.info("Making Response with MemberId, Member Name & total sales");
        return new MemberSalesResponse(memberId, member.getFirstName() + " " + member.getLastName(), totalSales);
    }

    public List<ProductSalesResponse> totalSalesByDateAndSalesLevelWithGroupByProduct(ReportsRequest reportsRequest, Long memberId) {
        log.info("Get sales by product filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevelAndMemberId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), memberId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getProductId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<ProductSalesResponse> productSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildProductSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(ProductSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        List<Long> productIds = new ArrayList<>();
        for(ProductSalesResponse productSalesResponse : productSalesResponseList){
            productIds.add(productSalesResponse.getProductId());
        }
        List<ProductRes> productResList = externalRestService.getAllProductByIds(productIds);
        List<ProductSalesResponse> updatedProductSaleResponseList = new ArrayList<>();
        for(ProductSalesResponse productSalesResponse : productSalesResponseList){
            ProductRes productRes = fetchProductResFromList(productResList, productSalesResponse.getProductId());
            productSalesResponse.setProductName(productRes.getName());
            productSalesResponse.setSku(productRes.getSku());
            productSalesResponse.setProductImageUrl(productRes.getImageUrl());
            updatedProductSaleResponseList.add(productSalesResponse);
        }
        return updatedProductSaleResponseList;
    }

    //Beet Report For member by sale
    public List<BeetSalesResponse> totalSalesByDateWithGroupByBeetAndMemberId(ReportsRequest reportsRequest, Long memberId) {
        log.info("Get sales by beet filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevelAndMemberId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), memberId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getBeetId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<BeetSalesResponse> beetSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildBeetSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(BeetSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        Set<Long> beetIds = new HashSet<>();
        for(BeetSalesResponse beetSalesResponse : beetSalesResponseList){
            beetIds.add(beetSalesResponse.getBeetRespForOrderDto().getId());
        }
        List<BeetRespForOrderDto> beetResList = productServiceClient.getBeets(beetIds);
        List<BeetSalesResponse> updatedBeetSaleResponseList = new ArrayList<>();
        for(BeetSalesResponse beetSalesResponse : beetSalesResponseList){
            BeetRespForOrderDto beetRespForOrderDto = fetchBeetResFromList(beetResList, beetSalesResponse.getBeetRespForOrderDto().getId());
            beetSalesResponse.setBeetRespForOrderDto(beetRespForOrderDto);
            updatedBeetSaleResponseList.add(beetSalesResponse);
        }
        return updatedBeetSaleResponseList;
    }
    //Beet Report For super admin by sale
    public List<BeetSalesResponse> totalSalesByDateWithGroupByBeetForSuperAdmin(ReportsRequest reportsRequest) {
        log.info("Get sales by beet filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant());
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getBeetId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<BeetSalesResponse> beetSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildBeetSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(BeetSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        Set<Long> beetIds = new HashSet<>();
        for(BeetSalesResponse beetSalesResponse : beetSalesResponseList){
            beetIds.add(beetSalesResponse.getBeetRespForOrderDto().getId());
        }
        List<BeetRespForOrderDto> beetResList = productServiceClient.getBeets(beetIds);
        List<BeetSalesResponse> updatedBeetSaleResponseList = new ArrayList<>();
        for(BeetSalesResponse beetSalesResponse : beetSalesResponseList){
            BeetRespForOrderDto beetRespForOrderDto = fetchBeetResFromList(beetResList, beetSalesResponse.getBeetRespForOrderDto().getId());
            beetSalesResponse.setBeetRespForOrderDto(beetRespForOrderDto);
            updatedBeetSaleResponseList.add(beetSalesResponse);
        }
        return updatedBeetSaleResponseList;
    }
    //Beet Report For reporting manager by sale
    public List<BeetSalesResponse> totalSalesByDateWithGroupByBeetForReportingManager(ReportsRequest reportsRequest, Long managerId) {
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(managerId);
        log.info("Get sales by beet filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findOrdersByStartDateAndEndDateAndMembersAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getSalesLevelConstant(), reportsRequest.getEndDate(), memberIds);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getBeetId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<BeetSalesResponse> beetSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildBeetSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(BeetSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        Set<Long> beetIds = new HashSet<>();
        for(BeetSalesResponse beetSalesResponse : beetSalesResponseList){
            beetIds.add(beetSalesResponse.getBeetRespForOrderDto().getId());
        }
        List<BeetRespForOrderDto> beetResList = productServiceClient.getBeets(beetIds);
        List<BeetSalesResponse> updatedBeetSaleResponseList = new ArrayList<>();
        for(BeetSalesResponse beetSalesResponse : beetSalesResponseList){
            BeetRespForOrderDto beetRespForOrderDto = fetchBeetResFromList(beetResList, beetSalesResponse.getBeetRespForOrderDto().getId());
            beetSalesResponse.setBeetRespForOrderDto(beetRespForOrderDto);
            updatedBeetSaleResponseList.add(beetSalesResponse);
        }
        return updatedBeetSaleResponseList;
    }

    //Outlet Report For member by sale
    public List<OutletSalesResponse> totalSalesByDateWithGroupByOutletAndMemberId(ReportsRequest reportsRequest, Long memberId) {
        log.info("Get sales by beet filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevelAndMemberId(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), memberId);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getOutletId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<OutletSalesResponse> outletSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildOutletSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(OutletSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        Set<Long> outletIds = new HashSet<>();
        for(OutletSalesResponse outletSalesResponse : outletSalesResponseList){
            outletIds.add(outletSalesResponse.getOutletRespForOrderDto().getId());
        }
        List<OutletRespForOrderDto> outletResList = productServiceClient.getOutlets(outletIds);
        List<OutletSalesResponse> updatedOutletSaleResponseList = new ArrayList<>();
        for(OutletSalesResponse outletSalesResponse : outletSalesResponseList){
            OutletRespForOrderDto outletRespForOrderDto = fetchOutletResFromList(outletResList, outletSalesResponse.getOutletRespForOrderDto().getId());
            outletSalesResponse.setOutletRespForOrderDto(outletRespForOrderDto);
            updatedOutletSaleResponseList.add(outletSalesResponse);
        }
        return updatedOutletSaleResponseList;
    }
    //Outlet Report For super admin by sale
    public List<OutletSalesResponse> totalSalesByDateWithGroupByOutletForSuperAdmin(ReportsRequest reportsRequest) {
        log.info("Get sales by beet filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findAllByCreatedDateBetweenAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant());
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getOutletId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<OutletSalesResponse> outletSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildOutletSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(OutletSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        Set<Long> outletIds = new HashSet<>();
        for(OutletSalesResponse outletSalesResponse : outletSalesResponseList){
            outletIds.add(outletSalesResponse.getOutletRespForOrderDto().getId());
        }
        List<OutletRespForOrderDto> outletResList = productServiceClient.getOutlets(outletIds);
        List<OutletSalesResponse> updatedOutletSaleResponseList = new ArrayList<>();
        for(OutletSalesResponse outletSalesResponse : outletSalesResponseList){
            OutletRespForOrderDto outletRespForOrderDto = fetchOutletResFromList(outletResList, outletSalesResponse.getOutletRespForOrderDto().getId());
            outletSalesResponse.setOutletRespForOrderDto(outletRespForOrderDto);
            updatedOutletSaleResponseList.add(outletSalesResponse);
        }
        return updatedOutletSaleResponseList;
    }
    //Outlet Report For reporting manager by sale
    public List<OutletSalesResponse> totalSalesByDateWithGroupByOutletForReportingManager(ReportsRequest reportsRequest,Long managerId) {
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(managerId);
        log.info("Get sales by beet filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findOrdersByStartDateAndEndDateAndMembersAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getSalesLevelConstant(), reportsRequest.getEndDate(), memberIds);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getOutletId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<OutletSalesResponse> outletSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildOutletSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(OutletSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        Set<Long> outletIds = new HashSet<>();
        for(OutletSalesResponse outletSalesResponse : outletSalesResponseList){
            outletIds.add(outletSalesResponse.getOutletRespForOrderDto().getId());
        }
        List<OutletRespForOrderDto> outletResList = productServiceClient.getOutlets(outletIds);
        List<OutletSalesResponse> updatedOutletSaleResponseList = new ArrayList<>();
        for(OutletSalesResponse outletSalesResponse : outletSalesResponseList){
            OutletRespForOrderDto outletRespForOrderDto = fetchOutletResFromList(outletResList, outletSalesResponse.getOutletRespForOrderDto().getId());
            outletSalesResponse.setOutletRespForOrderDto(outletRespForOrderDto);
            updatedOutletSaleResponseList.add(outletSalesResponse);
        }
        return updatedOutletSaleResponseList;
    }

    private ProductRes fetchProductResFromList(List<ProductRes> productResList, Long productId){
        for(ProductRes productRes : productResList){
            if(Objects.equals(productRes.getProductId(), productId)){
                return productRes;
            }
        }
        return new ProductRes();
    }
    private BeetRespForOrderDto fetchBeetResFromList(List<BeetRespForOrderDto> beetRespForOrderDtoList, Long beetId){
        for(BeetRespForOrderDto beetRespForOrderDto : beetRespForOrderDtoList){
            if(Objects.equals(beetRespForOrderDto.getId(), beetId)){
                return beetRespForOrderDto;
            }
        }
        return new BeetRespForOrderDto();
    }
    private OutletRespForOrderDto fetchOutletResFromList(List<OutletRespForOrderDto> outletRespForOrderDtoList, Long beetId){
        for(OutletRespForOrderDto outletRespForOrderDto : outletRespForOrderDtoList){
            if(Objects.equals(outletRespForOrderDto.getId(), beetId)){
                return outletRespForOrderDto;
            }
        }
        return new OutletRespForOrderDto();
    }
    private ProductSalesResponse buildProductSalesResponse(Long productId, Double totalSales) {
        log.info("Get product with product id: {}", productId);
        ProductSalesResponse productSalesResponse = new ProductSalesResponse();
        productSalesResponse.setProductId(productId);
        productSalesResponse.setTotalSales(totalSales);
        log.info("Making Response with ProductId, Product Name & total sales");
        return productSalesResponse;
    }
    private BeetSalesResponse buildBeetSalesResponse(Long beetId, Double totalSales) {
        BeetRespForOrderDto beetRespForOrderDto = new BeetRespForOrderDto();
        beetRespForOrderDto.setId(beetId);
        return new BeetSalesResponse(beetRespForOrderDto, totalSales);
    }
    private OutletSalesResponse buildOutletSalesResponse(Long outletId, Double totalSales) {
        OutletRespForOrderDto outletRespForOrderDto = new OutletRespForOrderDto();
        outletRespForOrderDto.setId(outletId);
        return new OutletSalesResponse(outletRespForOrderDto, totalSales);
    }

    public List<MemberSalesResponse> totalSalesByDateAndSalesLevelAndReportingManagerIdWithGroupByMembers(ReportsRequest reportsRequest,Long reportingManagerId) {
        log.info("Fetch list of Members Ids by Reporting manager");
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(reportingManagerId);
        log.info("Fetch order by Start date and End Date And Sales level and members-list under Reporting manager");
        List<OrderEntity> orderEntityList =orderRepository.findOrdersByStartDateAndEndDateAndMembersAndSalesLevel(reportsRequest.getStartDate(),reportsRequest.getSalesLevelConstant(), reportsRequest.getEndDate(), memberIds);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by memberId and calculate total sales per member");
        Map<Long, Double> memberSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getMemberId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        return memberSalesMap.entrySet().stream().map(entry -> buildMemberSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(MemberSalesResponse::getTotalSales).reversed()) // Sort by total sales DESC
                .collect(Collectors.toList());
    }

    public List<ProductSalesResponse> totalSalesByDateAndSalesLevelAndReportingManagerIdWithGroupByProduct(ReportsRequest reportsRequest, Long reportingManagerId) {
        Set<Long> memberIds = productServiceClient.getAllMemberIdsByReportingManager(reportingManagerId);
        log.info("Get sales by product filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndSalesLevelAndMemberIdIn(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant(), memberIds);
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getProductId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<ProductSalesResponse> productSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildProductSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(ProductSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        List<Long> productIds = new ArrayList<>();
        for(ProductSalesResponse productSalesResponse : productSalesResponseList){
            productIds.add(productSalesResponse.getProductId());
        }
        List<ProductRes> productResList = externalRestService.getAllProductByIds(productIds);
        List<ProductSalesResponse> updatedProductSaleResponseList = new ArrayList<>();
        for(ProductSalesResponse productSalesResponse : productSalesResponseList){
            ProductRes productRes = fetchProductResFromList(productResList, productSalesResponse.getProductId());
            productSalesResponse.setProductName(productRes.getName());
            productSalesResponse.setSku(productRes.getSku());
            productSalesResponse.setProductImageUrl(productRes.getImageUrl());
            updatedProductSaleResponseList.add(productSalesResponse);
        }
        return updatedProductSaleResponseList;
    }
    public List<ProductSalesResponse> totalSalesByDateAndSalesLevelWithGroupByProduct(ReportsRequest reportsRequest) {
        log.info("Get sales by product filtered by member with date range: {} and sales level: {}", reportsRequest.getStartDate(), reportsRequest.getEndDate());
        List<OrderEntity> orderEntityList = orderRepository.findAllByOrderCreatedDateBetweenAndSalesLevel(reportsRequest.getStartDate(), reportsRequest.getEndDate(), reportsRequest.getSalesLevelConstant());
        if (orderEntityList.isEmpty()) {
            return Collections.emptyList();
        }
        log.info("Group orders by productId and calculate total sales per product");
        Map<Long, Double> productSalesMap = orderEntityList.stream().collect(Collectors.groupingBy(OrderEntity::getProductId, Collectors.summingDouble(order -> order.getPriceAfterDiscount() != null ? order.getPriceAfterDiscount() : order.getPrice())));
        log.info(" Prepare the response list with sorted sales data");
        List<ProductSalesResponse> productSalesResponseList = productSalesMap.entrySet().stream().map(entry -> buildProductSalesResponse(entry.getKey(), entry.getValue())).sorted(Comparator.comparingDouble(ProductSalesResponse::getTotalSales).reversed()).toList(); // Sort by total sales DESC.toList();
        List<Long> productIds = new ArrayList<>();
        for(ProductSalesResponse productSalesResponse : productSalesResponseList){
            productIds.add(productSalesResponse.getProductId());
        }
        List<ProductRes> productResList = externalRestService.getAllProductByIds(productIds);
        List<ProductSalesResponse> updatedProductSaleResponseList = new ArrayList<>();
        for(ProductSalesResponse productSalesResponse : productSalesResponseList){
            ProductRes productRes = fetchProductResFromList(productResList, productSalesResponse.getProductId());
            productSalesResponse.setProductName(productRes.getName());
            productSalesResponse.setSku(productRes.getSku());
            productSalesResponse.setProductImageUrl(productRes.getImageUrl());
            updatedProductSaleResponseList.add(productSalesResponse);
        }
        return updatedProductSaleResponseList;
    }
}