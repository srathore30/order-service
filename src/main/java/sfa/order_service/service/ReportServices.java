package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfa.order_service.dto.request.ReportsRequest;
import sfa.order_service.dto.response.ProductPriceRes;
import sfa.order_service.dto.response.ProductRes;
import sfa.order_service.dto.response.ReportsResponse;
import sfa.order_service.dto.response.TopSellingProductRes;
import sfa.order_service.entity.OrderEntity;
import sfa.order_service.repo.OrderRepository;
import sfa.order_service.util.CalculateGst;
import sfa.order_service.util.DateFormatter;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

@Service
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
}





