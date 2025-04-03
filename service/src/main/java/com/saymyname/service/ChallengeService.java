package com.saymyname.service;

import java.util.List;
import org.springframework.stereotype.Service;
import com.saymyname.persistence.dao.ChallengeDao;
import com.saymyname.persistence.projection.ChallengeCardProjection;
import com.saymyname.core.exception.ChallengeAlreadyExistsException;
import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeCard;
import com.saymyname.core.model.challenge.ChallengeMenu;
import com.saymyname.service.ChallengeService;

@Service
public class ChallengeService {

    private final ChallengeDao challengeDao;

    public ChallengeService(ChallengeDao challengeDao) {
        this.challengeDao = challengeDao;
    }

    public List<ChallengeCardProjection> getChallengesList(ChallengeMenu challengeMenu) {
        return challengeDao.getChallengeCards(challengeMenu);
    }

    public Challenge saveChallenge(Challenge challenge) {
        if (challengeExists(challenge)) {
            throw new ChallengeAlreadyExistsException("Un challenge avec ce mode et ce filtre existe déjà.");
        }
        return challengeDao.saveChallenge(challenge);
    }

    public Boolean challengeExists(Challenge challenge) {
        return challengeDao.challengeExists(
                challenge.getGameMode().getId(),
                challenge.getFilterAttribute().getId(),
                challenge.getFilterAttribute().getMinValue(),
                challenge.getFilterAttribute().getMaxValue()
            );
    }
}
