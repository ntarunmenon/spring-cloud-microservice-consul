package org.arunm;

import org.arunm.model.Address;
import org.arunm.model.OrderRequest;
import org.arunm.model.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Random;

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
    public RestTemplate loadbalancedRestTemplate() {
        return new RestTemplate();
    }

    @PostMapping("/order")
    public OrderResponse createOrder(@RequestBody  OrderRequest orderRequest) {
        URI uri = UriComponentsBuilder.
                fromUri(serviceUrl())
                    .path("/api/address")
                    .queryParam("customerId",orderRequest.getCustomerId())
                .build().toUri();
        Address address = restTemplate
                .getForEntity(uri,
                        Address.class)
                .getBody();
        Assert.notNull(address, "Address cannot be null");
        System.out.println("Address from service is " + address);
        serviceUrl();
        orderRequest.setAddress(address);
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
}
