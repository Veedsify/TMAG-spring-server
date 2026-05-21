package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * StorageService backed by AWS S3 (or any S3-compatible provider).
 *
 * <p>
 * Files whose size is at or above {@code multipartThresholdBytes} are uploaded
 * using the S3 multipart upload API so that individual parts are streamed
 * rather
 * than loading the entire file into memory at once. This prevents heap
 * exhaustion
 * and connection timeouts for files larger than 100 MB.
 */
public class S3StorageService implements StorageService {

    /** Minimum part size enforced by S3 (5 MiB). */
    private static final long MIN_PART_SIZE = 5L * 1024L * 1024L;

    private final String bucket;
    private final String region;
    private final String accessKey;
    private final String secretKey;
    private final String endpoint;
    private final long multipartThresholdBytes;
    private final long partSizeBytes;

    public S3StorageService(String bucket, String region, String accessKey, String secretKey,
            String endpoint, long multipartThresholdBytes, long partSizeBytes) {
        this.bucket = bucket;
        this.region = region;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.endpoint = endpoint;
        this.multipartThresholdBytes = multipartThresholdBytes;
        this.partSizeBytes = Math.max(partSizeBytes, MIN_PART_SIZE);
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

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
            throw new RuntimeException("Failed to store file to S3", e);
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
        if (endpoint != null && !endpoint.isBlank()) {
            return endpoint.replaceAll("/$", "") + "/" + bucket + "/" + path;
        }
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + path;
    }

    // -------------------------------------------------------------------------
    // Multipart upload
    // -------------------------------------------------------------------------

    /**
     * Executes an S3 multipart upload, reading {@code inputStream} in
     * {@code partSizeBytes} chunks. The upload is aborted automatically if
     * any part fails.
     *
     * @param inputStream   the source data stream
     * @param contentLength total byte count, or {@code -1} if unknown
     * @param key           S3 object key
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
                int bytesRead;
                int offset = 0;
                while (true) {
                    // Fill the buffer for one part
                    int remaining = buffer.length - offset;
                    bytesRead = inputStream.read(buffer, offset, remaining);

                    boolean endOfStream = bytesRead == -1;
                    if (!endOfStream) {
                        offset += bytesRead;
                    }

                    boolean bufferFull = offset == buffer.length;
                    if ((bufferFull || endOfStream) && offset > 0) {
                        byte[] partData = offset == buffer.length ? buffer : java.util.Arrays.copyOf(buffer, offset);
                        UploadPartResponse partResponse = client.uploadPart(
                                UploadPartRequest.builder()
                                        .bucket(bucket).key(key)
                                        .uploadId(uploadId)
                                        .partNumber(partNumber)
                                        .contentLength((long) partData.length)
                                        .build(),
                                RequestBody.fromBytes(partData));
                        completedParts.add(CompletedPart.builder()
                                .partNumber(partNumber)
                                .eTag(partResponse.eTag())
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
                // Best-effort abort to avoid orphaned parts
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
