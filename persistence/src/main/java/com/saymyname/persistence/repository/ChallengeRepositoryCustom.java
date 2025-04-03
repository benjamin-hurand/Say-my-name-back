package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.ChallengeEntity;
import com.saymyname.persistence.projection.ChallengeCardProjection;

import jakarta.persistence.Tuple;

import com.saymyname.core.model.challenge.ChallengeMenu;
import java.util.List;

public interface ChallengeRepositoryCustom {
    List<ChallengeCardProjection> findChallengeCards(ChallengeMenu challengeMenu);
}
