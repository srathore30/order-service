package sfa.order_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import sfa.order_service.Configs.TokenContext;
import sfa.order_service.dto.response.ProductRes;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceClient {
    private final RestTemplate restTemplate;
    @Value("${products.service.url}")
    private String productServiceUrl;
    @Value("${product.getProduct.url}")
    private String productUrl;

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
        String url = productServiceUrl + "/products/getByIdAndPriceType/" + productId + "?priceType=" + priceType;
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
        log.info("Fetch product details with authorization header");
        ResponseEntity<Double> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, Double.class);
        return response.getBody();
    }

    public ProductRes getProduct(Long productId) {
        String url = productUrl + productId;
        HttpEntity<Void> requestEntity = new HttpEntity<>(createHeaders());
        log.info("Fetch product details with authorization header");
        ResponseEntity<ProductRes> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, ProductRes.class);
        return response.getBody();
    }

}
