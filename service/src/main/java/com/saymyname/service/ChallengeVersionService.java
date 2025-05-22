package com.saymyname.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.persistence.dao.ChallengeQuestionDao;
import com.saymyname.persistence.dao.ChallengeVersionDao;

@Service
public class ChallengeVersionService {
    private final ChallengeVersionDao challengeVersionDao;
    private final ChallengeQuestionDao challengeQuestionDao;

    public ChallengeVersionService(ChallengeVersionDao challengeVersionDao, ChallengeQuestionDao challengeQuestionDao) {
        this.challengeQuestionDao = challengeQuestionDao;
        this.challengeVersionDao = challengeVersionDao;
    }

    public ChallengeVersion createChallengeVersionAndQuestions(ChallengeVersion challengeVersion, String nextFilterMax,
            LocalDateTime nextSeasonStartDate) {
        // Insert version
        ChallengeVersion savedVersion = challengeVersionDao.saveChallengeVersion(challengeVersion);

        challengeQuestionDao.saveQuestionsOfVersion(savedVersion.getChallenge().getId(), nextFilterMax,
                nextSeasonStartDate, savedVersion.getId());

        return savedVersion;
    }

}
