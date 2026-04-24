package com.TravelMedicineAdvisory.Server.core.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class DoctorWebSocketService {

    private static final Logger log = LoggerFactory.getLogger(DoctorWebSocketService.class);

    private final AppWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public DoctorWebSocketService(AppWebSocketHandler webSocketHandler, ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    public void broadcastNewPlanPending(Long planId, String destination) {
        try {
            String json = objectMapper.writeValueAsString(Map.of(
                    "type", "NEW_PLAN_PENDING",
                    "planId", planId,
                    "destination", destination,
                    "timestamp", java.time.Instant.now().toString()));
            webSocketHandler.broadcast(json);
            log.info("WebSocket broadcast: new plan pending review planId={}", planId);
        } catch (Exception e) {
            log.error("Failed to broadcast WebSocket message for plan {}", planId, e);
        }
    }
}
