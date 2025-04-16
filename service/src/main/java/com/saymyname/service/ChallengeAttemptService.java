package com.saymyname.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.core.model.challenge.ChallengeQuestion;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.persistence.dao.ChallengeAttemptDao;
import com.saymyname.persistence.dao.ChallengeQuestionDao;

@Service
public class ChallengeAttemptService {

    private final ChallengeAttemptDao challengeAttemptDao;
    private final ChallengeQuestionDao challengeQuestionDao;

    public ChallengeAttemptService(ChallengeAttemptDao challengeAttemptDao, ChallengeQuestionDao challengeQuestionDao) {
        this.challengeAttemptDao = challengeAttemptDao;
        this.challengeQuestionDao = challengeQuestionDao;
    }

    @Transactional
    public ChallengeAttempt createChallengeAttempt(ChallengeAttempt challengeAttempt) {
        // Sauvegarde de la tentative
        ChallengeAttempt savedAttempt = challengeAttemptDao.createChallengeAttempt(challengeAttempt);

        // Récupération de la version associée
        ChallengeVersion version = savedAttempt.getChallengeVersion();
        if (version != null && version.getId() > 0) {
            // Récupérer les questions associées via le repository
            List<ChallengeQuestion> rawQuestions = challengeQuestionDao.findByVersionId(version.getId());

            // Copier dans une liste modifiable
            List<ChallengeQuestion> questions = new ArrayList<>(rawQuestions);

            // Mélanger les questions
            Collections.shuffle(questions);

            // Mettre à jour la version dans le modèle avec ces questions mélangées
            version.setQuestions(questions);

            // Possiblement, mettre à jour le challengeAttempt si nécessaire :
            savedAttempt.setChallengeVersion(version);
        }
        return savedAttempt;
    }

    @Transactional(readOnly = true)
    public ChallengeAttempt getChallengeAttemptById(Long id) {
        return challengeAttemptDao.findById(id);
    }
}
