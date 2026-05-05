package com.TravelMedicineAdvisory.Server.middlewares;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

/**
 * MVC configuration. CORS is handled exclusively by Spring Security's
 * CorsConfigurationSource in SecurityConfig — do not add addCorsMappings()
 * here, as it conflicts with the Security-level CORS filter and causes
 * null-status preflight failures in the browser.
 */
@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    @Value("${app.storage.path:storage/upload}")
    private String storagePath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/storage/upload/**")
                .addResourceLocations("file:" + storagePath + "/");
        registry.addResourceHandler("/storage/uploads/**")
                .addResourceLocations("file:" + storagePath + "/");
    }
}
