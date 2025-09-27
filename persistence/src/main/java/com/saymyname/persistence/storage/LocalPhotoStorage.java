// src/main/java/com/saymyname/persistence/storage/LocalPhotoStorage.java
package com.saymyname.persistence.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.*;
import java.util.UUID;

@Service
public class LocalPhotoStorage implements PhotoStorageReadable { // ⬅️ implémente aussi Readable

    private final Path root;
    private final String publicBaseUrl; // ex: "/photos/"

    public LocalPhotoStorage(
            @Value("${photos.storage.root}") String rootDir,
            @Value("${photos.public-base-url:/photos/}") String publicBaseUrl) {
        this.root = Paths.get(rootDir).toAbsolutePath().normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
        try {
            Files.createDirectories(this.root);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public StoredFile store(MultipartFile file) {
        String ext = switch (String.valueOf(file.getContentType())) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> "";
        };
        String key = UUID.randomUUID() + ext;
        Path dest = root.resolve(key).normalize();
        if (!dest.startsWith(root))
            throw new RuntimeException("Path traversal");

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Storing file failed", e);
        }
        return new StoredFile(key, publicBaseUrl + key);
    }

    @Override
    public void deleteQuietly(String key) {
        if (key == null || key.isBlank())
            return;
        Path p = root.resolve(key).normalize();
        if (!p.startsWith(root))
            return;
        try {
            Files.deleteIfExists(p);
        } catch (Exception ignore) {
        }
    }

    // ====== lecture (nécessaire pour les miniatures) ======
    @Override
    public InputStream open(String key) {
        try {
            Path p = root.resolve(key).normalize();
            if (!p.startsWith(root))
                throw new RuntimeException("Path traversal");
            return Files.newInputStream(p, StandardOpenOption.READ);
        } catch (Exception e) {
            throw new RuntimeException("Cannot open original image: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Path p = root.resolve(key).normalize();
            return p.startsWith(root) && Files.exists(p);
        } catch (Exception e) {
            return false;
        }
    }
}
