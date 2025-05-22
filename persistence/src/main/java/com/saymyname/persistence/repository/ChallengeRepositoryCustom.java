package com.saymyname.persistence.repository;

import java.util.List;

import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.persistence.projection.ChallengeCardProjection;

public interface ChallengeRepositoryCustom {
    List<ChallengeCardProjection> findChallengeCards(ChallengeMenu challengeMenu);
}
