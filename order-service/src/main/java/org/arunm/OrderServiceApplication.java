package org.arunm;

import java.net.URI;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import org.arunm.model.Address;
import org.arunm.model.OrderRequest;
import org.arunm.model.OrderResponse;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@SpringBootApplication
@RestController
@Configuration
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class,args);
    }

    private DiscoveryClient discoveryClient;

    private RestTemplate restTemplate;

    public OrderServiceApplication(DiscoveryClient discoveryClient, RestTemplate restTemplate) {
        this.discoveryClient = discoveryClient;
        this.restTemplate = restTemplate;
    }

    @Bean
    public RestTemplate loadbalancedRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @PostMapping("/api/order")
    public OrderResponse createOrder(@RequestBody  OrderRequest orderRequest) {
        URI uri = UriComponentsBuilder.
                fromUri(serviceUrl())
                    .path("/api/address")
                    .queryParam("customerId",orderRequest.getCustomerId())
                .build().toUri();

        HttpEntity<String> entity = new HttpEntity<>(null , addAuthHeaderFromKeycloak());

        ResponseEntity<Address> addressResponseEntity = restTemplate.exchange(uri, HttpMethod.GET,entity, Address.class);
        Assert.notNull(addressResponseEntity.getBody(), "Address cannot be null");
        orderRequest.setAddress(addressResponseEntity.getBody());
        return createOrderResponse();
    }

    private OrderResponse createOrderResponse() {
        OrderResponse response = new OrderResponse();
        Random random = new Random();

        String generatedString = random.ints(97, 122 + 1)
                .limit(10)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        response.setReceiptNo(generatedString);
        return response;
    }

    public URI serviceUrl() {
        List<ServiceInstance> list = discoveryClient.getInstances("Address-Lookup-Service");
        if (list != null && list.size() > 0 ) {
            System.out.println("url is" + list.get(0).getUri().toString());
            return list.get(0).getUri();
        }
        return null;
    }

    private HttpHeaders addAuthHeaderFromKeycloak() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " +
                getAccessToken());
        return headers;
    }

    private String getAccessToken() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return Stream.of(authentication)
                .map(Authentication::getDetails)
                .map(OidcKeycloakAccount.class::cast)
                .map(OidcKeycloakAccount::getKeycloakSecurityContext)
                .map(KeycloakSecurityContext::getTokenString)
                .findFirst()
                .orElseThrow(IllegalStateException::new);
    }
}
