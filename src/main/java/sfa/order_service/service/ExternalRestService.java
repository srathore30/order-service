package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
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
public class ExternalRestService {

    private final RestTemplate restTemplate;
    @Value("${clients.service.url}")
    private String clientServiceUrl;
    @Value("${members.service.url}")
    private String memberServiceUrl;
    @Value("${clients.updateClient.url}")
    private String updateClientUrl;

    // Helper method to create HTTP headers with the token
    private HttpHeaders createHeaders() {
        String token = TokenContext.getToken();  // Get token from the context
        HttpHeaders headers = new HttpHeaders();
        if (token != null) {
            headers.set("Authorization", "Bearer " + token);  // Add token to headers
        }
        return headers;
    }

    // Fetch client details with authorization header
    public ClientResponse getClient(Long clientId) {
        String url = clientServiceUrl + "/" + clientId;
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
        ResponseEntity<ClientResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ClientResponse.class);
        return response.getBody();
    }

    // Fetch member details with authorization header
    public MemberResponse getMember(Long memberId) {
        String url = memberServiceUrl + "/" + memberId;
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
        ResponseEntity<MemberResponse> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, MemberResponse.class);
        return response.getBody();
    }

    // Async method to update client with authorization header
    @Async
    public void updateClientAsync(ClientUpdateRequest request) {
        String url = updateClientUrl + "/" + request.getId();
        HttpEntity<ClientUpdateRequest> requestEntity = new HttpEntity<>(request, createHeaders());
        restTemplate.exchange(url, HttpMethod.PUT, requestEntity, Void.class);
    }
}
