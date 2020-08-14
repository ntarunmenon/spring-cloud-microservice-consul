package org.arunm;

import org.arunm.model.Address;
import org.arunm.model.OrderRequest;
import org.arunm.model.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Random;

@SpringBootApplication
@RestController
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class,args);
    }


    private RestTemplate restTemplate;

    OrderServiceApplication(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder
                .build();
    }

    @PostMapping("/order")
    public OrderResponse createOrder(@RequestBody  OrderRequest orderRequest) {
        Address address = restTemplate
                .getForEntity("http://address-lookup-service:7090/address?customerId={customerId}",
                        Address.class,
                        orderRequest.getCustomerId())
                .getBody();
        Assert.notNull(address, "Address cannot be null");
        System.out.println("Address from service is " + address);
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
}
