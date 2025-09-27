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
import com.saymyname.persistence.entity.subscription.UserSubscriptionEntity;
import com.saymyname.persistence.entity.subscription.UserSubscriptionId;
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
        return repository.existsById(new UserSubscriptionId(userId, personId));
    }

    /**
     * Renvoie true si une ligne a été insérée, false si déjà existant (idempotent).
     */
    public boolean subscribe(UserSubscription subscription) {
        var id = new UserSubscriptionId(subscription.getUserId(), subscription.getPersonId());
        try {
            repository.save(new UserSubscriptionEntity(id, null)); // createdAt par DB
            return true;
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            // PK (userId, personId) déjà présente -> idempotent
            return false;
        }
    }

    public int unsubscribe(Long userId, Long personId) {
        return (int) repository.deleteByIdUserIdAndIdPersonId(userId, personId);
    }

    public long countByUser(Long userId) {
        return repository.countByIdUserId(userId);
    }

    public Page<UserSubscription> findByUser(Long userId, Pageable pageable) {
        return repository.findByIdUserId(userId, pageable).map(mapper::toModel);
    }

    public Page<Long> findPersonIdsByUser(Long userId, Pageable pageable) {
        return repository.findPersonIdsPageByUserId(userId, pageable);
    }

    /**
     * Bulk idempotent : renvoie le nombre réellement inséré.
     * - dédoublonne les entrées
     * - filtre les existants en 1 requête
     * - saveAll des manquants en chunks
     */
    @Transactional
    public int bulkSubscribe(Long userId, List<Long> personIds) {
        if (personIds == null || personIds.isEmpty())
            return 0;

        List<Long> cleaned = personIds.stream().filter(Objects::nonNull).distinct().toList();
        if (cleaned.isEmpty())
            return 0;

        // existants pour cet user
        List<UserSubscriptionEntity> existing = repository.findByIdUserIdAndIdPersonIdIn(userId, cleaned);
        Set<Long> existingPersonIds = existing.stream()
                .map(e -> e.getId().getPersonId())
                .collect(Collectors.toSet());

        List<UserSubscriptionEntity> toInsert = cleaned.stream()
                .filter(pid -> !existingPersonIds.contains(pid))
                .map(pid -> new UserSubscriptionEntity(new UserSubscriptionId(userId, pid), null))
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
                // course condition sur une minorité d’IDs
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

    /**
     * --- NOUVEAU : bulkUnsubscribe idempotent, renvoie le nombre réellement
     * supprimé.
     */
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
}
