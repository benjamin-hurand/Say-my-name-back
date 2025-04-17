package com.saymyname.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.atn.SemanticContext.Operator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.ChallengeAttemptException;
import com.saymyname.core.model.challenge.Challenge;
import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.core.model.challenge.ChallengeEvaluation;
import com.saymyname.core.model.challenge.ChallengeEvaluationRequest;
import com.saymyname.core.model.challenge.ChallengeHistoryEntry;
import com.saymyname.core.model.challenge.ChallengeQuestion;
import com.saymyname.core.model.challenge.ChallengeVersion;
import com.saymyname.core.model.challenge.CorrectionEntry;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.game.options.GameModeAttribute;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.util.AnswerValidator;
import com.saymyname.persistence.dao.ChallengeAttemptDao;
import com.saymyname.persistence.dao.ChallengeQuestionDao;
import com.saymyname.persistence.dao.PersonAttributeDao;

@Service
public class ChallengeAttemptService {

    private final ChallengeAttemptDao challengeAttemptDao;
    private final ChallengeQuestionDao challengeQuestionDao;
    private final PersonAttributeDao personAttributeDao;

    public ChallengeAttemptService(ChallengeAttemptDao challengeAttemptDao, ChallengeQuestionDao challengeQuestionDao,
            PersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
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
        // on récupère et initialise version+questions
        ChallengeAttempt attempt = challengeAttemptDao.findById(id);
        if (attempt != null && attempt.getChallengeVersion() != null) {
            List<ChallengeQuestion> questions = attempt.getChallengeVersion().getQuestions();
            // on s’assure qu’on a bien une liste mutable
            questions = new ArrayList<>(questions);
            // on mélange
            Collections.shuffle(questions);
            // puis on remet dans le modèle
            attempt.getChallengeVersion().setQuestions(questions);
        }
        return attempt;
    }

    public void startChallengeAttempt(Long id) throws ChallengeAttemptException {
        challengeAttemptDao.startAttempt(id);
    }

    public void stopChallengeAttempt(Long id) throws ChallengeAttemptException {
        challengeAttemptDao.stopAttempt(id);
    }

    @Transactional
    public ChallengeEvaluation evaluateChallengeAttempt(Long attemptId,
            ChallengeEvaluationRequest request) {
        // 1. Charger la tentative
        ChallengeAttempt attempt = challengeAttemptDao.findById(attemptId);
        if (attempt == null) {
            throw new ChallengeAttemptException("Tentative non trouvée pour id=" + attemptId);
        }

        // 2. Récupérer le game mode et ses attributs
        GameMode gameMode = attempt.getChallengeVersion()
                .getChallenge()
                .getGameMode();
        String operator = gameMode.getOperator(); // "AND" ou "OR"
        List<Long> modeAttrIds = gameMode.getGameModeAttributes()
                .stream()
                .map(gma -> gma.getAttribute().getId())
                .collect(Collectors.toList());

        List<CorrectionEntry> entries = new ArrayList<>();

        // 3. Pour chaque réponse utilisateur
        for (ChallengeHistoryEntry userEntry : request.getHistory()) {
            Long personId = userEntry.getPerson().getId();
            String userAnswer = userEntry.getAnswer();
            Integer questionNumber = userEntry.getQuestionNumber();

            // 3a. Récupérer les attributs de la personne
            List<PersonAttribute> personAttrs = personAttributeDao.findAttributesByPersonId(personId);

            // 3b. Filtrer ceux qui sont dans le game mode
            List<String> correctValues = personAttrs.stream()
                    .filter(pa -> modeAttrIds.contains(pa.getAttribute().getId()))
                    .map(PersonAttribute::getValue)
                    .collect(Collectors.toList());

            // 3c. Vérifier la réponse
            boolean isCorrect = AnswerValidator.match(userAnswer, correctValues, operator, true);
            String correctAnswer = String.join(" ", correctValues);

            // 3d. Créer l’entrée d’évaluation
            entries.add(new CorrectionEntry.Builder().withQuestionNumber(questionNumber)
                    .withCorrectAnswer(correctAnswer)
                    .withIsCorrect(isCorrect).build());
        }

        // 4. Calcul du score total
        int totalCorrect = (int) entries.stream()
                .filter(e -> e.isCorrect())
                .count();

        // 5. Sauvegarde de l'évaluation dans la tentative
        challengeAttemptDao.updateCorrectAnswers(attemptId, totalCorrect);

        return new ChallengeEvaluation.Builder()
                .withTotalCorrect(totalCorrect)
                .withEntries(entries)
                .build();
    }

}
