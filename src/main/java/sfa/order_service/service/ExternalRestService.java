package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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

    public ClientResponse getClient(Long clientId) {
        String url = clientServiceUrl + "/" + clientId;
        return restTemplate.getForObject(url, ClientResponse.class);
    }

    public MemberResponse getMember(Long memberId) {
        String url = memberServiceUrl + "/" + memberId;
        return restTemplate.getForObject(url, MemberResponse.class);
    }

    @Async
    public void updateClientAsync( ClientUpdateRequest request) {
        String url = updateClientUrl + "/"+request.getId();
       restTemplate.put(url, request);
    }


}
