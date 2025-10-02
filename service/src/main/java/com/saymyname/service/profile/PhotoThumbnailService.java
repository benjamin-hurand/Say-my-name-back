// src/main/java/com/saymyname/service/profile/PhotoThumbnailService.java
package com.saymyname.service.profile;

import com.saymyname.persistence.storage.PhotoStorageReadable;
import com.saymyname.persistence.storage.SmallPhotoStorage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.InputStream;

@Service
public class PhotoThumbnailService {

    private final PhotoStorageReadable originalStorage;
    private final SmallPhotoStorage smallStorage;

    private final int maxW;
    private final int maxH;

    public PhotoThumbnailService(
            PhotoStorageReadable originalStorage,
            SmallPhotoStorage smallStorage,
            @Value("${photos.small.max-width:256}") int maxW,
            @Value("${photos.small.max-height:256}") int maxH) {
        this.originalStorage = originalStorage;
        this.smallStorage = smallStorage;
        this.maxW = maxW;
        this.maxH = maxH;
    }

    /** Génère et stocke la miniature (écrase si existe). */
    public String generateAndStore(String originalKey) {
        try (InputStream in = originalStorage.open(originalKey)) {
            BufferedImage src = ImageIO.read(in);
            if (src == null)
                throw new IllegalStateException("Image non lisible: " + originalKey);
            BufferedImage thumb = resizeKeepingRatio(src, maxW, maxH, true);
            smallStorage.storeSmall(originalKey, thumb);
            return smallStorage.publicSmallUrl(originalKey);
        } catch (Exception e) {
            throw new RuntimeException("Thumbnail generation failed for " + originalKey, e);
        }
    }

    /** Vérifie et génère au besoin (synchrone). Renvoie l’URL publique small. */
    public String ensureSmallExists(String originalKey) {
        if (smallStorage.exists(originalKey)) {
            return smallStorage.publicSmallUrl(originalKey);
        }
        return generateAndStore(originalKey);
    }

    /** Variante asynchrone (pour fallback non bloquant dans le listing). */
    @Async("appAsyncExecutor")
    public void ensureSmallExistsAsync(String originalKey) {
        try {
            ensureSmallExists(originalKey);
        } catch (Exception ignore) {
            // log.warn("Cannot build thumbnail for {}", originalKey, ignore);
        }
    }

    // --- util ---
    private static BufferedImage resizeKeepingRatio(BufferedImage src, int maxW, int maxH, boolean highQuality) {
        int w = src.getWidth();
        int h = src.getHeight();
        double ratio = Math.min((double) maxW / w, (double) maxH / h);
        ratio = Math.min(ratio, 1.0); // ne pas upscaler
        int tw = Math.max(1, (int) Math.round(w * ratio));
        int th = Math.max(1, (int) Math.round(h * ratio));

        int type = src.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : src.getType();
        BufferedImage dst = new BufferedImage(tw, th, type);
        Graphics2D g = dst.createGraphics();
        try {
            if (highQuality) {
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            }
            g.drawImage(src, 0, 0, tw, th, null);
        } finally {
            g.dispose();
        }
        return dst;
    }
}
