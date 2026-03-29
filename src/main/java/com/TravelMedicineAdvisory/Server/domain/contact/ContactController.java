package com.TravelMedicineAdvisory.Server.domain.contact;

import com.TravelMedicineAdvisory.Server.core.types.SuccessResponse;
import com.TravelMedicineAdvisory.Server.domain.contact.ContactDto.ContactRequest;
import com.TravelMedicineAdvisory.Server.domain.contact.ContactDto.ContactResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/contact")
public class ContactController {

    private final ContactService service;

    public ContactController(ContactService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SuccessResponse> submit(
            @RequestBody ContactRequest request,
            HttpServletRequest httpRequest) {

        String forwarded = httpRequest.getHeader("X-Forwarded-For");
        String ipAddress = (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : httpRequest.getRemoteAddr();
        String userAgent = httpRequest.getHeader("User-Agent");

        ContactResponse response = service.submit(request, ipAddress, userAgent);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new SuccessResponse("Message received", response));
    }
}
