// src/main/java/com/saymyname/service/photo/PhotoUrlResolver.java
package com.saymyname.service.photo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PhotoUrlResolver {

    /** Public base for originals & smalls, e.g. "http://localhost:8080/photos/" */
    @Value("${photos.storage.public-base-url}")
    private String publicBaseUrl;

    /** Folder name for small files under the public base, e.g. "small" */
    @Value("${photos.storage.small-subdir:small}")
    private String smallSubdir;

    /**
     * Placeholder path relative to the public base, e.g.
     * "placeholder/avatar-sm.webp"
     */
    @Value("${photos.storage.placeholder-small:placeholder/avatar-sm.webp}")
    private String placeholderSmall;

    /**
     * If true, return the API endpoint that ensures small exists before redirecting
     */
    @Value("${photos.ensure-small-via-endpoint:true}")
    private boolean ensureSmallViaEndpoint;

    /** Base path of the ensure endpoint (no host), e.g. "/api/photos/small/" */
    @Value("${photos.api.small-endpoint-base:/api/photos/small/}")
    private String apiSmallEndpointBase;

    /**
     * Full-size file URL (direct public) — not used for the trombi, but handy
     * elsewhere
     */
    public String largeUrl(String storageKey) {
        if (isBlank(storageKey))
            return null;
        return join(publicBaseUrl, storageKey);
    }

    /**
     * Trombi miniature URL:
     * - if no key: placeholder
     * - if ensureSmallViaEndpoint: "/api/photos/small/{key}" (will 302 to static
     * once ready)
     * - else direct static small "/photos/small/{key}"
     */
    public String smallUrl(String storageKey) {
        if (isBlank(storageKey))
            return placeholderUrl();

        if (ensureSmallViaEndpoint) {
            return join(trimTrailingSlash(apiSmallEndpointBase), storageKey);
        }
        return join(publicBaseUrl, smallSubdir + "/" + storageKey);
    }

    /**
     * Direct static small URL (no ensure step). Useful if you pre-generated all.
     */
    public String smallStaticUrl(String storageKey) {
        if (isBlank(storageKey))
            return placeholderUrl();
        return join(publicBaseUrl, smallSubdir + "/" + storageKey);
    }

    public String placeholderUrl() {
        return join(publicBaseUrl, placeholderSmall);
    }

    // ---- helpers ----
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String join(String base, String tail) {
        if (base == null)
            return tail;
        boolean slash = base.endsWith("/");
        return slash ? (base + tail) : (base + "/" + tail);
    }

    private static String trimTrailingSlash(String s) {
        if (s == null || s.isEmpty())
            return "/";
        return s.endsWith("/") ? s : s + "/";
    }
}
