// src/main/java/com/saymyname/persistence/storage/LocalSmallPhotoStorage.java
package com.saymyname.persistence.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.*;

@Service
public class LocalSmallPhotoStorage implements SmallPhotoStorage {

    private final Path smallRoot; // ex: {photos.storage.root}/small
    private final String publicBaseUrl; // ex: "/photos/small/"

    public LocalSmallPhotoStorage(
            @Value("${photos.storage.root}") String rootDir,
            @Value("${photos.small.subdir:small}") String smallSubdir,
            @Value("${photos.small.public-base-url:/photos/small/}") String publicBaseUrl) {
        Path root = Paths.get(rootDir).toAbsolutePath().normalize();
        this.smallRoot = root.resolve(smallSubdir).normalize();
        this.publicBaseUrl = publicBaseUrl.endsWith("/") ? publicBaseUrl : publicBaseUrl + "/";
        try {
            Files.createDirectories(this.smallRoot);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void storeSmall(String originalKey, BufferedImage image) {
        String format = guessFormatFromExtension(originalKey); // "jpg","png","webp"
        Path dest = smallRoot.resolve(originalKey).normalize();
        if (!dest.startsWith(smallRoot))
            throw new RuntimeException("Path traversal");
        try {
            Files.createDirectories(dest.getParent());
            ImageIO.write(image, format, dest.toFile());
        } catch (Exception e) {
            throw new RuntimeException("Writing thumbnail failed", e);
        }
    }

    @Override
    public boolean exists(String originalKey) {
        try {
            Path p = smallRoot.resolve(originalKey).normalize();
            return p.startsWith(smallRoot) && Files.exists(p);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public String publicSmallUrl(String originalKey) {
        return publicBaseUrl + originalKey;
    }

    @Override
    public void deleteQuietly(String originalKey) {
        try {
            Path p = smallRoot.resolve(originalKey).normalize();
            if (p.startsWith(smallRoot))
                Files.deleteIfExists(p);
        } catch (Exception ignore) {
        }
    }

    private static String guessFormatFromExtension(String key) {
        String lower = key == null ? "" : key.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg"))
            return "jpg";
        if (lower.endsWith(".png"))
            return "png";
        if (lower.endsWith(".webp"))
            return "webp"; // nécessite ImageIO plugin (ex: TwelveMonkeys)
        return "png"; // fallback
    }
}
