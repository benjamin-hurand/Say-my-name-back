package com.saymyname.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.persistence.entity.ChallengeQuestionEntity;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChallengeQuestionRepository extends JpaRepository<ChallengeQuestionEntity, Long> {

    // Insère les questions pour une version donnée
    @Modifying
    @Transactional
    @Query(value = "INSERT INTO challenge_questions (version_id, person_id) " +
            "SELECT DISTINCT cv.id, p.id " +
            "FROM challenges c " +
            "JOIN challenge_versions cv ON cv.challenge_id = c.id " +
            "JOIN persons_attributes pa ON pa.attribute_id = c.filter_id " +
            "JOIN persons p ON p.id = pa.person_id " +
            "WHERE c.id = ?1 " +
            "  AND pa.value >= c.min_filter_value " +
            "  AND pa.value < ?2 " +
            "  AND pa.valid_from <= ?3 " +
            "  AND (pa.valid_to IS NULL OR pa.valid_to >= ?3) " +
            "  AND cv.id = ?4", nativeQuery = true)
    int insertChallengeQuestions(Long challengeId, String nextFilterMax, LocalDateTime seasonStart, Long versionId);

    @Query("SELECT q FROM ChallengeQuestionEntity q WHERE q.version.id = :versionId")
    List<ChallengeQuestionEntity> findByVersionId(@Param("versionId") Long versionId);

    @Query("""
            SELECT q
            FROM ChallengeQuestionEntity q
            JOIN FETCH q.version v
            JOIN FETCH v.challenge c
            JOIN FETCH q.person p
            JOIN FETCH p.attributes a
            WHERE v.id = :versionId
            """)
    List<ChallengeQuestionEntity> findByVersionWithAll(@Param("versionId") Long versionId);
}
