// src/main/java/com/saymyname/service/quiz/plugins/QuizQuestionPlugin.java
package com.saymyname.service.quiz.plugins;

import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.quiz.QuizAnswerSubmission; // ✅ CORE
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionSpec;
import com.saymyname.core.model.quiz.QuizValidationResult;
import com.saymyname.core.model.quiz.answer.NormalizedSubmission;
import com.saymyname.core.model.quiz.snapshot.QuizQuestionSnapshot;

public interface QuizQuestionPlugin {

    QuizFormat supports();

    QuizQuestion build(QuizQuestionSpec spec);

    NormalizedSubmission normalize(
            QuizQuestion question,
            QuizAnswerSubmission submission);

    QuizValidationResult validate(
            QuizQuestionSnapshot snapshot,
            QuizAnswerSubmission submission,
            NormalizedSubmission normalized);
}
