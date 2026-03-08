package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    Attachment store(MultipartFile file, String path, Long modelId, String modelName);
    void delete(String path);
    String getUrl(String path);
}
