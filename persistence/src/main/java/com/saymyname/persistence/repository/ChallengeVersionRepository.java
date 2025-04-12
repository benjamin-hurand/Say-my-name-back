package com.saymyname.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.saymyname.persistence.entity.ChallengeVersionEntity;

@Repository
public interface ChallengeVersionRepository extends JpaRepository<ChallengeVersionEntity, Long> {
}
