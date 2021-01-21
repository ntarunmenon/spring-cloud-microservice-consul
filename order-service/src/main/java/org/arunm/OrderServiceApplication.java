package org.arunm;

import java.net.URI;
import java.util.List;
import java.util.Random;

import org.arunm.model.Address;
import org.arunm.model.OrderRequest;
import org.arunm.model.OrderResponse;
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


    @Autowired
    private DiscoveryClient discoveryClient;

    @Autowired
    private RestTemplate restTemplate;

    @Bean
    public RestTemplate loadbalancedRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder.build();
    }

    @PostMapping("/order")
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
                ("eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJoS3NPWmhyQTFLaXNOaVNsVlZLNzNrek1DdE45Q25Ya3JCcTdSN3RYMWRBIn0.eyJleHAiOjE2MTEyMDIxODYsImlhdCI6MTYxMTE5ODU4NiwianRpIjoiYmQ4OTA3MzktYjRlZS00N2VjLWJlNDgtZmQzNjM1NzJlNmQ4IiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwL2F1dGgvcmVhbG1zL21hc3RlciIsImF1ZCI6ImFjY291bnQiLCJzdWIiOiIxYmI5ZWM0Mi04MmY2LTQ4MzUtYjM0OS1lYjY1MTRkYmEwNDEiLCJ0eXAiOiJCZWFyZXIiLCJhenAiOiJhZGRyZXNzLWxvb2t1cC1zZXJ2aWNlIiwic2Vzc2lvbl9zdGF0ZSI6IjAzYWFkNzgyLWM0ZTgtNDFkZS1hOTBmLTJiYTdhZDQ1YTI0NSIsImFjciI6IjEiLCJyZWFsbV9hY2Nlc3MiOnsicm9sZXMiOlsib2ZmbGluZV9hY2Nlc3MiLCJ1bWFfYXV0aG9yaXphdGlvbiJdfSwicmVzb3VyY2VfYWNjZXNzIjp7ImFjY291bnQiOnsicm9sZXMiOlsibWFuYWdlLWFjY291bnQiLCJtYW5hZ2UtYWNjb3VudC1saW5rcyIsInZpZXctcHJvZmlsZSJdfX0sInNjb3BlIjoicHJvZmlsZSBlbWFpbCIsImNsaWVudEhvc3QiOiIxNzIuMjAuMC4xIiwiY2xpZW50SWQiOiJhZGRyZXNzLWxvb2t1cC1zZXJ2aWNlIiwiZW1haWxfdmVyaWZpZWQiOmZhbHNlLCJwcmVmZXJyZWRfdXNlcm5hbWUiOiJzZXJ2aWNlLWFjY291bnQtYWRkcmVzcy1sb29rdXAtc2VydmljZSIsImNsaWVudEFkZHJlc3MiOiIxNzIuMjAuMC4xIn0.Rz7EbdNDiBaC1Zbo8UH61beB3tDWa2YADb9XgJ34vQxcEZ698rBpbCTIQHjoekHE7ZjVHoKLYiIO7TUyAq1B8yo64Jhr7Eq8shN_ZcmhvFscQAFLSGCr3qFzBfXQNGL4H58-_1ONyCqwbyq_nrb2UzvmddUVHmTFo1tIIUCJWeLBJ9JOEftPRUGDMsftzJdTgASD48LieFlHLs-s_Q4PfMOrtO4Jvd7tnJqRrrRKN8fVq2XOep9vObMOtYUj8sVUagVModVUfG6mN2LrkeSh44Wu6WmFM1VTAWpEFHjzvpsCkUCxXFLRwR548G8rxQUOncXm45KxwKMW6WeZWmAU6A"));
        return headers;
    }
}
