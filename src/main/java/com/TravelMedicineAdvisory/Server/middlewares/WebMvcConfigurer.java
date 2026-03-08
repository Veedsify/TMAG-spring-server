package com.TravelMedicineAdvisory.Server.middlewares;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

@Configuration
public class WebMvcConfigurer implements org.springframework.web.servlet.config.annotation.WebMvcConfigurer {

    @Value("${app.cors.allowed-origins:http://localhost:3000,http://localhost:3001}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS,PATCH,HEAD}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Origin,Content-Type,Accept,Authorization,X-Api-Key,Base-Orgid}")
    private String allowedHeaders;

    @Value("${app.cors.expose-headers:Content-Length,Content-Type}")
    private String exposeHeaders;

    @Value("${app.cors.allow-credentials:true}")
    private boolean allowCredentials;

    @Value("${app.cors.max-age:43200}")
    private long maxAge;

    @Value("${app.storage.path:storage/upload}")
    private String storagePath;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins.split(","))
                .allowedMethods(allowedMethods.split(","))
                .allowedHeaders(allowedHeaders.split(","))
                .exposedHeaders(exposeHeaders.split(","))
                .allowCredentials(allowCredentials)
                .maxAge(maxAge);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/storage/**")
                .addResourceLocations("file:" + storagePath + "/");
    }
}
