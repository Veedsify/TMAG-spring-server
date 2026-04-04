package com.TravelMedicineAdvisory.Server.domain.admin.system;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.TravelMedicineAdvisory.Server.domain.systemlog.SystemLog;
import com.TravelMedicineAdvisory.Server.domain.systemlog.SystemLogRepository;
import com.TravelMedicineAdvisory.Server.domain.systemsetting.SystemSetting;
import com.TravelMedicineAdvisory.Server.domain.systemsetting.SystemSettingRepository;

@Service
public class AdminSystemService {

    private final SystemLogRepository systemLogRepository;
    private final SystemSettingRepository systemSettingRepository;

    public AdminSystemService(SystemLogRepository systemLogRepository,
            SystemSettingRepository systemSettingRepository) {
        this.systemLogRepository = systemLogRepository;
        this.systemSettingRepository = systemSettingRepository;
    }

    public Map<String, Object> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();

        // Overall status
        status.put("status", "healthy");
        status.put("uptime", "99.9%");
        status.put("lastChecked", java.time.LocalDateTime.now().toString());

        // Services array matching frontend SystemStatus.services: {name, status, latency}[]
        List<Map<String, Object>> services = new java.util.ArrayList<>();

        Map<String, Object> dbService = new HashMap<>();
        dbService.put("name", "Database");
        dbService.put("status", "healthy");
        dbService.put("latency", "12ms");
        services.add(dbService);

        Map<String, Object> aiService = new HashMap<>();
        aiService.put("name", "AI Engine");
        aiService.put("status", "healthy");
        aiService.put("latency", "245ms");
        services.add(aiService);

        Map<String, Object> paymentService = new HashMap<>();
        paymentService.put("name", "Payment Gateway");
        paymentService.put("status", "healthy");
        paymentService.put("latency", "89ms");
        services.add(paymentService);

        Map<String, Object> emailService = new HashMap<>();
        emailService.put("name", "Email Service");
        emailService.put("status", "healthy");
        emailService.put("latency", "34ms");
        services.add(emailService);

        status.put("services", services);

        return status;
    }

    public List<AdminSystemLogResponse> getSystemLogs(String level, Integer limit) {
        List<SystemLog> logs = systemLogRepository.findAll();

        if (level != null) {
            logs = logs.stream()
                    .filter(l -> level.equalsIgnoreCase(l.getLevel()))
                    .toList();
        }

        logs = logs.stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .toList();

        if (limit != null && limit > 0) {
            logs = logs.stream().limit(limit).toList();
        }

        return logs.stream().map(this::mapToResponse).toList();
    }

    public AdminSystemSettingsResponse getSettings() {
        List<SystemSetting> settings = systemSettingRepository.findAll();

        Map<String, Object> settingsMap = new HashMap<>();
        for (SystemSetting setting : settings) {
            settingsMap.put(setting.getKey(), parseValue(setting));
        }

        return AdminSystemSettingsResponse.fromMap(settingsMap);
    }

    @Transactional
    public AdminSystemSettingsResponse updateSettings(Map<String, Object> updates) {
        for (Map.Entry<String, Object> entry : updates.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            SystemSetting setting = systemSettingRepository.findByKey(key)
                    .orElse(null);

            if (setting == null) {
                setting = new SystemSetting();
                setting.setKey(key);
            }

            if (value instanceof String) {
                setting.setValue((String) value);
            } else {
                setting.setValue(String.valueOf(value));
            }

            systemSettingRepository.save(setting);
        }

        return getSettings();
    }

    @Transactional
    public void toggleMaintenance() {
        SystemSetting maintenanceSetting = systemSettingRepository.findByKey("maintenanceMode")
                .orElse(null);

        if (maintenanceSetting == null) {
            maintenanceSetting = new SystemSetting();
            maintenanceSetting.setKey("maintenanceMode");
            maintenanceSetting.setValue("false");
            maintenanceSetting.setType("boolean");
            maintenanceSetting.setGroup("system");
        }

        boolean current = "true".equalsIgnoreCase(maintenanceSetting.getValue());
        maintenanceSetting.setValue(String.valueOf(!current));

        systemSettingRepository.save(maintenanceSetting);
    }

    private Object parseValue(SystemSetting setting) {
        if (setting.getValue() == null) {
            return null;
        }

        String type = setting.getType();
        String value = setting.getValue();

        if ("boolean".equalsIgnoreCase(type)) {
            return "true".equalsIgnoreCase(value);
        } else if ("decimal".equalsIgnoreCase(type) || "float".equalsIgnoreCase(type) || "double".equalsIgnoreCase(type)) {
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return value;
            }
        } else if ("number".equalsIgnoreCase(type) || "integer".equalsIgnoreCase(type)) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return value;
            }
        }

        return value;
    }

    private AdminSystemLogResponse mapToResponse(SystemLog log) {
        return new AdminSystemLogResponse(
                log.getId(),
                log.getLevel(),
                log.getMessage(),
                log.getSource(),
                log.getCreatedAt(),
                log.getDetails()
        );
    }
}
