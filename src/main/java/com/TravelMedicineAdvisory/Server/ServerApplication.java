package com.TravelMedicineAdvisory.Server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class ServerApplication {
    public static void main(final String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @GetMapping("/")
    public String ApiPage() {
        return "Welcome to the Travel Medicine Advisory API!";
    }
}
