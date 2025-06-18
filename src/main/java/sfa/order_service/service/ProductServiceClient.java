package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfa.order_service.Configs.TokenContext;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.response.BeetRespForOrderDto;
import sfa.order_service.dto.response.OutletRespForOrderDto;
import sfa.order_service.dto.response.ProductRes;
import sfa.order_service.exception.BusinessServiceException;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {
    private final RestTemplate restTemplate;
    @Value("${products.service.url}")
    private String productServiceUrl;
    @Value("${product.getProduct.url}")
    private String productUrl;
    @Value("${members.memberIds.url}")
    private String memberIdsUrl;
    @Value("${beets.getAllBeets.url}")
    private String beetUrl;
    @Value("${beets.getBeetForReport.url}")
    private String beetReportUrl;
    @Value("${outlets.getAllOutlet.url}")
    private String outletUrl;
    @Value("${outlets.getOutForReport.url}")
    private String outletReportUrl;
    @Value("${name.state.url}")
    private String stateUrl;
    @Value("${name.city.url}")
    private String cityUrl;
    @Value("${name.region.url}")
    private String regionUrl;

    private HttpHeaders createHeaders() {
        log.info("Helper method to create HTTP headers with the token");
        log.info("Token: {}", TokenContext.getToken());
        String token = TokenContext.getToken();
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            log.info("Add token to headers");
            headers.set("Authorization", "Bearer " + token);
        }
        return headers;
    }

    public Double getProductPrice(Long productId, String priceType) {
        try {
            log.info("While calling getProductPrice from product service client");
            String url = productServiceUrl + "/products/getByIdAndPriceType/" + productId + "?priceType=" + priceType;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            log.info("Fetch product details with authorization header");
            ResponseEntity<Double> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Double.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public ProductRes getProduct(Long productId) {
        try {
            log.info("While calling getProduct from product service client");
            String url = productUrl + productId;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            log.info("Fetch product details with authorization header");
            ResponseEntity<ProductRes> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ProductRes.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public List<BeetRespForOrderDto> getBeets(Set<Long> beetIds) {
        try {
            log.info("While calling getBeets from product service client");
            String url = beetUrl;
            HttpEntity<Set<Long>> requestEntity = new HttpEntity<>(beetIds, createHeaders());
            return restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<BeetRespForOrderDto>>() {
            }).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public List<OutletRespForOrderDto> getOutlets(Set<Long> outletIds) {
        try {
            log.info("While calling getOutlets from product service client");
            String url = outletUrl;
            HttpEntity<Set<Long>> requestEntity = new HttpEntity<>(outletIds, createHeaders());
            return restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<OutletRespForOrderDto>>() {
            }).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public Set<Long> getAllMemberIdsByReportingManager(Long reportingManager) {
        try {
            log.info("While calling getAllMemberIdsByReportingManager from product service client");
            String url = memberIdsUrl + "/" + reportingManager;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, new ParameterizedTypeReference<Set<Long>>() {
            }).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
    public BeetRespForOrderDto getBeetForReport(Long beetId) {
        try {
            log.info("While calling getBeetForReport from product service client");
            String url = beetReportUrl + "/" + beetId;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, BeetRespForOrderDto.class).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
    public OutletRespForOrderDto getOutletForReport(Long outletId) {
        try {
            log.info("While calling getOutletForReport from product service client");
            String url = outletReportUrl + "/" + outletId;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, OutletRespForOrderDto.class).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
    public String getStateNameById(Long id) {
        try {
            log.info("While calling getStateNameById from product service client");
            String url = stateUrl + "/" + id;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
    public String getCityNameById(Long id) {
        try {
            log.info("While calling getCityNameById from product service client");
            String url = cityUrl + "/" + id;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
    public String getRegionNameById(Long id) {
        try {
            log.info("While calling getRegionNameById from product service client");
            String url = regionUrl + "/" + id;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            return restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
}