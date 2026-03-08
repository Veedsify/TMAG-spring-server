package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    @Value("${storage.local.base-path:storage/uploads}")
    private String basePath;

    @Value("${storage.local.base-url:http://localhost:8080/storage/uploads}")
    private String baseUrl;

    @Override
    public Attachment store(MultipartFile file, String customPath, Long modelId, String modelName) {
        try {
            String filename = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path uploadPath = Paths.get(basePath, customPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);

            Attachment attachment = new Attachment();
            attachment.setFileName(file.getOriginalFilename());
            attachment.setFileSize(file.getSize());
            attachment.setContentType(file.getContentType());
            attachment.setStoragePath(Paths.get(customPath, filename).toString());
            attachment.setModelId(modelId);
            attachment.setModelName(modelName);

            return attachment;

        } catch (IOException e) {
            throw new RuntimeException("Failed to store file", e);
        }
    }

    @Override
    public void delete(String customPath) {
        try {
            Path filePath = Paths.get(basePath, customPath);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file", e);
        }
    }

    @Override
    public String getUrl(String customPath) {
        return baseUrl + "/" + customPath;
    }
}
