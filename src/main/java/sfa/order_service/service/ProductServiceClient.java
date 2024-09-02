package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfa.order_service.dto.response.ProductRes;


@Service
@RequiredArgsConstructor
public class ProductServiceClient {
    private final RestTemplate restTemplate;
    @Value("${products.service.url}")
    private String productServiceUrl;
    public ProductRes getProductById(Long productId) {
        String url = productServiceUrl + "/products/" + productId;
        return restTemplate.getForObject(url, ProductRes.class);
    }
}
