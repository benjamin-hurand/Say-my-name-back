package com.saymyname.persistence.dao;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.people.UserSubscription;
import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionId;
import com.saymyname.persistence.mapper.UserSubscriptionEntityMapper;
import com.saymyname.persistence.repository.UserSubscriptionRepository;

@Repository
public class UserSubscriptionDao {

    private final UserSubscriptionRepository repository;
    private final UserSubscriptionEntityMapper mapper;

    public UserSubscriptionDao(UserSubscriptionRepository repository,
            UserSubscriptionEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    private static Long tenantIdOrThrow() {
        Long t = TenantContext.get();
        if (t == null)
            throw new IllegalStateException("TenantContext is null");
        return t;
    }

    public boolean exists(Long userId, Long personId) {
        Long tenantId = tenantIdOrThrow();
        return repository.existsById(new UserSubscriptionId(tenantId, userId, personId));
    }

    public boolean subscribe(UserSubscription subscription) {
        Long tenantId = tenantIdOrThrow();
        var id = new UserSubscriptionId(tenantId, subscription.getUserId(), subscription.getPersonId());
        try {
            repository.save(new UserSubscriptionEntity(id, null)); // created_at géré DB
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            return false;
        }
    }

    public int unsubscribe(Long userId, Long personId) {
        Long tenantId = tenantIdOrThrow();
        return (int) repository.deleteByIdTenantIdAndIdUserIdAndIdPersonId(tenantId, userId, personId);
    }

    public long countByUser(Long userId) {
        Long tenantId = tenantIdOrThrow();
        return repository.countByIdTenantIdAndIdUserId(tenantId, userId);
    }

    public Page<UserSubscription> findByUser(Long userId, Pageable pageable) {
        Long tenantId = tenantIdOrThrow();
        return repository.findByIdTenantIdAndIdUserId(tenantId, userId, pageable).map(mapper::toModel);
    }

    public Page<Long> findPersonIdsByUser(Long userId, Pageable pageable) {
        Long tenantId = tenantIdOrThrow();
        return repository.findPersonIdsPageByTenantIdAndUserId(tenantId, userId, pageable);
    }

    @Transactional
    public int bulkSubscribe(Long userId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty())
            return 0;

        Long tenantId = tenantIdOrThrow();

        List<Long> cleaned = personIds.stream().filter(Objects::nonNull).distinct().toList();
        if (cleaned.isEmpty())
            return 0;

        List<UserSubscriptionEntity> existing = repository.findByIdTenantIdAndIdUserIdAndIdPersonIdIn(tenantId, userId,
                cleaned);

        Set<Long> existingPersonIds = existing.stream()
                .map(e -> e.getId().getPersonId())
                .collect(Collectors.toSet());

        List<UserSubscriptionEntity> toInsert = cleaned.stream()
                .filter(pid -> !existingPersonIds.contains(pid))
                .map(pid -> new UserSubscriptionEntity(new UserSubscriptionId(tenantId, userId, pid), null))
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
            } catch (org.springframework.dao.DataIntegrityViolationException e) {
                for (var e1 : chunk) {
                    try {
                        repository.save(e1);
                        inserted++;
                    } catch (org.springframework.dao.DataIntegrityViolationException ignore) {
                        /* déjà présent */ }
                }
            }
        }
        return inserted;
    }

    @Transactional
    public int bulkUnsubscribe(Long userId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty())
            return 0;

        Long tenantId = tenantIdOrThrow();

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

    // Ces deux méthodes doivent probablement devenir tenant-scoped aussi
    public long countFollowedEligibleAND(Long userId, Long gameModeId) {
        Long tenantId = tenantIdOrThrow();
        return repository.countFollowedEligibleAND(tenantId, userId, gameModeId);
    }

    public long countFollowedEligibleOR(Long userId, Long gameModeId) {
        Long tenantId = tenantIdOrThrow();
        return repository.countFollowedEligibleOR(tenantId, userId, gameModeId);
    }
}