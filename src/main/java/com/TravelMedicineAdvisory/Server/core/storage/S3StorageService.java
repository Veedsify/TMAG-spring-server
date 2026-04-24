package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.UUID;

public class S3StorageService implements StorageService {

    private final String bucket;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String endpoint;

    public S3StorageService(String bucket, String region, String accessKey, String secretKey, String endpoint) {
        this.bucket = bucket;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;
    }

    private S3Client buildClient() {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)));
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    @Override
    public Attachment store(MultipartFile file, String path, Long modelId, String modelName) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String key = path + "/" + filename;
            try (S3Client client = buildClient()) {
                client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket)
                                .key(key)
                                .contentType(file.getContentType())
                                .build(),
                        RequestBody.fromBytes(file.getBytes()));
            }
            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setStoragePath(key);
            attachment.setModelId(modelId);
            attachment.setModelName(modelName);
            return attachment;
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file to S3", e);
        }
    }

    @Override
    public String storeBytes(byte[] content, String path, String filename, String contentType) {
        String key = path + "/" + filename;
        try (S3Client client = buildClient()) {
            client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content));
        }
        return key;
    }

    @Override
    public byte[] readBytes(String path) {
        try (S3Client client = buildClient()) {
            return client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(path)
                            .build()).asByteArray();
        }
    }

    @Override
    public void delete(String path) {
        try (S3Client client = buildClient()) {
            client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket)
                            .key(path)
                            .build());
        }
    }

    @Override
    public String getUrl(String path) {
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint.replaceAll("/$", "") + "/" + bucket + "/" + path;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + path;
    }
}
