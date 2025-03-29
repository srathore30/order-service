package sfa.order_service.Configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Order Service Swagger APIs")
                        .description("API documentation for Order Service")
                        .version("v1.0"))
                .servers(List.of(
                        new Server().url("http://localhost:9092/order-service").description("Local Server"),
                        new Server().url("https://staging.prism-sfa-dev.net/order-service").description("Dev Server With https"),
                        new Server().url("http://staging.prism-sfa-dev.net/order-service").description("Dev Server With http")
                ));
    }
}
