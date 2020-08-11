package org.arunm;

import org.arunm.model.Address;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class AddressLookupApplication {

    @GetMapping("/address")
    public Address greeting(@RequestParam(value = "customerId") String customerId) {
        return createAddress();
    }

    public static void main(String[] args) {
        SpringApplication.run(AddressLookupApplication.class, args);
    }
    private Address createAddress() {
        Address address = new Address();
        address.setAddressLine1("fancy place");
        address.setAddressLine2("belonngs to 1%");
        address.setState("UTOIPA");
        address.setSuburb("Priv");
        return address;
    }
}
