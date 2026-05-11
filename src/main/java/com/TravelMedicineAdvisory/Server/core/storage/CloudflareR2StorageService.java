package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * StorageService backed by Cloudflare R2.
 *
 * <p>
 * R2 is S3-compatible, so this service uses the AWS SDK v2 with a custom
 * endpoint override pointing at the R2 account endpoint
 * ({@code https://<ACCOUNT_ID>.r2.cloudflarestorage.com}).
 *
 * <p>
 * Files at or above {@code multipartThresholdBytes} (default 100 MB) are
 * uploaded using the S3 multipart upload API, streaming each part from the
 * temp file that Spring wrote to disk. This avoids heap exhaustion and
 * connection timeouts for large files.
 *
 * <p>
 * Public access URLs are resolved in one of two ways:
 * <ol>
 * <li>If {@code app.storage.r2.public-url} is set (e.g. a custom domain or
 * R2 public-bucket URL), that value is used as the base.</li>
 * <li>Otherwise the URL is constructed from the R2 endpoint + bucket name,
 * which only works when the bucket has public access enabled.</li>
 * </ol>
 */
public class CloudflareR2StorageService implements StorageService {

    /** Minimum part size enforced by R2 / S3 (5 MiB). */
    private static final long MIN_PART_SIZE = 5L * 1024L * 1024L;

    private final String bucket;
    private final String accountId;
    private final String accessKey;
    private final String secretKey;
    private final String publicUrl;
    private final long multipartThresholdBytes;
    private final long partSizeBytes;

    /**
     * @param bucket                  R2 bucket name
     * @param accountId               Cloudflare account ID (builds the endpoint
     *                                URL)
     * @param accessKey               R2 API token – Access Key ID
     * @param secretKey               R2 API token – Secret Access Key
     * @param publicUrl               Optional public base URL (custom domain or R2
     *                                public URL)
     * @param multipartThresholdBytes File size (bytes) above which multipart upload
     *                                is used
     * @param partSizeBytes           Size of each individual part (must be ≥ 5 MiB)
     */
    public CloudflareR2StorageService(
            String bucket,
            String accountId,
            String accessKey,
            String secretKey,
            String publicUrl,
            long multipartThresholdBytes,
            long partSizeBytes) {
        this.bucket = bucket;
        this.accountId = accountId;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.publicUrl = publicUrl;
        this.multipartThresholdBytes = multipartThresholdBytes;
        this.partSizeBytes = Math.max(partSizeBytes, MIN_PART_SIZE);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String r2Endpoint() {
        return "https://" + accountId + ".eu.r2.cloudflarestorage.com";
    }

    private S3Client buildClient() {
        validateConfiguration();
        return S3Client.builder()
                // R2 uses "auto" as the recommended region value
                .region(Region.of("auto"))
                .endpointOverride(URI.create(r2Endpoint()))
                // R2 is S3-compatible, but path-style addressing is the most reliable
                // form for the account endpoint: /<bucket>/<key>.
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(true)
                        .build())
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    private void validateConfiguration() {
        List<String> missing = new ArrayList<>();
        if (isBlank(bucket))
            missing.add("APP_STORAGE_R2_BUCKET");
        if (isBlank(accountId))
            missing.add("APP_STORAGE_R2_ACCOUNT_ID");
        if (isBlank(accessKey))
            missing.add("APP_STORAGE_R2_ACCESS_KEY");
        if (isBlank(secretKey))
            missing.add("APP_STORAGE_R2_SECRET_KEY");
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Cloudflare R2 storage is selected but these settings are missing: "
                    + String.join(", ", missing));
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private RuntimeException r2Exception(String operation, String key, S3Exception e) {
        String message = "Cloudflare R2 " + operation + " failed for bucket '" + bucket + "' and key '" + key
                + "' with status " + e.statusCode() + " (" + e.awsErrorDetails().errorMessage() + "). "
                + "If this is 403 Access Denied, verify APP_STORAGE_R2_ACCOUNT_ID matches the bucket account, "
                + "APP_STORAGE_R2_BUCKET is the exact bucket name, and the R2 API token has Object Read & Write permissions for this bucket.";
        return new RuntimeException(message, e);
    }

    // -------------------------------------------------------------------------
    // StorageService implementation
    // -------------------------------------------------------------------------

    @Override
    public Attachment store(MultipartFile file, String path, Long modelId, String modelName) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String key = path + "/" + filename;

            if (file.getSize() >= multipartThresholdBytes) {
                doMultipartUpload(file.getInputStream(), file.getSize(), key, file.getContentType());
            } else {
                try (S3Client client = buildClient()) {
                    client.putObject(
                            PutObjectRequest.builder()
                                    .bucket(bucket).key(key)
                                    .contentType(file.getContentType())
                                    .build(),
                            RequestBody.fromBytes(file.getBytes()));
                }
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
                            .bucket(bucket).key(key)
                            .contentType(contentType)
                            .build(),
                    RequestBody.fromBytes(content));
        } catch (S3Exception e) {
            throw r2Exception("putObject", key, e);
        }
        return key;
    }

    @Override
    public String storeStream(InputStream inputStream, long contentLength, String path,
            String filename, String contentType) {
        String key = path + "/" + filename;
        if (contentLength >= multipartThresholdBytes || contentLength < 0) {
            doMultipartUpload(inputStream, contentLength, key, contentType);
        } else {
            try (S3Client client = buildClient()) {
                client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucket).key(key)
                                .contentType(contentType)
                                .contentLength(contentLength)
                                .build(),
                        RequestBody.fromInputStream(inputStream, contentLength));
            } catch (S3Exception e) {
                throw r2Exception("putObject", key, e);
            }
        }
        return key;
    }

    @Override
    public byte[] readBytes(String path) {
        try (S3Client client = buildClient()) {
            return client.getObjectAsBytes(
                    GetObjectRequest.builder()
                            .bucket(bucket).key(path)
                            .build())
                    .asByteArray();
        }
    }

    @Override
    public void delete(String path) {
        try (S3Client client = buildClient()) {
            client.deleteObject(
                    DeleteObjectRequest.builder()
                            .bucket(bucket).key(path)
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

    // -------------------------------------------------------------------------
    // Multipart upload
    // -------------------------------------------------------------------------

    /**
     * Executes a multipart upload, reading {@code inputStream} in
     * {@code partSizeBytes} chunks. The upload is aborted automatically if
     * any part fails.
     *
     * @param inputStream   the source data stream
     * @param contentLength total byte count, or {@code -1} if unknown
     * @param key           object key in the bucket
     * @param contentType   MIME type of the object
     */
    private void doMultipartUpload(InputStream inputStream, long contentLength,
            String key, String contentType) {
        try (S3Client client = buildClient()) {
            CreateMultipartUploadResponse init = client.createMultipartUpload(
                    CreateMultipartUploadRequest.builder()
                            .bucket(bucket).key(key)
                            .contentType(contentType)
                            .build());
            String uploadId = init.uploadId();

            List<CompletedPart> completedParts = new ArrayList<>();
            byte[] buffer = new byte[(int) partSizeBytes];
            int partNumber = 1;

            try {
                int offset = 0;
                while (true) {
                    int remaining = buffer.length - offset;
                    int bytesRead = inputStream.read(buffer, offset, remaining);
                    boolean endOfStream = bytesRead == -1;
                    if (!endOfStream)
                        offset += bytesRead;

                    boolean bufferFull = offset == buffer.length;
                    if ((bufferFull || endOfStream) && offset > 0) {
                        byte[] partData = offset == buffer.length ? buffer : Arrays.copyOf(buffer, offset);
                        UploadPartResponse partResp = client.uploadPart(
                                UploadPartRequest.builder()
                                        .bucket(bucket).key(key)
                                        .uploadId(uploadId)
                                        .partNumber(partNumber)
                                        .contentLength((long) partData.length)
                                        .build(),
                                RequestBody.fromBytes(partData));
                        completedParts.add(CompletedPart.builder()
                                .partNumber(partNumber)
                                .eTag(partResp.eTag())
                                .build());
                        partNumber++;
                        offset = 0;
                    }

                    if (endOfStream)
                        break;
                }

                client.completeMultipartUpload(
                        CompleteMultipartUploadRequest.builder()
                                .bucket(bucket).key(key)
                                .uploadId(uploadId)
                                .multipartUpload(CompletedMultipartUpload.builder()
                                        .parts(completedParts)
                                        .build())
                                .build());
            } catch (Exception e) {
                try {
                    client.abortMultipartUpload(
                            AbortMultipartUploadRequest.builder()
                                    .bucket(bucket).key(key)
                                    .uploadId(uploadId)
                                    .build());
                } catch (Exception abort) {
                    // ignore abort failure
                }
                throw new RuntimeException("Multipart upload failed for key: " + key, e);
            }
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException("Failed to initiate multipart upload for key: " + key, e);
        }
    }
}
