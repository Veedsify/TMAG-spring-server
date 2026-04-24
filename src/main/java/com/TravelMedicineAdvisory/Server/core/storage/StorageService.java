package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    Attachment store(MultipartFile file, String path, Long modelId, String modelName);
    String storeBytes(byte[] content, String path, String filename, String contentType);
    byte[] readBytes(String path);
    void delete(String path);
    String getUrl(String path);
}
