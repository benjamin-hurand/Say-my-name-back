# Planning System

## But business
Le planning choisit un format et des candidats ensemble pour que la difficulte soit une fonction du couple
format + candidats (et pas du format seul). L objectif est d ajuster la difficulte percue tout en gardant un
parcours coherent (SRS, gate multi target, anti repetition).

## Entrees et sorties

### PlanningRequest (entree)
Champs importants:
- `userId`, `gameModeId`: identite de session et mode de quiz.
- `populationScope`, `gameOptions`: selection de population et filtres.
- `context`: PlanningContext (type, flags, formats autorises).
- `course`: present en mode course.
- `primaryKnowledge`, `selectedPoolType`: ancrage SRS pour course.
- `sessionStats`: streak, formatStreak, lastFormat, etc (si sessionTracking).
- `preferredFormat`, `requestedTimed`, `requestedTimeLimitMs`: preferences utilisateur.
- `lastPersonId`, `lastCorrect`: anti repetition et adaptation.

### PlanningContext (entree)
Flags et factories:
- `forCourse(courseId)`: SRS, knowledgeTracking, multiTarget, sessionTracking.
- `forTraining(trackKnowledge)`: pas de multi target, knowledgeTracking configurable.
- `forChallenge(challengeId, difficultyRange)`: formats limites, difficulte fixe.
- `forMinigame(minigameType, formats)`: formats limites, difficulte facile.
Champs cles:
- `contextType`, `allowedFormats`, `difficultyRange`.
- `multiTargetAllowed`, `srsEnabled`, `knowledgeTracking`, `sessionTracking`.

### QuestionPlan (sortie)
Garanties principales:
- `format` et `primaryCandidate` sont choisis ensemble (FormatAndCandidateSelector).
- `targetKnowledges` et `targetCount` sont coherents (multi target si besoin).
- `timed` et `timeLimitMs` sont configures selon DifficultyProfile.
- `reasonCode` et `reasonDetailsJson` expliquent la decision.
- `selectedDistractors` et `candidatePoolIds` sont coherents avec le format.

## Pipeline
1. LearnerStateAssessor: evalue le state (SRS, sessionStats, fatigue).
2. ObjectiveSelector: choisit l objectif (introduire, renforcer, pratiquer, etc).
3. DifficultyProfile: mappe objectif -> difficulte cible.
4. MultiTargetPlanner (course): gate multi target, selection de targets.
5. FormatAndCandidateSelector: format + candidats couples.
6. Timing: applique timed/timeLimitMs selon DifficultyProfile.
7. Build QuestionPlan: assemble la decision + reasonDetailsJson.
8. Build QuizQuestion: CourseQuizQuestionBuilder (course) ou QuizEngine (training)
   -> QuizQuestionFactory -> plugin.

## Invariants
- format et candidats sont couples (pas de format "isolé").
- training interdit le multi target (`PlanningContext.forTraining`).
- knowledgeTracking est respecte a la reponse (QuizEngine.shouldTrackKnowledge).
- course multi target seulement si le gate passe (MultiTargetPlanner).

## Responsabilites par classe
- `QuestionPlanningService`: orchestration du pipeline.
- `LearnerStateAssessor`: derive LearnerState depuis SRS et sessionStats.
- `ObjectiveSelector`: choisit l objectif pedagogique.
- `DifficultyProfile`: normalise difficulte + timing.
- `MultiTargetPlanner`: gate et selection multi target en course.
- `FormatAndCandidateSelector`: selection couple format + candidats.
- `CourseService`: construit PlanningRequest course, persiste attempt + plan.
- `CourseQuizQuestionBuilder`: construit la question course + snapshot.
- `QuizEngine`: flow training (emit/answer) + application de knowledgeTracking.
- `QuizQuestionFactory` + plugins: generation du QuizQuestion concret.
- `QuizQuestionSnapshotFactory`: freeze truth + snapshot.

## Scenarios

### Training normal
1. QuizEngine.emitTraining -> PlanningRequest (forTraining, trackKnowledge=true).
2. QuestionPlanningService -> QuestionPlan.
3. QuizEngine construit QuizQuestion + snapshot + handle token.

### Training trackKnowledge=false
1. PlanningContext.forTraining(false).
2. shouldTrackKnowledge(snapshot) -> false a la reponse.
3. Pas de write SRS/knowledge stats.

### Course single target
1. CourseService.buildPlan avec primaryKnowledge.
2. MultiTargetPlanner gate off -> targetCount=1.
3. CourseQuizQuestionBuilder construit la question et snapshot.

### Course multi target
1. CourseService.buildPlan avec primaryKnowledge + MultiTargetPlanner.
2. Gate passe -> targetCount > 1, format ORDERING/ASSOCIATION.
3. CourseQuizQuestionBuilder utilise targets pour construire la question.

## Debug
- `reasonDetailsJson`: details de decision (objective, format, target_count, pool_type).
- Logs WARN:
  - fallback format si MCQ sans distractors.
  - absence de candidat ou fallback training.
- Logs DEBUG/INFO selon services pour divergence ou conflits.
