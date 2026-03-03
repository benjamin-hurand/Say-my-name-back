package com.saymyname.persistence.dao;

import java.time.Clock;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.people.UserSubscription;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.mapper.UserSubscriptionEntityMapper;
import com.saymyname.persistence.repository.UserSubscriptionRepository;

@Repository
public class UserSubscriptionDao {

    private final UserSubscriptionRepository repository;
    private final UserSubscriptionEntityMapper mapper;

    // optionnel mais très clean pour tests + cohérence
    private final Clock clock;

    public UserSubscriptionDao(UserSubscriptionRepository repository,
            UserSubscriptionEntityMapper mapper) {
        this(repository, mapper, Clock.systemUTC());
    }

    public UserSubscriptionDao(UserSubscriptionRepository repository,
            UserSubscriptionEntityMapper mapper,
            Clock clock) {
        this.repository = repository;
        this.mapper = mapper;
        this.clock = clock;
    }

    public boolean exists(Long userId, Long personId) {
        if (userId == null || personId == null)
            return false;
        Long tenantId = TenantContext.get();
        return repository.existsByTenantIdAndUserIdAndPersonId(tenantId, userId, personId);
    }

    public boolean subscribe(UserSubscription subscription) {
        if (subscription == null)
            return false;
        try {
            repository.save(mapper.toEntity(subscription)); // tenantId rempli par listener (@PrePersist)
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    @Transactional
    public int unsubscribe(Long userId, Long personId) {
        if (userId == null || personId == null)
            return 0;
        Long tenantId = TenantContext.get();
        return (int) repository.deleteByTenantIdAndUserIdAndPersonId(tenantId, userId, personId);
    }

    public long countByUser(Long userId) {
        if (userId == null)
            return 0L;
        Long tenantId = TenantContext.get();
        return repository.countByTenantIdAndUserId(tenantId, userId);
    }

    public Page<UserSubscription> findByUser(Long userId, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable");
        if (userId == null)
            return Page.empty(pageable);
        Long tenantId = TenantContext.get();
        return repository.findByTenantIdAndUserId(tenantId, userId, pageable).map(mapper::toModel);
    }

    public Page<Long> findPersonIdsByUser(Long userId, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable");
        if (userId == null)
            return Page.empty(pageable);
        Long tenantId = TenantContext.get();
        return repository.findPersonIdsPageByTenantIdAndUserId(tenantId, userId, pageable);
    }

    @Transactional
    public int bulkSubscribe(Long userId, List<Long> personIds) {
        if (userId == null)
            return 0;
        if (personIds == null || personIds.isEmpty())
            return 0;

        Long tenantId = TenantContext.get();

        List<Long> cleaned = personIds.stream().filter(Objects::nonNull).distinct().toList();
        if (cleaned.isEmpty())
            return 0;

        List<UserSubscriptionEntity> existing = repository.findByTenantIdAndUserIdAndPersonIdIn(tenantId, userId,
                cleaned);

        Set<Long> existingPersonIds = existing.stream()
                .map(UserSubscriptionEntity::getPersonId)
                .collect(Collectors.toSet());

        List<UserSubscriptionEntity> toInsert = cleaned.stream()
                .filter(pid -> !existingPersonIds.contains(pid))
                .map(pid -> UserSubscriptionEntity.builder()
                        .userId(userId)
                        .personId(pid)
                        .build())
                .toList();

        if (toInsert.isEmpty())
            return 0;

        int inserted = 0;
        final int chunkSize = 1000;

        for (int i = 0; i < toInsert.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, toInsert.size());
            var chunk = toInsert.subList(i, end);

            try {
                repository.saveAll(chunk);
                inserted += chunk.size();
            } catch (DataIntegrityViolationException e) {
                for (var ent : chunk) {
                    try {
                        repository.save(ent);
                        inserted++;
                    } catch (DataIntegrityViolationException ignore) {
                        // déjà présent
                    }
                }
            }
        }
        return inserted;
    }

    @Transactional
    public int bulkUnsubscribe(Long userId, List<Long> personIds) {
        if (userId == null)
            return 0;
        if (personIds == null || personIds.isEmpty())
            return 0;

        Long tenantId = TenantContext.get();

        List<Long> cleaned = personIds.stream().filter(Objects::nonNull).distinct().toList();
        if (cleaned.isEmpty())
            return 0;

        int deleted = 0;
        final int chunkSize = 1000;

        for (int i = 0; i < cleaned.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, cleaned.size());
            var sub = cleaned.subList(i, end);
            deleted += repository.deleteByTenantIdAndUserIdAndPersonIdIn(tenantId, userId, sub);
        }
        return deleted;
    }
}