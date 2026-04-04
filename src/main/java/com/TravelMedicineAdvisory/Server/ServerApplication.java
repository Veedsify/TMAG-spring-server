package com.TravelMedicineAdvisory.Server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Root")
@RestController
@SpringBootApplication
public class ServerApplication {
    public static void main(final String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Operation(summary = "API welcome")
    @GetMapping("/")
    public String ApiPage() {
        return "Welcome to the Travel Medicine Global Advisory API!";
    }
}
