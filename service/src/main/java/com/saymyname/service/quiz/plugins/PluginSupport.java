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
import com.saymyname.core.model.quiz.QuizPayloadItem;

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
        if (attributeId == null) {
            throw new IllegalStateException("ResultAttribute.attributeId is required");
        }

        // Placeholder Attribute (id + name) pour rester compatible DTO sans lookup DB
        Attribute a = new Attribute.Builder()
                .withId(attributeId)
                .withName("attribute#" + attributeId)
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
                .withReasonCode(spec.getReasonCode())
                .withReasonDetailsJson(spec.getReasonDetailsJson())
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

    static List<QuizPayloadItem> poolItemsLimited(QuizQuestionSpec spec, int limit, Long excludePersonId) {
        List<QuizPayloadItem> src = spec.getCandidatePoolItems();
        if (src != null && !src.isEmpty()) {
            List<QuizPayloadItem> out = new ArrayList<>(Math.min(limit, src.size()));
            for (QuizPayloadItem item : src) {
                if (item == null)
                    continue;
                Long id = item.getPersonId();
                if (id == null)
                    continue;
                if (excludePersonId != null && excludePersonId.equals(id))
                    continue;
                String labelId = item.getLabelId() != null ? item.getLabelId() : String.valueOf(id);
                out.add(new QuizPayloadItem.Builder()
                        .withPersonId(id)
                        .withStorageKey(item.getStorageKey())
                        .withLabelId(labelId)
                        .build());
                if (out.size() >= limit)
                    break;
            }
            return Collections.unmodifiableList(out);
        }

        List<Long> ids = poolIdsLimited(spec, limit, excludePersonId);
        if (ids.isEmpty())
            return List.of();

        List<QuizPayloadItem> out = new ArrayList<>(ids.size());
        for (Long id : ids) {
            if (id == null)
                continue;
            out.add(new QuizPayloadItem.Builder()
                    .withPersonId(id)
                    .withStorageKey(null)
                    .withLabelId(String.valueOf(id))
                    .build());
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

    static long deterministicSeedFromSpec(QuizQuestionSpec spec) {
        if (spec == null)
            return 0L;
        var ctx = spec.getContext();
        int round = (ctx != null && ctx.getQuestionRound() != null) ? ctx.getQuestionRound() : 0;
        Long personId = spec.getPersonId();
        Long gameModeId = spec.getGameModeId();
        return java.util.Objects.hash(personId, gameModeId, round);
    }

    /**
     * Cloze UX-friendly: masque la plupart des caractères alphanum,
     * mais révèle quelques lettres/chiffres de façon déterministe.
     *
     * Exemples:
     * - "Abarna" => "A____a"
     * - "Jean Dupont" => "J__n D_____"
     */
    static String clozeMaskRevealSome(String answer, long seed) {
        if (answer == null || answer.isBlank()) {
            return "";
        }

        // On travaille sur la string originale pour préserver espaces/ponctuation
        char[] src = answer.toCharArray();
        int n = src.length;

        // Indices des caractères alphanumériques (ceux qu'on peut masquer/révéler)
        java.util.List<Integer> alphaNumIdx = new java.util.ArrayList<>();
        for (int i = 0; i < n; i++) {
            if (Character.isLetterOrDigit(src[i])) {
                alphaNumIdx.add(i);
            }
        }

        if (alphaNumIdx.isEmpty()) {
            return answer; // rien à masquer
        }

        // Règle simple et stable:
        // - révèle toujours le premier caractère alphanumérique
        // - révèle souvent le dernier si longueur >= 6
        // - révèle 1 caractère alphanum additionnel par ~4 chars (capé)
        java.util.Random rnd = new java.util.Random(seed);

        boolean[] reveal = new boolean[n];

        // Reveal first
        reveal[alphaNumIdx.get(0)] = true;

        // Reveal last for longer answers
        if (alphaNumIdx.size() >= 6) {
            reveal[alphaNumIdx.get(alphaNumIdx.size() - 1)] = true;
        }

        int extraToReveal = Math.min(2, Math.max(0, (alphaNumIdx.size() / 4) - 1));
        // Randomly reveal extra characters (excluding already revealed)
        for (int k = 0; k < extraToReveal; k++) {
            // Try a few draws to find a not-yet-revealed index
            for (int tries = 0; tries < 8; tries++) {
                int pick = alphaNumIdx.get(rnd.nextInt(alphaNumIdx.size()));
                if (!reveal[pick]) {
                    reveal[pick] = true;
                    break;
                }
            }
        }

        StringBuilder out = new StringBuilder(n);
        for (int i = 0; i < n; i++) {
            char c = src[i];
            if (Character.isLetterOrDigit(c) && !reveal[i]) {
                out.append('_');
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }
}
