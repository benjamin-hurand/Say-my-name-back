// src/main/java/com/saymyname/service/attribute/AttributeMetaCache.java
package com.saymyname.service.attribute;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;

import org.springframework.stereotype.Component;

import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.repository.AttributeRepository;

/**
 * Cache par tenant des métadonnées d'attributs :
 * - id -> (isIdentitySource, isDerived, displayOrder)
 *
 * Chargé à la demande, avec un TTL simple.
 */
@Component
public class AttributeMetaCache {

    private static final Duration TTL = Duration.ofMinutes(10);
    private static final int DEFAULT_DISPLAY_ORDER = 100;

    private final AttributeDao attributeDao;

    /** tenantId -> entrée cache */
    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    public AttributeMetaCache(AttributeDao attributeDao) {
        this.attributeDao = attributeDao;
    }

    /** Représente une méta d'attribut. */
    public static final class Meta {
        public final boolean identitySource;
        public final boolean derived;
        public final int displayOrder;

        public Meta(boolean identitySource, boolean derived, int displayOrder) {
            this.identitySource = identitySource;
            this.derived = derived;
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

    public Map<Long, Meta> currentTenantMeta() {
        Long tenantId = TenantContext.get();
        if (tenantId == null) {
            throw new IllegalStateException("TenantContext.get() est null — impossible de résoudre les métadonnées.");
        }
        return metaForTenant(tenantId);
    }

    public Map<Long, Meta> metaForTenant(Long tenantId) {
        CacheEntry entry = cache.get(tenantId);
        if (entry != null && !entry.isExpired()) {
            return entry.byId;
        }
        synchronized (cache) {
            entry = cache.get(tenantId);
            if (entry != null && !entry.isExpired()) {
                return entry.byId;
            }
            CacheEntry fresh = load();
            cache.put(tenantId, fresh);
            return fresh.byId;
        }
    }

    public boolean isIdentitySource(Long attributeId) {
        if (attributeId == null)
            return false;
        Meta m = currentTenantMeta().get(attributeId);
        return m != null && m.identitySource;
    }

    public boolean isDerived(Long attributeId) {
        if (attributeId == null)
            return false;
        Meta m = currentTenantMeta().get(attributeId);
        return m != null && m.derived;
    }

    public int displayOrder(Long attributeId) {
        if (attributeId == null)
            return DEFAULT_DISPLAY_ORDER;
        Meta m = currentTenantMeta().get(attributeId);
        return (m != null) ? m.displayOrder : DEFAULT_DISPLAY_ORDER;
    }

    /**
     * ✅ Liste des attributeIds qui participent à l'identité
     * (is_identity_source=true),
     * triée par displayOrder puis id.
     *
     * NB: c'est ce que tu utilisais avant sous le nom "primary identity
     * attributes".
     */
    public List<Long> getIdentitySourceAttributeIds() {
        Map<Long, Meta> meta = currentTenantMeta();
        if (meta.isEmpty())
            return List.of();

        return meta.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue().identitySource)
                .sorted(Comparator
                        .comparingInt((Map.Entry<Long, Meta> e) -> e.getValue().displayOrder)
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .toList();
    }

    /**
     * ✅ Retourne l'attributeId dérivé "Identité composite" (is_derived=true).
     * Sans contrainte DB, on adopte une stratégie défensive :
     * - 0 trouvé => null
     * - 1 trouvé => id
     * - >1 trouvé => on prend le plus petit displayOrder puis id (déterministe)
     * mais on te conseille de normaliser.
     */
    public Long getDerivedIdentityAttributeId() {
        Map<Long, Meta> meta = currentTenantMeta();
        if (meta.isEmpty())
            return null;

        return meta.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue().derived)
                .sorted(Comparator
                        .comparingInt((Map.Entry<Long, Meta> e) -> e.getValue().displayOrder)
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    // ---- Eviction / reload

    public void evictCurrentTenant() {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            cache.remove(tenantId);
        }
    }

    public void evictTenant(Long tenantId) {
        if (tenantId != null) {
            cache.remove(tenantId);
        }
    }

    public void evictAll() {
        cache.clear();
    }

    public void reloadCurrentTenant() {
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            synchronized (cache) {
                cache.put(tenantId, load());
            }
        }
    }

    // ---- impl

    private CacheEntry load() {
        List<AttributeRepository.AttributeMetaRow> rows = attributeDao.findMetaForCurrentTenant();
        Map<Long, Meta> map = new LinkedHashMap<>(rows.size());
        for (AttributeRepository.AttributeMetaRow r : rows) {
            Long id = r.getId();
            boolean identitySource = r.getIdentitySource();
            boolean derived = r.getDerived();
            int order = r.getDisplayOrder();
            if (id != null) {
                map.put(id, new Meta(identitySource, derived, order));
            }
        }
        return new CacheEntry(Collections.unmodifiableMap(map), Instant.now());
    }
}