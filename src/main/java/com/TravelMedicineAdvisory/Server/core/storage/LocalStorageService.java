package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    @Value("${app.storage.path:storage/upload}")
    private String basePath;

    @Value("${app.storage.base-url:http://localhost:8080/storage/upload}")
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
    public String storeBytes(byte[] content, String customPath, String filename, String contentType) {
        try {
            Path uploadPath = Paths.get(basePath, customPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.write(filePath, content);
            return Paths.get(customPath, filename).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file bytes", e);
        }
    }

    @Override
    public String storeStream(InputStream inputStream, long contentLength, String customPath,
                               String filename, String contentType) {
        try {
            Path uploadPath = Paths.get(basePath, customPath);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(filename);
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            return Paths.get(customPath, filename).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to stream file to local storage", e);
        }
    }

    @Override
    public byte[] readBytes(String customPath) {
        try {
            Path filePath = Paths.get(basePath, customPath);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file bytes", e);
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
        return baseUrl.replaceAll("/+$", "") + "/" + customPath;
    }
}
