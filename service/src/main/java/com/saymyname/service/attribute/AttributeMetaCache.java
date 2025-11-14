// src/main/java/com/saymyname/service/attribute/AttributeMetaCache.java
package com.saymyname.service.attribute;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import com.saymyname.core.multitenancy.OrgContext;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.repository.AttributeRepository;

/**
 * Cache par organisation des métadonnées d'attributs :
 * - id -> (primaryField, displayOrder)
 * Chargé à la demande, avec un TTL simple.
 */
@Component
public class AttributeMetaCache {

    /** TTL du cache (ajuste si besoin). */
    private static final Duration TTL = Duration.ofMinutes(10);
    /** Valeur par défaut si l’attrib n’est pas trouvé. */
    private static final int DEFAULT_DISPLAY_ORDER = 100;

    private final AttributeDao attributeDao;

    /** orgId -> entrée cache */
    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    public AttributeMetaCache(AttributeDao attributeDao) {
        this.attributeDao = attributeDao;
    }

    /** Représente une méta d'attribut. */
    public static final class Meta {
        public final boolean primary;
        public final int displayOrder;

        public Meta(boolean primary, int displayOrder) {
            this.primary = primary;
            this.displayOrder = displayOrder;
        }
    }

    private static final class CacheEntry {
        final Map<Long, Meta> byId; // id -> meta
        final Instant loadedAt;

        CacheEntry(Map<Long, Meta> byId, Instant loadedAt) {
            this.byId = byId;
            this.loadedAt = loadedAt;
        }

        boolean isExpired() {
            return loadedAt.plus(TTL).isBefore(Instant.now());
        }
    }

    /** Obtenir la map meta pour l’org courante (via OrgContext). */
    public Map<Long, Meta> currentOrgMeta() {
        Long orgId = OrgContext.get();
        if (orgId == null) {
            throw new IllegalStateException("OrgContext.get() est null — impossible de résoudre les métadonnées.");
        }
        return metaForOrg(orgId);
    }

    /** Obtenir la map meta pour un org donné. */
    public Map<Long, Meta> metaForOrg(Long orgId) {
        CacheEntry entry = cache.get(orgId);
        if (entry != null && !entry.isExpired()) {
            return entry.byId;
        }
        synchronized (cache) {
            // double-check
            entry = cache.get(orgId);
            if (entry != null && !entry.isExpired()) {
                return entry.byId;
            }
            CacheEntry fresh = load(orgId);
            cache.put(orgId, fresh);
            return fresh.byId;
        }
    }

    /** True si l’attribut (dans l’org courante) est primaire. */
    public boolean isPrimary(Long attributeId) {
        if (attributeId == null)
            return false;
        Meta m = currentOrgMeta().get(attributeId);
        return m != null && m.primary;
    }

    /** Ordre d’affichage (dans l’org courante). */
    public int displayOrder(Long attributeId) {
        if (attributeId == null)
            return DEFAULT_DISPLAY_ORDER;
        Meta m = currentOrgMeta().get(attributeId);
        return (m != null) ? m.displayOrder : DEFAULT_DISPLAY_ORDER;
    }

    /** Invalider le cache pour l’org courante. */
    public void evictCurrentOrg() {
        Long orgId = OrgContext.get();
        if (orgId != null) {
            cache.remove(orgId);
        }
    }

    /** Invalider pour une org spécifique. */
    public void evictOrg(Long orgId) {
        if (orgId != null) {
            cache.remove(orgId);
        }
    }

    /** Invalider tout. */
    public void evictAll() {
        cache.clear();
    }

    /** Forcer un reload pour l’org courante. */
    public void reloadCurrentOrg() {
        Long orgId = OrgContext.get();
        if (orgId != null) {
            synchronized (cache) {
                cache.put(orgId, load(orgId));
            }
        }
    }

    // --- impl ---

    private CacheEntry load(Long orgId) {
        List<AttributeRepository.AttributeMetaRow> rows = attributeDao.findMetaByOrgId(orgId);
        Map<Long, Meta> map = new LinkedHashMap<>(rows.size());
        for (AttributeRepository.AttributeMetaRow r : rows) {
            Long id = r.getId();
            boolean primary = r.getPrimaryField();
            int order = r.getDisplayOrder();
            if (id != null) {
                map.put(id, new Meta(primary, order));
            }
        }
        return new CacheEntry(Collections.unmodifiableMap(map), Instant.now());
    }
}
