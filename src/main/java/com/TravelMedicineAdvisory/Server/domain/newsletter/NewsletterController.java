package com.TravelMedicineAdvisory.Server.domain.newsletter;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.newsletter.NewsletterDto.NewsletterRequest;
import com.TravelMedicineAdvisory.Server.domain.newsletter.NewsletterDto.NewsletterResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/newsletter")
public class NewsletterController {

    private final NewsletterService service;

    public NewsletterController(NewsletterService service) {
        this.service = service;
    }

    @PostMapping("/subscribe")
    public ResponseEntity<SuccessResponse> subscribe(@RequestBody NewsletterRequest request) {
        try {
            NewsletterResponse response = service.subscribe(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SuccessResponse("Subscribed successfully", response));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new SuccessResponse(e.getMessage(), null));
        }
    }
}
