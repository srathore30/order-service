package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfa.order_service.Configs.TokenContext;
import sfa.order_service.constant.ApiErrorCodes;
import sfa.order_service.dto.request.ClientFMCGUpdateRequest;
import sfa.order_service.dto.request.InventoryUpdateRequest;
import sfa.order_service.dto.request.UpdateBjpAndDjpOrderValueReq;
import sfa.order_service.dto.request.UpdateCustomInventoryReq;
import sfa.order_service.dto.response.*;
import sfa.order_service.exception.BusinessServiceException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
@Slf4j
public class ExternalRestService {

    private final RestTemplate restTemplate;
    @Value("${clients.service.url}")
    private String clientServiceUrl;
    @Value("${members.service.url}")
    private String memberServiceUrl;
    @Value("${clients.updateClient.url}")
    private String updateClientUrl;
    @Value("${outlets.getOutlet.url}")
    private String getOutletUrlById;

    @Value("${outlets.getOutForReport.url}")
    private String getOutForReportUrl;
    @Value("${beets.getBeet.url}")
    private String getBeetByIdUrl;
    @Value("${doctor.getDoctor.url}")
    private String getDoctorById;
    @Value("${sampleInventory.getInventory.url}")
    private String getSampleInventoryById;
    @Value("${sampleInventory.deduct.url}")
    private String deductSampleInventoryById;
    @Value("${sampleInventory.customUpdate.url}")
    private String customUpdateSampleUrl;
    @Value("${inventory.update.url}")
    private String inventoryUpdateUrl;
    @Value("${inventory.customUpdate.url}")
    private String customUpdateUrl;

    @Value("${products.getAllProductByIds.url}")
    private String getAllProductByIdsUrl;

    @Value("${combineTourPlan.updateBjpAndCjpOrderValue.url}")
    private String updateBjpAndCjpOrderValueUrl;

    @Value("${combineTourPlan.getLocationBulkResUrl.url}")
    private String getLocationBulkResUrl;

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

    public ClientFMCGResponse getClient(Long clientId) {
        try{
            log.info("While fetching getClient from external rest service");
            log.info("Get client with id: {}", clientId);
            String url = clientServiceUrl + "/" + clientId;
            log.info("URL: {}", url);
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            log.info("Fetch client details with authorization header");
            ResponseEntity<ClientFMCGResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ClientFMCGResponse.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
    public CombineRes getCombineResForBeetAndOutletAndMemberAndClient(Long outletId, Long memberId,  Long clientFmcgId) {
        try {
            String url = "http://localhost:9090/combine-tour-plan/getCombineResForBeetAndOutletAndMemberAndClient" +
                    "?outletId={outletId}&memberId={memberId}&clientFmcgId={clientFmcgId}";

            log.info("URL: {}", url);
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());

            Map<String, Object> uriParams = new HashMap<>();
            uriParams.put("outletId", outletId);
            uriParams.put("memberId", memberId);
            uriParams.put("clientFmcgId", clientFmcgId);

            log.info("Fetching client details with authorization header");

            ResponseEntity<CombineRes> response = restTemplate.exchange(
                    url, HttpMethod.GET, requestEntity, CombineRes.class, uriParams);

            return response.getBody();
        } catch (Exception e) {
            log.error("Error occurred while fetching combine response: {}", e.getMessage(), e);
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public SampleInventoryResponse getSampleInventory(Long memberId, Long productId) {
        try{
            log.info("While fetching getSampleInventory from external rest service");
            String url = getSampleInventoryById + "/" + memberId + "/" + productId;
            log.info("URL: {}", url);
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            log.info("Fetch inventory details with authorization header");
            ResponseEntity<SampleInventoryResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, SampleInventoryResponse.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public void rollBackInventoryForOrder(List<UpdateCustomInventoryReq> updateCustomInventoryReqList) {
        try{
            log.info("RollBackInventory from external rest service");
            String url = customUpdateUrl;
            log.info("URL: {}", url);
            HttpEntity<List<UpdateCustomInventoryReq>> requestEntity = new HttpEntity<>(updateCustomInventoryReqList, createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public List<ProductRes> getAllProductByIds(List<Long> productIds) {
        try{
            log.info("getAllProductByIds from external rest service");
            String url = getAllProductByIdsUrl;
            log.info("URL: {}", url);
            HttpEntity<List<Long>> requestEntity = new HttpEntity<>(productIds, createHeaders());
            return restTemplate.exchange(url, HttpMethod.POST, requestEntity, new ParameterizedTypeReference<List<ProductRes>>() {
            }).getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public LocationBulkRes getLocationBulkRes(List<Long> clientFmcgIds) {
        try{
            String url = getLocationBulkResUrl;
            HttpEntity<List<Long>> requestEntity = new HttpEntity<>(clientFmcgIds, createHeaders());
            return restTemplate.exchange(url, HttpMethod.POST, requestEntity, LocationBulkRes.class).getBody();
        }catch (Exception e){
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }


    public void rollBackInventoryForSample(List<UpdateCustomInventoryReq> updateCustomInventoryReqList) {
        try{
            log.info("RollBackInventory for sample from external rest service");
            String url = customUpdateSampleUrl;
            log.info("URL: {}", url);
            HttpEntity<List<UpdateCustomInventoryReq>> requestEntity = new HttpEntity<>(updateCustomInventoryReqList, createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public void deductSampleInventory(Long id, Integer quantity) {
        try {
            log.info("While fetching deductSampleInventory from external rest service");
            log.info("deductSampleInventory with id: {}", id);
            String url = deductSampleInventoryById + "/" + id + "/" + quantity;
            log.info("URL: {}", url);
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            log.info("Fetch client details with authorization header");
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public DoctorRes getDoctor(Long doctorId) {
        try {
            log.info("While fetching getDoctor from external rest service");
            log.info("Get doctor with id: {}", doctorId);
            String url = getDoctorById + "/" + doctorId;
            log.info("URL: {}", url);
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            log.info("Fetch doctor details with authorization header");
            ResponseEntity<DoctorRes> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, DoctorRes.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
    @Async
    public void updateInventory(Long clientId, Long productId, InventoryUpdateRequest inventoryUpdateRequest) {
        try {
            log.info("While fetching updateInventory from external rest service");
            log.info("Updating inventory with client ID: {}", clientId);
            String url = inventoryUpdateUrl + "/" + productId + "/" + clientId;
            log.info("URL: {}", url);

            HttpEntity<InventoryUpdateRequest> requestEntity = new HttpEntity<>(inventoryUpdateRequest, createHeaders());

            log.info("Sending inventory update request with body");
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, ClientFMCGResponse.class);
        }
        catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }


    public MemberGetDto getMember(Long memberId) {
        try {
            log.info("While fetching getMember from external rest service");
            String url = memberServiceUrl + "/" + memberId;
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            log.info("Fetch member details with authorization header");
            ResponseEntity<MemberGetDto> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, MemberGetDto.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    @Async
    public void updateClientAsync(ClientFMCGUpdateRequest request) {
        try {
            log.info("While fetching updateClientAsync from external rest service");
            String url = updateClientUrl + "/" + request.getId();
            log.info("Async method to update client with authorization header");
            HttpEntity<ClientFMCGUpdateRequest> requestEntity = new HttpEntity<>(request, createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
    @Async
    public String getOutletById(Long outletId) {
        try {
            log.info("While fetching getOutletById from external rest service");
            String url = getOutletUrlById + "/" + outletId;
            log.info("Async method to get outlet with authorization header");
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public OutletRespForOrderDto getOutletByIdWithResp(Long outletId) {
        try {
            log.info("While fetching getOutletByIdWithResp from external rest service");
            String url = getOutForReportUrl + "/" + outletId;
            log.info("Async method to get outlet with authorization header");
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<OutletRespForOrderDto> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, OutletRespForOrderDto.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public String getBeetById(Long beetId) {
        try {
            log.info("While fetching getBeetById from external rest service");
            String url = getBeetByIdUrl + "/" + beetId;
            log.info("Async method to get beets with authorization header");
            HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
            return response.getBody();
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }

    public void updateBjpAndDjpOrderValue(UpdateBjpAndDjpOrderValueReq req) {
        try {
            log.info("While fetching updateBjpAndDjpOrderValue from external rest service");
            String url = updateBjpAndCjpOrderValueUrl;
            log.info("Async method to update client with authorization header");
            HttpEntity<UpdateBjpAndDjpOrderValueReq> requestEntity = new HttpEntity<>(req, createHeaders());
            restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
        }catch (Exception e){
            log.info("Error occurred: " + e.getMessage());
            throw new BusinessServiceException(ApiErrorCodes.CLIENT_NOT_FOUND.getErrorCode(), e.getMessage());
        }
    }
}
