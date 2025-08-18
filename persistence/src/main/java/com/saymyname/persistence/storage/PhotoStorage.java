package com.saymyname.persistence.storage;

import org.springframework.web.multipart.MultipartFile;

public interface PhotoStorage {
    record StoredFile(String key, String publicUrl) {
    }

    StoredFile store(MultipartFile file);

    void deleteQuietly(String key);
}
