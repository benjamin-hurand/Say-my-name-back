package com.saymyname.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.exception.ChallengeAttemptException;
import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.challenge.ChallengeAttempt;
import com.saymyname.core.model.challenge.ChallengeEvaluation;
import com.saymyname.core.model.challenge.ChallengeEvaluationRequest;
import com.saymyname.core.model.challenge.ChallengeHistoryEntry;
import com.saymyname.core.model.challenge.ChallengeQuestion;
import com.saymyname.core.model.challenge.CorrectionEntry;
import com.saymyname.core.model.enums.AttemptStatus;
import com.saymyname.core.model.game.options.GameMode;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.core.util.AnswerValidator;
import com.saymyname.persistence.dao.ChallengeAttemptDao;
import com.saymyname.persistence.dao.PersonAttributeDao;
import com.saymyname.persistence.entity.ChallengeAttemptEntity;

@Service
public class ChallengeAttemptService {

    private final ChallengeAttemptDao challengeAttemptDao;
    private final PersonAttributeDao personAttributeDao;

    public ChallengeAttemptService(ChallengeAttemptDao challengeAttemptDao, PersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
        this.challengeAttemptDao = challengeAttemptDao;
    }

    @Transactional
    public ChallengeAttempt createChallengeAttempt(ChallengeAttempt challengeAttempt) {
        // Sauvegarde de la nouvelle tentative
        ChallengeAttempt savedAttempt = challengeAttemptDao.createChallengeAttempt(challengeAttempt);

        return getChallengeAttemptById(savedAttempt.getId());
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

    public boolean verifyUserCanAttempt(User user, ChallengeAttempt attempt) {
        return challengeAttemptDao.verifyUserCanAttempt(user.getId(), attempt.getId());
    }

    public void markAbandoned(Long id) {
        challengeAttemptDao.markAbandoned(id);
    }

    /**
     * Marque toutes les tentatives IN_PROGRESS depuis plus de 24 h comme ABANDONED.
     */
    @Transactional
    public void purgeStaleAttempts() {
        // On définit 24h comme durée “maxAge”
        Duration maxAge = Duration.ofHours(24);
        List<ChallengeAttemptEntity> stale = challengeAttemptDao.findStaleAttempts(maxAge);

        for (ChallengeAttemptEntity a : stale) {
            a.setAttemptEnd(LocalDateTime.now());
            a.setStatus(AttemptStatus.ABANDONED);
        }

        challengeAttemptDao.saveAll(stale);
    }

}
