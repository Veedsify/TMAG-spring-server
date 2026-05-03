package com.TravelMedicineAdvisory.Server.domain.user;

import org.springframework.stereotype.Service;

import com.TravelMedicineAdvisory.Server.core.storage.StorageService;

@Service
public class AvatarUrlService {

    private final StorageService storageService;

    public AvatarUrlService(StorageService storageService) {
        this.storageService = storageService;
    }

    public String toFullUrl(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank() || avatarUrl.startsWith("http://") || avatarUrl.startsWith("https://")) {
            return avatarUrl;
        }
        if (avatarUrl.startsWith("/storage/avatars/")) {
            return storageService.getUrl("avatars/" + avatarUrl.substring("/storage/avatars/".length()));
        }
        if (avatarUrl.startsWith("/storage/upload/")) {
            return storageService.getUrl(avatarUrl.substring("/storage/upload/".length()));
        }
        if (avatarUrl.startsWith("/storage/uploads/")) {
            return storageService.getUrl(avatarUrl.substring("/storage/uploads/".length()));
        }
        return storageService.getUrl(avatarUrl.replaceFirst("^/", ""));
    }
}
