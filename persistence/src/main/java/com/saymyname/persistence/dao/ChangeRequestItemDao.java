// src/main/java/com/saymyname/persistence/dao/ChangeRequestItemDao.java
package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;
import com.saymyname.persistence.repository.ChangeRequestItemRepository;

@Repository
public class ChangeRequestItemDao {

    private final ChangeRequestItemRepository repo;

    public ChangeRequestItemDao(ChangeRequestItemRepository repo) {
        this.repo = repo;
    }

    /**
     * Détache en masse les FK CR→PA pour les PA tombstones expirées, org courante.
     */
    @Transactional
    public int detachExpiredTombstoneLinksForResolved(LocalDateTime cutoffExclusive) {
        return repo.nullifyPersonAttributeFkForResolvedAndExpiredTombstones(cutoffExclusive);
    }

    /* ======= CANCEL ======= */

    @Transactional
    public int cancelAllPending(Long changeRequestId, String comment) {
        return repo.cancelAllPendingByCrId(
                changeRequestId,
                ChangeRequestItemStatus.CANCELED,
                ChangeRequestItemStatus.PENDING,
                (comment == null ? "Canceled with envelope" : comment));
    }

    /* ======= RESOLUTION MIXTE ======= */

    @Transactional
    public int resolveMixedPendingOnly(Long changeRequestId,
            Collection<Long> approvedIds,
            Collection<Long> rejectedIds,
            String comment) {

        Set<Long> a = (approvedIds == null) ? Set.of()
                : approvedIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> r = (rejectedIds == null) ? Set.of()
                : rejectedIds.stream().filter(Objects::nonNull).collect(Collectors.toSet());

        Set<Long> intersection = new HashSet<>(a);
        intersection.retainAll(r);
        if (!intersection.isEmpty()) {
            throw new IllegalArgumentException(
                    "Un même item ne peut pas être à la fois APPROVED et REJECTED : " + intersection);
        }
        if (a.isEmpty() && r.isEmpty())
            return 0;

        List<Long> allIds = Stream.concat(a.stream(), r.stream()).toList();

        return repo.resolveMixedByIdsPendingOnly(
                changeRequestId,
                allIds,
                a,
                r,
                ChangeRequestItemStatus.APPROVED,
                ChangeRequestItemStatus.REJECTED,
                ChangeRequestItemStatus.PENDING,
                (comment == null ? "Resolved by admin" : comment));
    }

    /* ======= Comptages + chargements ======= */

    @Transactional(readOnly = true)
    public long countAllByCr(Long changeRequestId) {
        return repo.countAllByCr(changeRequestId);
    }

    @Transactional(readOnly = true)
    public long countByCrAndStatus(Long changeRequestId, ChangeRequestItemStatus status) {
        return repo.countByCrAndStatus(changeRequestId, status);
    }

    @Transactional(readOnly = true)
    public List<ChangeRequestItemEntity> loadItemsByIds(Long changeRequestId, Collection<Long> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return repo.findAllByCrAndIds(changeRequestId, ids);
    }

    @Transactional(readOnly = true)
    public List<Long> listPendingIdsByCr(Long changeRequestId) {
        return repo.findPendingIdsByCr(changeRequestId, ChangeRequestItemStatus.PENDING);
    }
}
