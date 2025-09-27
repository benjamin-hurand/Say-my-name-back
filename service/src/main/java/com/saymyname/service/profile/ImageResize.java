// src/main/java/com/saymyname/service/profile/ImageResize.java
package com.saymyname.service.profile;

import java.awt.*;
import java.awt.image.BufferedImage;

final class ImageResize {

    private ImageResize() {
    }

    /**
     * Produit une miniature carrée (crop centré) de taille targetSize x targetSize.
     * - Pas d'upscale (si l'image est plus petite, on s'arrête à sa taille min).
     */
    static BufferedImage squareThumbnail(BufferedImage src, int targetSize) {
        if (src == null)
            throw new IllegalArgumentException("src is null");
        int w = src.getWidth();
        int h = src.getHeight();

        // Détermine le crop carré centré
        int side = Math.min(w, h);
        int x = (w - side) / 2;
        int y = (h - side) / 2;
        BufferedImage cropped = src.getSubimage(x, y, side, side);

        int outSize = Math.min(targetSize, side); // no upscale
        BufferedImage dst = new BufferedImage(outSize, outSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = dst.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.drawImage(cropped, 0, 0, outSize, outSize, null);
        } finally {
            g.dispose();
        }
        return dst;
    }
}
