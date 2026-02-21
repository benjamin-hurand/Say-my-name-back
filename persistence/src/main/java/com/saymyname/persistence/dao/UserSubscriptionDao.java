package com.saymyname.persistence.dao;

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
import com.saymyname.persistence.entity.organization.subscription.UserSubscriptionEntity;
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

    public boolean exists(Long userId, Long personId) {
        return repository.existsByUserIdAndPersonId(userId, personId);
    }

    public boolean subscribe(UserSubscription subscription) {
        try {
            repository.save(mapper.toEntity(subscription)); // tenantId rempli par listener
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }

    public int unsubscribe(Long userId, Long personId) {
        return (int) repository.deleteByUserIdAndPersonId(userId, personId);
    }

    public long countByUser(Long userId) {
        return repository.countByUserId(userId);
    }

    public Page<UserSubscription> findByUser(Long userId, Pageable pageable) {
        return repository.findByUserId(userId, pageable).map(mapper::toModel);
    }

    public Page<Long> findPersonIdsByUser(Long userId, Pageable pageable) {
        return repository.findPersonIdsPageByUserId(userId, pageable);
    }

    @Transactional
    public int bulkSubscribe(Long userId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty())
            return 0;

        List<Long> cleaned = personIds.stream().filter(Objects::nonNull).distinct().toList();
        if (cleaned.isEmpty())
            return 0;

        List<UserSubscriptionEntity> existing = repository.findByUserIdAndPersonIdIn(userId, cleaned);

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
                // fallback safe (en cas de concurrence / doublons)
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
        if (personIds == null || personIds.isEmpty())
            return 0;

        List<Long> cleaned = personIds.stream().filter(Objects::nonNull).distinct().toList();
        if (cleaned.isEmpty())
            return 0;

        int deleted = 0;
        final int chunkSize = 1000;

        for (int i = 0; i < cleaned.size(); i += chunkSize) {
            int end = Math.min(i + chunkSize, cleaned.size());
            var sub = cleaned.subList(i, end);
            deleted += repository.deleteByUserIdAndPersonIdIn(userId, sub);
        }
        return deleted;
    }

    public long countFollowedEligibleAND(Long userId, Long gameModeId) {
        return repository.countFollowedEligibleAND(userId, gameModeId);
    }

    public long countFollowedEligibleOR(Long userId, Long gameModeId) {
        return repository.countFollowedEligibleOR(userId, gameModeId);
    }
}