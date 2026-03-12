package com.saymyname.service.attribute;

import com.saymyname.core.model.enums.concept.ConceptPortabilityKind;
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
    private static final int DEFAULT_DISPLAY_ORDER = 100;

    private final AttributeDao attributeDao;
    private final ConcurrentHashMap<Long, CacheEntry> cache = new ConcurrentHashMap<>();

    public AttributeMetaCache(AttributeDao attributeDao) {
        this.attributeDao = attributeDao;
    }

    public static final class Meta {
        public final boolean identitySource;
        public final int displayOrder;
        public final Long conceptId;
        public final String conceptCode;
        public final boolean conceptDerived;
        public final ConceptPortabilityKind portabilityKind;
        public final boolean identityComponentEligible;

        public Meta(
                boolean identitySource,
                int displayOrder,
                Long conceptId,
                String conceptCode,
                boolean conceptDerived,
                ConceptPortabilityKind portabilityKind,
                boolean identityComponentEligible) {
            this.identitySource = identitySource;
            this.displayOrder = displayOrder;
            this.conceptId = conceptId;
            this.conceptCode = conceptCode;
            this.conceptDerived = conceptDerived;
            this.portabilityKind = portabilityKind;
            this.identityComponentEligible = identityComponentEligible;
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

    public Meta meta(Long attributeId) {
        if (attributeId == null)
            return null;
        return currentTenantMeta().get(attributeId);
    }

    public boolean isIdentitySource(Long attributeId) {
        Meta m = meta(attributeId);
        return m != null && m.identitySource;
    }

    public boolean isDerived(Long attributeId) {
        Meta m = meta(attributeId);
        return m != null && m.conceptDerived;
    }

    public String conceptCode(Long attributeId) {
        Meta m = meta(attributeId);
        return m != null ? m.conceptCode : null;
    }

    public ConceptPortabilityKind portabilityKind(Long attributeId) {
        Meta m = meta(attributeId);
        return m != null ? m.portabilityKind : null;
    }

    public boolean isIdentityComponentEligible(Long attributeId) {
        Meta m = meta(attributeId);
        return m != null && m.identityComponentEligible;
    }

    public int displayOrder(Long attributeId) {
        Meta m = meta(attributeId);
        return (m != null) ? m.displayOrder : DEFAULT_DISPLAY_ORDER;
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

    public List<Long> getIdentityEligibleAttributeIds() {
        Map<Long, Meta> meta = currentTenantMeta();
        if (meta.isEmpty())
            return List.of();

        return meta.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null && e.getValue().identityComponentEligible)
                .sorted(Comparator
                        .comparingInt((Map.Entry<Long, Meta> e) -> e.getValue().displayOrder)
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .toList();
    }

    public Long getDerivedIdentityAttributeId() {
        Map<Long, Meta> meta = currentTenantMeta();
        if (meta.isEmpty())
            return null;

        return meta.entrySet().stream()
                .filter(e -> e.getKey() != null && e.getValue() != null)
                .filter(e -> "IDENTITY".equals(e.getValue().conceptCode) || e.getValue().conceptDerived)
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

            ConceptPortabilityKind portabilityKind = null;
            if (r.getPortabilityKind() != null) {
                portabilityKind = ConceptPortabilityKind.valueOf(r.getPortabilityKind());
            }

            boolean conceptDerived = r.getConceptDerived() != null && r.getConceptDerived();
            boolean identityEligible = r.getIdentityComponentEligible() != null && r.getIdentityComponentEligible();

            map.put(id, new Meta(
                    r.getIdentitySource(),
                    r.getDisplayOrder(),
                    r.getConceptId(),
                    r.getConceptCode(),
                    conceptDerived,
                    portabilityKind,
                    identityEligible));
        }

        return new CacheEntry(Collections.unmodifiableMap(map), Instant.now());
    }
}