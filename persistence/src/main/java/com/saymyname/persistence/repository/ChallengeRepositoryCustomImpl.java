package com.saymyname.persistence.repository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.core.model.challenge.ChallengeSortCriterion;
import com.saymyname.core.model.enums.OrderDirection;
import com.saymyname.core.model.enums.UserPerformance;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.persistence.entity.ChallengeAttemptEntity;
import com.saymyname.persistence.entity.ChallengeEntity;
import com.saymyname.persistence.entity.ChallengeVersionEntity;
import com.saymyname.persistence.entity.GameModeEntity;
import com.saymyname.persistence.entity.attribute.AttributeEntity;
import com.saymyname.persistence.projection.ChallengeCardProjection;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Tuple;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

@Repository
@Transactional(readOnly = true)
public class ChallengeRepositoryCustomImpl implements ChallengeRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    @Override
    public List<ChallengeCardProjection> findChallengeCards(ChallengeMenu challengeMenu) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Tuple> cq = cb.createTupleQuery();

        // Racine : ChallengeEntity (alias "c")
        Root<ChallengeEntity> challenge = cq.from(ChallengeEntity.class);

        // Jointures vers GameModeEntity, l'attribut de filtre et les versions du
        // challenge
        Join<ChallengeEntity, GameModeEntity> gmJoin = challenge.join("gameMode", JoinType.INNER);
        Join<ChallengeEntity, AttributeEntity> attrJoin = challenge.join("filterAttribute", JoinType.INNER);
        Join<ChallengeEntity, ChallengeVersionEntity> cvJoin = challenge.join("challengeVersions", JoinType.INNER);

        List<Predicate> predicates = new ArrayList<>();

        // 1. Validité temporelle : versions et attributs doivent être valides à la date
        // de début de saison
        if (challengeMenu.getSeasonStart() != null) {
            LocalDateTime seasonStart = challengeMenu.getSeasonStart();
            predicates.add(cb.or(cb.isNull(cvJoin.get("endDate")),
                    cb.greaterThanOrEqualTo(cvJoin.get("endDate"), seasonStart)));
            // A quoi peuvent servir les predicates sur la validité des valeurs d'attributs
            // ? pour l'instant on retire, on verra apres
            // predicates.add(cb.lessThanOrEqualTo(attrJoin.get("validFrom"), seasonStart));
            // predicates.add(cb.or(cb.isNull(attrJoin.get("validTo")),
            // cb.greaterThanOrEqualTo(attrJoin.get("validTo"), seasonStart)));
        }

        // 2. Filtre de recherche textuelle sur la description du challenge et le titre
        // du gamemode
        if (challengeMenu.getSearch() != null && !challengeMenu.getSearch().trim().isEmpty()) {
            String searchTerm = "%" + challengeMenu.getSearch().toLowerCase() + "%";
            Predicate searchPredicate = cb.or(
                    cb.like(cb.lower(challenge.get("description")), searchTerm),
                    cb.like(cb.lower(gmJoin.get("gameModeTitle")), searchTerm));
            predicates.add(searchPredicate);
        }

        // 3. Filtre sur les gameModeIds (si définis)
        if (challengeMenu.getFilters() != null &&
                challengeMenu.getFilters().getGameModeIds() != null &&
                !challengeMenu.getFilters().getGameModeIds().isEmpty()) {
            predicates.add(gmJoin.get("id").in(challengeMenu.getFilters().getGameModeIds()));
        }

        // 4. Filtre sur le nombre de questions (questionCount)
        if (challengeMenu.getFilters() != null) {
            if (challengeMenu.getFilters().getQuestionsRangeMin() != null) {
                predicates.add(cb.ge(cvJoin.get("questionCount"), challengeMenu.getFilters().getQuestionsRangeMin()));
            }
            if (challengeMenu.getFilters().getQuestionsRangeMax() != null) {
                predicates.add(cb.le(cvJoin.get("questionCount"), challengeMenu.getFilters().getQuestionsRangeMax()));
            }
        }

        // 5. Filtre sur la plage de dates de création du challenge
        if (challengeMenu.getFilters() != null &&
                challengeMenu.getFilters().getDateRangeMin() != null &&
                challengeMenu.getFilters().getDateRangeMax() != null) {
            LocalDateTime minDate = challengeMenu.getFilters().getDateRangeMin().atStartOfDay();
            LocalDateTime maxDate = challengeMenu.getFilters().getDateRangeMax().atTime(23, 59, 59);
            predicates.add(cb.between(challenge.get("creationDate"), minDate, maxDate));
        }

        // 6. Sous-requête pour obtenir le dernier versionNumber pour chaque challenge,
        // en ne considérant que les versions valides à la date de début de saison.
        Subquery<Integer> latestVersionSubquery = cq.subquery(Integer.class);
        Root<ChallengeVersionEntity> cv2 = latestVersionSubquery.from(ChallengeVersionEntity.class);
        latestVersionSubquery.select(cb.max(cv2.get("versionNumber")))
                .where(
                        cb.equal(cv2.get("challenge").get("id"), challenge.get("id")),
                        cb.or(cb.isNull(cv2.get("endDate")),
                                cb.greaterThanOrEqualTo(cv2.get("endDate"), challengeMenu.getSeasonStart())));
        predicates.add(cb.equal(cvJoin.get("versionNumber"), latestVersionSubquery));

        // 7. Filtre sur le GameAttributeFilter par intersection (puisqu'un challenge
        // n'a qu'un seul filtre)
        if (challengeMenu.getFilters() != null && challengeMenu.getFilters().getAttributeFilter() != null) {
            // Supposons que getAttributeFilter() renvoie un GameAttributeFilter
            // Vérifier que l'attribut correspond
            predicates.add(cb.equal(attrJoin.get("id"),
                    challengeMenu.getFilters().getAttributeFilter().getAttribute().getId()));
            // Condition d'intersection entre la plage du challenge et la plage utilisateur
            // :
            // (challenge.minFilterValue <= userFilter.maxValue) AND
            // (challenge.maxFilterValue >= userFilter.minValue)
            String userMin = challengeMenu.getFilters().getAttributeFilter().getMinValue();
            String userMax = challengeMenu.getFilters().getAttributeFilter().getMaxValue();
            if (userMin != null && userMax != null && !userMin.trim().isEmpty() && !userMax.trim().isEmpty()) {
                predicates.add(cb.and(
                        cb.lessThanOrEqualTo(challenge.get("minFilterValue"), userMax),
                        cb.greaterThanOrEqualTo(challenge.get("maxFilterValue"), userMin)));
            }
        }

        // 8. Sous-requêtes pour le tri par popularité : compter le nombre de
        // participants (tous utilisateurs) sur cette version
        Subquery<Long> participantsSubquery = cq.subquery(Long.class);
        Root<ChallengeAttemptEntity> attemptForPopularity = participantsSubquery.from(ChallengeAttemptEntity.class);
        participantsSubquery.select(cb.countDistinct(attemptForPopularity.get("user").get("id")))
                .where(cb.equal(attemptForPopularity.get("challengeVersion").get("id"), cvJoin.get("id")));

        // 9. Sous-requête pour compter les tentatives de l'utilisateur sur cette
        // version
        Subquery<Long> userAttemptsSubquery = cq.subquery(Long.class);
        Root<ChallengeAttemptEntity> userAttempt = userAttemptsSubquery.from(ChallengeAttemptEntity.class);
        userAttemptsSubquery.select(cb.count(userAttempt))
                .where(
                        cb.equal(userAttempt.get("challengeVersion").get("id"), cvJoin.get("id")),
                        cb.equal(userAttempt.get("user").get("id"), challengeMenu.getUserId()));

        // 10. Sous-requête pour obtenir le meilleur score de l'utilisateur sur cette
        // version
        Subquery<Integer> bestScoreSubquery = cq.subquery(Integer.class);
        Root<ChallengeAttemptEntity> attemptBest = bestScoreSubquery.from(ChallengeAttemptEntity.class);
        bestScoreSubquery.select(cb.max(attemptBest.get("correctAnswers")))
                .where(
                        cb.equal(attemptBest.get("challengeVersion").get("id"), cvJoin.get("id")),
                        cb.equal(attemptBest.get("user").get("id"), challengeMenu.getUserId()));

        // 11. Définir la sous-requête pour obtenir le meilleur temps de l'utilisateur
        // sur cette version
        Subquery<Long> bestTimeSubquery = cq.subquery(Long.class);
        Root<ChallengeAttemptEntity> attemptTimeForBest = bestTimeSubquery.from(ChallengeAttemptEntity.class);
        // Utiliser unix_timestamp pour obtenir les secondes depuis l'époque, puis
        // multiplier par 1000 pour obtenir des millisecondes
        Expression<Long> startTimestamp = cb.function("unix_timestamp", Long.class,
                attemptTimeForBest.get("attemptStart"));
        Expression<Long> endTimestamp = cb.function("unix_timestamp", Long.class, attemptTimeForBest.get("attemptEnd"));
        Expression<Long> diffSeconds = cb.diff(endTimestamp, startTimestamp);
        Expression<Long> bestTimeExpr = cb.prod(diffSeconds, cb.literal(1000L));

        bestTimeSubquery.select(cb.min(bestTimeExpr))
                .where(
                        cb.equal(attemptTimeForBest.get("challengeVersion").get("id"), cvJoin.get("id")),
                        cb.equal(attemptTimeForBest.get("user").get("id"), challengeMenu.getUserId()),
                        cb.equal(attemptTimeForBest.get("correctAnswers"), bestScoreSubquery));

        // 12. Filtre sur userPerformance
        if (challengeMenu.getFilters() != null &&
                challengeMenu.getFilters().getUserPerformances() != null &&
                !challengeMenu.getFilters().getUserPerformances().isEmpty()) {

            List<Predicate> userPerfPredicates = new ArrayList<>();

            // PAS_COMMENCE : l'utilisateur n'a jamais tenté cette version (userAttempts =
            // 0)
            if (challengeMenu.getFilters().getUserPerformances().contains(UserPerformance.PAS_COMMENCE)) {
                userPerfPredicates.add(cb.equal(cb.coalesce(userAttemptsSubquery, 0L), 0L));
            }
            // ACHEVE : l'utilisateur a tenté au moins une fois
            if (challengeMenu.getFilters().getUserPerformances().contains(UserPerformance.ACHEVE)) {
                userPerfPredicates.add(cb.greaterThan(cb.coalesce(userAttemptsSubquery, 0L), 0L));
            }
            // PAS_PARFAIT : l'utilisateur a tenté et son meilleur score est inférieur au
            // nombre de questions
            if (challengeMenu.getFilters().getUserPerformances().contains(UserPerformance.PAS_PARFAIT)) {
                userPerfPredicates.add(cb.and(
                        cb.greaterThan(cb.coalesce(userAttemptsSubquery, 0L), 0L),
                        cb.isNotNull(bestScoreSubquery),
                        cb.lt(bestScoreSubquery, cvJoin.get("questionCount"))));
            }
            // REUSSI : l'utilisateur a tenté et son meilleur score est égal au nombre de
            // questions
            if (challengeMenu.getFilters().getUserPerformances().contains(UserPerformance.REUSSI)) {
                userPerfPredicates.add(cb.and(
                        cb.greaterThan(cb.coalesce(userAttemptsSubquery, 0L), 0L),
                        cb.isNotNull(bestScoreSubquery),
                        cb.equal(bestScoreSubquery, cvJoin.get("questionCount"))));
            }
            // PODIUM : l'utilisateur est en podium si le nombre de tentatives avec une
            // performance meilleure
            // que la sienne (score supérieur, ou score égal et temps inférieur) est
            // inférieur à 3
            if (challengeMenu.getFilters().getUserPerformances().contains(UserPerformance.PODIUM)) {
                Subquery<Long> betterAttemptsSubquery = cq.subquery(Long.class);
                Root<ChallengeAttemptEntity> attemptBetter = betterAttemptsSubquery.from(ChallengeAttemptEntity.class);
                Expression<Integer> scoreComparison = attemptBetter.get("correctAnswers");

                // Calcul du temps en millisecondes en utilisant unix_timestamp
                Expression<Long> startTs = cb.function("unix_timestamp", Long.class, attemptBetter.get("attemptStart"));
                Expression<Long> endTs = cb.function("unix_timestamp", Long.class, attemptBetter.get("attemptEnd"));
                Expression<Long> diff = cb.diff(endTs, startTs);
                Expression<Long> timeComparison = cb.prod(diff, cb.literal(1000L));

                betterAttemptsSubquery.select(cb.count(attemptBetter))
                        .where(
                                cb.equal(attemptBetter.get("challengeVersion").get("id"), cvJoin.get("id")),
                                cb.or(
                                        cb.greaterThan(scoreComparison, bestScoreSubquery),
                                        cb.and(
                                                cb.equal(scoreComparison, bestScoreSubquery),
                                                cb.lessThan(timeComparison, bestTimeSubquery))));
                userPerfPredicates.add(cb.lt(betterAttemptsSubquery, 3L));
            }

            predicates.add(cb.or(userPerfPredicates.toArray(new Predicate[0])));
        }

        cq.where(cb.and(predicates.toArray(new Predicate[0])));

        // 13. Sous-requêtes supplémentaires pour les attempts (bestAttemptStart)
        Subquery<LocalDateTime> bestAttemptStartSubquery = cq.subquery(LocalDateTime.class);
        Root<ChallengeAttemptEntity> attemptStart = bestAttemptStartSubquery.from(ChallengeAttemptEntity.class);
        bestAttemptStartSubquery.select(cb.greatest(attemptStart.get("attemptStart").as(LocalDateTime.class)))
                .where(
                        cb.equal(attemptStart.get("challengeVersion").get("id"), cvJoin.get("id")),
                        cb.equal(attemptStart.get("user").get("id"), challengeMenu.getUserId()),
                        cb.equal(attemptStart.get("correctAnswers"), bestScoreSubquery));

        // 14. Tri dynamique basé sur les critères de tri de ChallengeMenu
        List<Order> orders = new ArrayList<>();
        if (challengeMenu.getSorts() != null && !challengeMenu.getSorts().isEmpty()) {
            for (ChallengeSortCriterion sortCriterion : challengeMenu.getSorts()) {
                switch (sortCriterion.getSortType()) {
                    case CREATION_DATE:
                        orders.add(
                                sortCriterion.getOrder() == OrderDirection.ASC ? cb.asc(challenge.get("creationDate"))
                                        : cb.desc(challenge.get("creationDate")));
                        break;
                    case POPULARITY:
                        orders.add(sortCriterion.getOrder() == OrderDirection.ASC
                                ? cb.asc(cb.coalesce(participantsSubquery, 0L))
                                : cb.desc(cb.coalesce(participantsSubquery, 0L)));
                        break;
                    case LENGTH:
                        orders.add(sortCriterion.getOrder() == OrderDirection.ASC ? cb.asc(cvJoin.get("questionCount"))
                                : cb.desc(cvJoin.get("questionCount")));
                        break;
                    case PERFORMANCE:
                        Expression<Integer> participated = cb.<Integer>selectCase()
                                .when(cb.isNull(bestScoreSubquery), 0)
                                .otherwise(1);
                        Expression<Double> normalizedScore = (Expression<Double>) (Expression<?>) cb.quot(
                                bestScoreSubquery.as(Double.class),
                                cvJoin.get("questionCount").as(Double.class));
                        Expression<Double> normalizedTime = (Expression<Double>) (Expression<?>) cb.quot(
                                bestTimeSubquery.as(Double.class),
                                cvJoin.get("questionCount").as(Double.class));
                        if (sortCriterion.getOrder() == OrderDirection.ASC) {
                            orders.add(cb.asc(participated)); // Les challenges non tentés (0) en premier
                            orders.add(cb.asc(normalizedScore)); // Puis score normalisé croissant
                            orders.add(cb.desc(normalizedTime)); // Puis temps normalisé décroissant (temps court =
                                                                 // mieux)
                        } else {
                            orders.add(cb.desc(participated)); // Les challenges tentés (1) en premier
                            orders.add(cb.desc(normalizedScore)); // Puis score normalisé décroissant
                            orders.add(cb.asc(normalizedTime)); // Puis temps normalisé croissant (temps court = mieux)
                        }
                        break;
                    default:
                        break;
                }
            }
        }
        if (!orders.isEmpty()) {
            cq.orderBy(orders);
        }

        // 15. Construction de la sélection avec alias
        cq.multiselect(
                challenge.get("id").alias("challenge_id"),
                challenge.get("description").alias("description"),
                challenge.get("creationDate").alias("creation_date"),
                challenge.get("minFilterValue").alias("min_filter_value"),
                challenge.get("maxFilterValue").alias("max_filter_value"),
                cvJoin.get("id").alias("challenge_version_id"),
                cvJoin.get("versionNumber").alias("version_number"),
                cvJoin.get("startDate").alias("version_start_date"),
                cvJoin.get("endDate").alias("version_end_date"),
                cvJoin.get("questionCount").alias("question_count"),
                cb.coalesce(participantsSubquery, 0L).alias("nb_participants"),
                bestScoreSubquery.alias("best_question_score"),
                bestTimeSubquery.alias("best_time_ms"),
                attrJoin.get("id").alias("filter_attribute_id"),
                attrJoin.get("attributeName").alias("attribute_name"),
                attrJoin.get("type").alias("filter_type"),
                gmJoin.get("id").alias("game_mode_id"),
                gmJoin.get("gameModeTitle").alias("game_mode_title"),
                gmJoin.get("gameModeDescription").alias("game_mode_description"),
                challenge.get("creator").get("id").alias("creator_id"),
                challenge.get("creator").get("username").alias("creator_username"),
                bestAttemptStartSubquery.alias("attempt_start_date"));

        List<Tuple> tuples = entityManager.createQuery(cq).getResultList();
        List<ChallengeCardProjection> result = new ArrayList<>();
        for (Tuple tuple : tuples) {
            ChallengeCardProjection projection = new ChallengeCardProjection() {
                @Override
                public Long getChallengeId() {
                    return tuple.get("challenge_id", Long.class);
                }

                @Override
                public String getDescription() {
                    return tuple.get("description", String.class);
                }

                @Override
                public LocalDateTime getCreationDate() {
                    return tuple.get("creation_date", LocalDateTime.class);
                }

                @Override
                public String getMinFilterValue() {
                    return tuple.get("min_filter_value", String.class);
                }

                @Override
                public String getMaxFilterValue() {
                    return tuple.get("max_filter_value", String.class);
                }

                @Override
                public Long getChallengeVersionId() {
                    return tuple.get("challenge_version_id", Long.class);
                }

                @Override
                public Integer getVersionNumber() {
                    return tuple.get("version_number", Integer.class);
                }

                @Override
                public LocalDateTime getVersionStartDate() {
                    return tuple.get("version_start_date", LocalDateTime.class);
                }

                @Override
                public LocalDateTime getVersionEndDate() {
                    return tuple.get("version_end_date", LocalDateTime.class);
                }

                @Override
                public Integer getQuestionCount() {
                    return tuple.get("question_count", Integer.class);
                }

                @Override
                public Long getNbParticipants() {
                    return tuple.get("nb_participants", Long.class);
                }

                @Override
                public Integer getBestQuestionScore() {
                    return tuple.get("best_question_score", Integer.class);
                }

                @Override
                public Long getBestTimeMs() {
                    return tuple.get("best_time_ms", Long.class);
                }

                @Override
                public Long getFilterAttributeId() {
                    return tuple.get("filter_attribute_id", Long.class);
                }

                @Override
                public String getAttributeName() {
                    return tuple.get("attribute_name", String.class);
                }

                @Override
                public AttributeType getFilterType() {
                    return tuple.get("filter_type", AttributeType.class);
                }

                @Override
                public Long getGameModeId() {
                    return tuple.get("game_mode_id", Long.class);
                }

                @Override
                public String getGameModeTitle() {
                    return tuple.get("game_mode_title", String.class);
                }

                @Override
                public String getGameModeDescription() {
                    return tuple.get("game_mode_description", String.class);
                }

                @Override
                public Long getCreatorId() {
                    return tuple.get("creator_id", Long.class);
                }

                @Override
                public String getCreatorUsername() {
                    return tuple.get("creator_username", String.class);
                }

                @Override
                public LocalDateTime getAttemptStartDate() {
                    return tuple.get("attempt_start_date", LocalDateTime.class);
                }
            };
            result.add(projection);
        }
        return result;
    }

}
