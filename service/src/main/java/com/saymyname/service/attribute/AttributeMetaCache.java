package com.saymyname.service.attribute;

import com.saymyname.core.model.people.ConceptCodes;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.repository.AttributeRepository;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AttributeMetaCache {

    private static final Duration TTL = Duration.ofMinutes(10);
    private final AttributeDao attributeDao;
    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    public AttributeMetaCache(AttributeDao attributeDao) {
        this.attributeDao = attributeDao;
    }

    public static final class Meta {
        public final boolean identitySource;
        public final int displayOrder;
        public final String conceptCode;

        public Meta(
                boolean identitySource,
                int displayOrder,
                String conceptCode) {
            this.identitySource = identitySource;
            this.displayOrder = displayOrder;
            this.conceptCode = conceptCode;
        }
    }

    private static final class CacheEntry {
        final Map<Long, Meta> byId;
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
            CacheEntry fresh = load(tenantId);
            cache.put(tenantId, fresh);
            return fresh.byId;
        }
    }

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

    public Long getIdentityAttributeId() {
        Map<Long, Meta> meta = currentTenantMeta();
        if (meta.isEmpty())
            return null;

        return meta.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .filter(e -> ConceptCodes.IDENTITY.equals(e.getValue().conceptCode))
                .sorted(Comparator
                        .comparingInt((Map.Entry<Long, Meta> e) -> e.getValue().displayOrder)
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

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
                cache.put(tenantId, load(tenantId));
            }
        }
    }

    private CacheEntry load(Long tenantId) {
        List<AttributeRepository.AttributeMetaRow> rows = attributeDao.findMetaByTenantId(tenantId);
        Map<Long, Meta> map = new LinkedHashMap<>(rows.size());

        for (AttributeRepository.AttributeMetaRow r : rows) {
            Long id = r.getId();
            if (id == null)
                continue;

            map.put(id, new Meta(
                    r.getIdentitySource(),
                    r.getDisplayOrder(),
                    r.getConceptCode()));
        }

        return new CacheEntry(Collections.unmodifiableMap(map), Instant.now());
    }
}
