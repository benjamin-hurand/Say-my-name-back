package com.saymyname.service.course;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.persistence.dao.course.CourseQuestionHistoryDao;
import com.saymyname.service.PersonAttributeService;

@Service
public class CourseQuestionHistoryService {

    private final CourseQuestionHistoryDao courseQuestionHistoryDao;
    private final PersonAttributeService personAttributeService;

    public CourseQuestionHistoryService(CourseQuestionHistoryDao courseQuestionHistoryDao,
            PersonAttributeService personAttributeService) {
        this.courseQuestionHistoryDao = courseQuestionHistoryDao;
        this.personAttributeService = personAttributeService;
    }

    public CourseQuestionHistory create(CourseQuestionHistory history) {
        return courseQuestionHistoryDao.create(history);
    }

    public CourseQuestionHistory findById(Long id) {
        return courseQuestionHistoryDao.findById(id);
    }

    public CourseQuestionHistory findByIdAndMarkHelpUsed(Long questionId) {
        CourseQuestionHistory question = findById(questionId);
        question.setHelpUsed(true);
        update(question);
        return question;
    }

    public void deleteAllByCourse(Course course) {
        courseQuestionHistoryDao.deleteAllByCourse(course);
    }

    public void update(CourseQuestionHistory courseQuestion) {
        courseQuestionHistoryDao.update(courseQuestion);
    }

    @Transactional
    public List<PersonAttribute> markHelpAndGetAttributes(Long courseId, Long questionId) {
        // 1. Marquer l’aide utilisée
        CourseQuestionHistory questionMarked = findByIdAndMarkHelpUsed(questionId);

        // 2. Récupérer les attributs de la personne liée à la question
        Long personId = questionMarked.getKnowledge().getPerson().getId();

        return personAttributeService.getAttributesByPersonId(personId);
    }

    // ------- Stats activité -------
    public int countAllAnswersByCourse(Course course) {
        return courseQuestionHistoryDao.countAllAnswersByCourse(course);
    }

    public int countAnswersSince(Course course, LocalDateTime since) {
        return courseQuestionHistoryDao.countAnswersSince(course, since);
    }

    public LocalDateTime findLastAnsweredAt(Course course) {
        return courseQuestionHistoryDao.findLastAnsweredAt(course);
    }
}
