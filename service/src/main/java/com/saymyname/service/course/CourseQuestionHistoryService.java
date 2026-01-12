// src/main/java/com/saymyname/service/course/CourseQuestionHistoryService.java
package com.saymyname.service.course;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.CourseQuestionItem;
import com.saymyname.core.model.enums.course.QuizQuestionItemRole;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.service.PersonAttributeService;
import com.saymyname.persistence.dao.course.CourseQuestionHistoryDao;

@Service
public class CourseQuestionHistoryService {

    private final CourseQuestionHistoryDao dao;
    private final PersonAttributeService personAttributeService;

    public CourseQuestionHistoryService(
            CourseQuestionHistoryDao dao,
            PersonAttributeService personAttributeService) {
        this.dao = dao;
        this.personAttributeService = personAttributeService;
    }

    public CourseQuestionHistory create(CourseQuestionHistory history) {
        return dao.create(history);
    }

    public CourseQuestionHistory findById(Long id) {
        return dao.findById(id);
    }

    public CourseQuestionHistory findByIdAndMarkHelpUsed(Long questionId) {
        dao.markHelpUsed(questionId);
        return findById(questionId);
    }

    public void deleteAllByCourse(Course course) {
        dao.deleteAllByCourse(course);
    }

    public void updateAnswerMetaAndItems(CourseQuestionHistory courseQuestion) {
        dao.updateAnswerMeta(courseQuestion);

        boolean hasTargets = courseQuestion.getItems() != null && courseQuestion.getItems().stream()
                .anyMatch(it -> it.getRole() == QuizQuestionItemRole.TARGET);
        if (hasTargets) {
            dao.updateTargetItemsAnswerMeta(
                    courseQuestion.getId(),
                    true,
                    courseQuestion.isGlobalCorrect(),
                    courseQuestion.getNormalizedSubmission(),
                    true);
        }
    }

    @Transactional
    public List<PersonAttribute> markHelpAndGetAttributes(Long courseId, Long questionId) {
        if (courseId == null)
            throw new IllegalArgumentException("courseId cannot be null");
        if (questionId == null)
            throw new IllegalArgumentException("questionId cannot be null");

        // 1) Marquer l’aide utilisée
        CourseQuestionHistory questionMarked = findByIdAndMarkHelpUsed(questionId);
        if (questionMarked == null) {
            throw new IllegalArgumentException("Question not found: " + questionId);
        }

        // 1bis) Guard : cohérence course
        if (questionMarked.getCourse() == null || questionMarked.getCourse().getId() == null) {
            throw new IllegalStateException("Question " + questionId + " has no course reference");
        }
        if (!courseId.equals(questionMarked.getCourse().getId())) {
            throw new IllegalArgumentException(
                    "Question " + questionId + " does not belong to course " + courseId);
        }

        // 2) Déterminer la personne cible (robuste aux futurs formats)
        Long personId = extractTargetPersonId(questionMarked);

        return personAttributeService.getAttributesByPersonId(personId);
    }

    private Long extractTargetPersonId(CourseQuestionHistory history) {
        // ✅ Priorité 0 : snapshot (robuste multi-format)
        if (history.getSnapshot() != null && history.getSnapshot().getTargetPersonIds() != null) {
            List<Long> ids = history.getSnapshot().getTargetPersonIds();
            if (!ids.isEmpty() && ids.get(0) != null) {
                return ids.get(0); // pour "help", on prend le 1er (primary)
            }
        }

        // Fallback legacy : items
        List<CourseQuestionItem> items = history.getItems();
        if (items == null || items.isEmpty()) {
            throw new IllegalStateException("No items found for question " + history.getId());
        }

        Long personId = items.stream()
                .filter(it -> it.getRole() == QuizQuestionItemRole.TARGET)
                .map(it -> it.getPerson() != null ? it.getPerson().getId() : null)
                .filter(id -> id != null)
                .findFirst()
                .orElse(null);

        if (personId != null)
            return personId;

        personId = items.stream()
                .filter(it -> it.getRole() == QuizQuestionItemRole.TARGET)
                .map(it -> (it.getKnowledge() != null
                        && it.getKnowledge().getPerson() != null)
                                ? it.getKnowledge().getPerson().getId()
                                : null)
                .filter(id -> id != null)
                .findFirst()
                .orElse(null);

        if (personId != null)
            return personId;

        throw new IllegalStateException(
                "Unable to resolve TARGET personId for question " + history.getId()
                        + " (snapshot has no targetPersonIds and items don't expose target person)");
    }

    // ------- Stats activité -------
    public int countAllAnswersByCourse(Course course) {
        return dao.countAllAnswersByCourse(course);
    }

    public int countAnswersSince(Course course, LocalDateTime since) {
        return dao.countAnswersSince(course, since);
    }

    public LocalDateTime findLastAnsweredAt(Course course) {
        return dao.findLastAnsweredAt(course);
    }

    @Transactional
    public void markHelpUsed(Long id) {
        dao.markHelpUsed(id);
    }
}
