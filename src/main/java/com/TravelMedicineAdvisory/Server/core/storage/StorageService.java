package com.TravelMedicineAdvisory.Server.core.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {
    /**
     * Store a {@link MultipartFile}. Implementations should stream to disk for
     * large files rather than buffering the entire payload in heap.
     */
    Attachment store(MultipartFile file, String path, Long modelId, String modelName);

    /**
     * Store raw bytes at {@code path/filename}. Returns the storage key.
     */
    String storeBytes(byte[] content, String path, String filename, String contentType);

    /**
     * Stream-based store — preferred for large files (> multipart threshold).
     * {@code contentLength} may be {@code -1} if unknown.
     * Returns the storage key.
     */
    String storeStream(InputStream inputStream, long contentLength, String path, String filename, String contentType);

    byte[] readBytes(String path);

    void delete(String path);

    String getUrl(String path);
}
