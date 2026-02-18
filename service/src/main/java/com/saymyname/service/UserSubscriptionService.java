package com.saymyname.service;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.FollowFilter;
import com.saymyname.core.model.people.SubscriptionBulkResult;
import com.saymyname.core.model.people.UserSubscription;
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.persistence.dao.UserSubscriptionDao;
import com.saymyname.service.person.PersonService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class UserSubscriptionService {

    private final UserSubscriptionDao dao;
    private final PersonService personService;

    public UserSubscriptionService(UserSubscriptionDao dao, PersonService personService) {
        this.dao = dao;
        this.personService = personService;
    }

    /** Abonne (idempotent). */
    @Transactional
    public boolean subscribe(UserSubscription subscription) {
        return dao.subscribe(subscription);
    }

    /** Abonne en masse (idempotent). */
    @Transactional
    public int bulkSubscribe(Long userId, List<Long> personIds) {
        return dao.bulkSubscribe(userId, personIds);
    }

    @Transactional
    public int bulkUnsubscribe(Long userId, List<Long> personIds) {
        return dao.bulkUnsubscribe(userId, personIds);
    }

    /** Désabonne. Retourne 0/1. */
    @Transactional
    public int unsubscribe(Long userId, Long personId) {
        return dao.unsubscribe(userId, personId);
    }

    @Transactional(readOnly = true)
    public boolean isSubscribed(Long userId, Long personId) {
        return dao.exists(userId, personId);
    }

    @Transactional(readOnly = true)
    public long countFollowed(Long userId) {
        return dao.countByUser(userId);
    }

    public long countFollowedEligibleForMode(Course course) {
        Long userId = course.getUser().getId();
        Long gameModeId = course.getGameMode().getId();
        var op = course.getGameMode().getOperator();
        String operator = (op == null || op.isBlank()) ? "AND" : op.trim();
        if ("AND".equalsIgnoreCase(operator)) {
            return dao.countFollowedEligibleAND(userId, gameModeId);
        } else {
            return dao.countFollowedEligibleOR(userId, gameModeId);
        }
    }

    /** Page d'abonnements (modèles complets). */
    @Transactional(readOnly = true)
    public Page<UserSubscription> listSubscriptions(Long userId, Pageable pageable) {
        return dao.findByUser(userId, pageable);
    }

    /** Version optimisée pour le Quiz: IDs seulement. */
    @Transactional(readOnly = true)
    public Page<Long> listFollowedPersonIds(Long userId, Pageable pageable) {
        return dao.findPersonIdsByUser(userId, pageable);
    }

    /** --- Helpers --- */
    private PersonSearchCriteria sanitizeForBulk(PersonSearchCriteria criteria) {
        // IMPORTANT : bulk = sur l'ensemble des résultats, on ignore
        // suivis/tri/pagination
        PersonSearchCriteria copy = PersonSearchCriteria.builder()
                .filters(criteria.getFilters())
                .followFilter(FollowFilter.ALL)
                .includeContextAttributes(criteria.isIncludeContextAttributes())
                .sort(null)
                .build();
        return copy;
    }

    /**
     * --- NOUVEAU : Follow tous les Person qui matchent les critères (idempotent).
     */
    @Transactional
    public SubscriptionBulkResult followAllMatching(PersonSearchCriteria criteria, Long userId) {
        var start = Instant.now();

        var crit = sanitizeForBulk(criteria);
        List<Long> ids = personService.findPersonIds(crit, userId);
        int matched = ids.size();
        if (matched == 0) {
            return new SubscriptionBulkResult(0, 0, 0, 0.0);
        }

        int acted = dao.bulkSubscribe(userId, ids);
        int skipped = matched - acted;
        double seconds = (Instant.now().toEpochMilli() - start.toEpochMilli()) / 1000.0;

        return new SubscriptionBulkResult(matched, acted, skipped, seconds);
    }

    /**
     * --- NOUVEAU : Unfollow tous les Person qui matchent les critères
     * (idempotent).
     */
    @Transactional
    public SubscriptionBulkResult unfollowAllMatching(PersonSearchCriteria criteria, Long userId) {
        var start = Instant.now();

        var crit = sanitizeForBulk(criteria);
        List<Long> ids = personService.findPersonIds(crit, userId);
        int matched = ids.size();
        if (matched == 0) {
            return new SubscriptionBulkResult(0, 0, 0, 0.0);
        }

        int acted = dao.bulkUnsubscribe(userId, ids);
        int skipped = matched - acted; // ceux déjà non suivis
        double seconds = (Instant.now().toEpochMilli() - start.toEpochMilli()) / 1000.0;

        return new SubscriptionBulkResult(matched, acted, skipped, seconds);
    }
}
