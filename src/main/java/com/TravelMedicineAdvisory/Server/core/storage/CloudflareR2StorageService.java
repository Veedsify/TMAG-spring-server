package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.net.URI;
import java.util.UUID;

/**
 * Storage service backed by Cloudflare R2.
 *
 * <p>R2 is S3-compatible, so this service uses the AWS SDK v2 with a custom
 * endpoint override pointing at the R2 account endpoint
 * ({@code https://<ACCOUNT_ID>.r2.cloudflarestorage.com}).
 *
 * <p>Public access URLs are resolved in one of two ways:
 * <ol>
 *   <li>If {@code app.storage.r2.public-url} is set (e.g. a custom domain or
 *       R2 public-bucket URL), that value is used as the base.</li>
 *   <li>Otherwise the URL is constructed from the R2 endpoint + bucket name,
 *       which only works when the bucket has public access enabled.</li>
 * </ol>
 */
public class CloudflareR2StorageService implements StorageService {

    private final String bucket;
    private final String accountId;
    private final String accessKey;
    private final String secretKey;
    private final String publicUrl;

    /**
     * @param bucket     R2 bucket name
     * @param accountId  Cloudflare account ID (used to build the endpoint URL)
     * @param accessKey  R2 API token – Access Key ID
     * @param secretKey  R2 API token – Secret Access Key
     * @param publicUrl  Optional public base URL (custom domain or R2 public URL).
     *                   Leave blank to derive from the endpoint.
     */
    public CloudflareR2StorageService(
            String bucket,
            String accountId,
            String accessKey,
            String secretKey,
            String publicUrl) {
        this.bucket = bucket;
        this.accountId = accountId;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.publicUrl = publicUrl;
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String r2Endpoint() {
        return "https://" + accountId + ".r2.cloudflarestorage.com";
    }

    private S3Client buildClient() {
        return S3Client.builder()
                // R2 uses auto region; "auto" is the Cloudflare-recommended value
                .region(Region.of("auto"))
                .endpointOverride(URI.create(r2Endpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    // -------------------------------------------------------------------------
    // StorageService implementation
    // -------------------------------------------------------------------------

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
            throw new RuntimeException("Failed to store file to Cloudflare R2", e);
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
        if (publicUrl != null && !publicUrl.isBlank()) {
            return publicUrl.replaceAll("/+$", "") + "/" + path;
        }
        // Fall back to: https://<accountId>.r2.cloudflarestorage.com/<bucket>/<path>
        return r2Endpoint() + "/" + bucket + "/" + path;
    }
}
