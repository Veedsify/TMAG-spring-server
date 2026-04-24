package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;

@Configuration
public class StorageConfig {

    @Value("${app.storage.provider:local}")
    private String provider;

    @Value("${app.storage.s3.bucket:}")
    private String s3Bucket;

    @Value("${app.storage.s3.region:us-east-1}")
    private String s3Region;

    @Value("${app.storage.s3.access-key:}")
    private String s3AccessKey;

    @Value("${app.storage.s3.secret-key:}")
    private String s3SecretKey;

    @Value("${app.storage.s3.endpoint:}")
    private String s3Endpoint;

    @Bean
    public StorageService storageService(LocalStorageService localStorageService,
                                          @Lazy GcsStorageService gcsStorageService) {
        return switch (provider.toLowerCase()) {
            case "gcs" -> gcsStorageService;
            case "s3" -> new S3StorageService(s3Bucket, s3Region, s3AccessKey, s3SecretKey, s3Endpoint);
            default -> localStorageService;
        };
    }
}
