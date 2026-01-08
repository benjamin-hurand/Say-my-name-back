// src/main/java/com/saymyname/service/quiz/plugins/PluginSupport.java
package com.saymyname.service.quiz.plugins;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import com.saymyname.core.model.course.ResultAttribute;
import com.saymyname.core.model.enums.quiz.QuizFormat;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.quiz.QuizFollowUp;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.model.quiz.QuizQuestionContext;
import com.saymyname.core.model.quiz.QuizQuestionDisplay;
import com.saymyname.core.model.quiz.QuizQuestionHints;
import com.saymyname.core.model.quiz.QuizQuestionPayload;
import com.saymyname.core.model.quiz.QuizQuestionSpec;

final class PluginSupport {

    private PluginSupport() {
    }

    static String canonicalizeText(String s) {
        if (s == null)
            return null;
        String t = s.trim();
        if (t.isEmpty())
            return null;

        // lowercase
        t = t.toLowerCase(Locale.ROOT);

        // remove diacritics
        t = Normalizer.normalize(t, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        // collapse whitespace
        t = t.replaceAll("\\s+", " ").trim();

        return t.isEmpty() ? null : t;
    }

    static ResultAttribute resultAttr(Long attributeId, String value, boolean correct, boolean target) {
        // Placeholder Attribute (id + name) pour rester compatible DTO sans lookup DB
        Attribute a = new Attribute.Builder()
                .withId(attributeId)
                .withName(attributeId == null ? null : ("attribute#" + attributeId))
                .build();

        return new ResultAttribute(a, value, correct, target);
    }

    static boolean equalsCanon(String a, String b) {
        String ca = canonicalizeText(a);
        String cb = canonicalizeText(b);
        return Objects.equals(ca, cb);
    }

    static QuizQuestion baseQuestion(
            QuizQuestionSpec spec,
            QuizFormat format,
            QuizQuestionPayload payload,
            QuizQuestionHints hints,
            QuizQuestionDisplay display,
            QuizFollowUp followUp) {

        Objects.requireNonNull(spec, "spec");
        Objects.requireNonNull(format, "format");

        QuizQuestionContext ctx = Objects.requireNonNull(spec.getContext(), "spec.context");

        if (Boolean.TRUE.equals(spec.getTimed()) && spec.getTimeLimitMs() == null) {
            throw new IllegalStateException("timed=true requires timeLimitMs in QuizQuestionSpec");
        }

        QuizQuestionDisplay resolvedDisplay = display;
        if (spec.getTimed() != null || spec.getTimeLimitMs() != null) {
            if (resolvedDisplay == null) {
                resolvedDisplay = new QuizQuestionDisplay.Builder()
                        .withTimed(spec.getTimed())
                        .withTimeLimitMs(spec.getTimeLimitMs())
                        .build();
            } else {
                resolvedDisplay.setTimed(spec.getTimed());
                resolvedDisplay.setTimeLimitMs(spec.getTimeLimitMs());
            }
        }

        return new QuizQuestion.Builder()
                .withPersonId(spec.getPersonId())
                .withStorageKey(spec.getStorageKey())
                .withGameModeId(spec.getGameModeId())
                .withTargetAttributeIds(spec.getTargetAttributeIds())
                .withOperator(spec.getOperator())
                .withContext(ctx)
                .withFormat(format)
                .withPayload(payload)
                .withHints(hints)
                .withDisplay(resolvedDisplay)
                .withFollowUp(followUp)
                .build();
    }

    static QuizQuestionHints hintsFromSpec(QuizQuestionSpec spec) {
        String initials = spec.getInitials();
        if (initials == null || initials.isBlank())
            return null;
        return new QuizQuestionHints(initials);
    }

    static List<Long> poolIdsLimited(QuizQuestionSpec spec, int limit, Long excludePersonId) {
        List<Long> src = spec.getCandidatePoolPersonIds();
        if (src == null || src.isEmpty())
            return List.of();

        List<Long> out = new ArrayList<>(Math.min(limit, src.size()));
        for (Long id : src) {
            if (id == null)
                continue;
            if (excludePersonId != null && excludePersonId.equals(id))
                continue;
            out.add(id);
            if (out.size() >= limit)
                break;
        }
        return Collections.unmodifiableList(out);
    }

    static String maskifyAlphaNum(String s) {
        if (s == null || s.isBlank())
            return "";
        StringBuilder b = new StringBuilder(s.length());
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c))
                b.append('_');
            else
                b.append(c);
        }
        return b.toString();
    }
}
