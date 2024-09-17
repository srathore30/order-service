package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class ProductServiceClient {
    private final RestTemplate restTemplate;
    @Value("${products.service.url}")
    private String productServiceUrl;

    public Double getProductPrice(Long productId, String priceType) {
        String url = productServiceUrl + "/products/getByIdAndPriceType/" + productId + "?priceType=" + priceType;
        return restTemplate.getForObject(url, Double.class);
    }
}
