package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfa.order_service.Configs.TokenContext;
import sfa.order_service.dto.request.ClientUpdateRequest;
import sfa.order_service.dto.response.ClientResponse;
import sfa.order_service.dto.response.MemberResponse;

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

    public ClientResponse getClient(Long clientId) {
        log.info("Get client with id: {}", clientId);
        String url = clientServiceUrl + "/" + clientId;
        log.info("URL: {}", url);
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
        log.info("Fetch client details with authorization header");
        ResponseEntity<ClientResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ClientResponse.class);
        return response.getBody();
    }

    public MemberResponse getMember(Long memberId) {
        String url = memberServiceUrl + "/" + memberId;
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
        log.info("Fetch member details with authorization header");
        ResponseEntity<MemberResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, MemberResponse.class);
        return response.getBody();
    }

    @Async
    public void updateClientAsync(ClientUpdateRequest request) {
        String url = updateClientUrl + "/" + request.getId();
        log.info("Async method to update client with authorization header");
        HttpEntity<ClientUpdateRequest> requestEntity = new HttpEntity<>(request, createHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
    }
}
