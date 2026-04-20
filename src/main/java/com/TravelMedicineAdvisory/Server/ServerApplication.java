package com.TravelMedicineAdvisory.Server;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Root")
@RestController
@SpringBootApplication
@ConfigurationPropertiesScan
public class ServerApplication {
    public static void main(final String[] args) {
        SpringApplication.run(ServerApplication.class, args);
    }

    @Operation(summary = "API welcome")
    @GetMapping("/")
    public String ApiPage() {
        return "<h3> Welcome to the Travel Medicine Global Advisory API! </h3> <p> This API provides comprehensive travel medicine advice, including health recommendations, vaccination requirements, and safety tips for travelers worldwide. </p>"
                +
                "<p>Endpoints:</p>" +
                "<ul>" +
                "<li>/countries - Get a list of all countries with travel medicine advice.</li>" +
                "<li>/countries/{country} - Get detailed travel medicine advice for a specific country.</li>" +
                "<li>/vaccinations - Get information on recommended vaccinations for travelers.</li>" +
                "<li>/health-tips - Get general health and safety tips for travelers.</li>" +
                "</ul>" +
                "<p>Please refer to the API documentation for more details on how to use each endpoint.";
    }
}
