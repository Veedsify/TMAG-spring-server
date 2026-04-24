package com.TravelMedicineAdvisory.Server.core.storage;

import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class GcsStorageService implements StorageService {

    @Value("${app.storage.gcs.bucket}")
    private String bucketName;

    @Value("${app.storage.gcs.project-id:}")
    private String projectId;

    private final Storage storage;

    public GcsStorageService() {
        StorageOptions.Builder builder = StorageOptions.newBuilder();
        if (projectId != null && !projectId.isBlank()) {
            builder.setProjectId(projectId);
        }
        this.storage = builder.build().getService();
    }

    @Override
    public Attachment store(MultipartFile file, String customPath, Long modelId, String modelName) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            String objectName = customPath + "/" + filename;

            BlobId blobId = BlobId.of(bucketName, objectName);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();
            storage.create(blobInfo, file.getBytes());

            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setStoragePath(objectName);
            attachment.setModelId(modelId);
            attachment.setModelName(modelName);
            return attachment;
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file to GCS", e);
        }
    }

    @Override
    public String storeBytes(byte[] content, String customPath, String filename, String contentType) {
        String objectName = customPath + "/" + filename;
        BlobId blobId = BlobId.of(bucketName, objectName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();
        storage.create(blobInfo, content);
        return objectName;
    }

    @Override
    public byte[] readBytes(String customPath) {
        BlobId blobId = BlobId.of(bucketName, customPath);
        return storage.readAllBytes(blobId);
    }

    @Override
    public void delete(String customPath) {
        BlobId blobId = BlobId.of(bucketName, customPath);
        storage.delete(blobId);
    }

    @Override
    public String getUrl(String customPath) {
        return "https://storage.googleapis.com/" + bucketName + "/" + customPath;
    }
}
