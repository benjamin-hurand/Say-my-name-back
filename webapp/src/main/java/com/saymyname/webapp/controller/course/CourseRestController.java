package com.saymyname.webapp.controller.course;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.course.AnswerAndNextQuestion;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.service.course.CourseQuestionHistoryService;
import com.saymyname.service.course.CourseService;
import com.saymyname.service.course.KnowledgeService;
import com.saymyname.webapp.dto.PersonAttributeDto;
import com.saymyname.webapp.dto.QuizEntryDto;
import com.saymyname.webapp.dto.course.CourseAnswerAndNextQuestionDto;
import com.saymyname.webapp.dto.course.CourseAnswerDto;
import com.saymyname.webapp.dto.course.CourseDto;
import com.saymyname.webapp.dto.course.CourseQuestionDto;
import com.saymyname.webapp.dto.course.CreateCourseDto;
import com.saymyname.webapp.dto.course.StatusCountsDto;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.QuizEntryDtoMapper;
import com.saymyname.webapp.mapper.course.CourseAnswerAndNextQuestionDtoMapper;
import com.saymyname.webapp.mapper.course.CourseDtoMapper;
import com.saymyname.webapp.mapper.course.CourseQuestionHistoryDtoMapper;

@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

        private final CourseService courseService;
        private final CourseDtoMapper courseDtoMapper;
        private final CourseQuestionHistoryDtoMapper courseQuestionHistoryDtoMapper;
        private final CourseAnswerAndNextQuestionDtoMapper courseAnswerAndNextQuestionDtoMapper;
        private final KnowledgeService knowledgeService;
        private final CourseQuestionHistoryService courseQuestionHistoryService;
        private final PersonAttributeDtoMapper personAttributeDtoMapper;
        private final QuizEntryDtoMapper quizEntryDtoMapper;
        private final InitialCrafter initialCrafter;
        private static final Logger logger = LoggerFactory.getLogger(CourseRestController.class);

        public CourseRestController(
                        CourseService courseService,
                        CourseDtoMapper courseDtoMapper,
                        CourseQuestionHistoryDtoMapper courseQuestionHistoryDtoMapper,
                        CourseAnswerAndNextQuestionDtoMapper courseAnswerAndNextQuestionDtoMapper,
                        KnowledgeService knowledgeService,
                        CourseQuestionHistoryService courseQuestionHistoryService,
                        PersonAttributeDtoMapper personAttributeDtoMapper,
                        QuizEntryDtoMapper quizEntryDtoMapper,
                        InitialCrafter initialCrafter) {
                this.courseService = courseService;
                this.courseDtoMapper = courseDtoMapper;
                this.courseQuestionHistoryDtoMapper = courseQuestionHistoryDtoMapper;
                this.courseAnswerAndNextQuestionDtoMapper = courseAnswerAndNextQuestionDtoMapper;
                this.knowledgeService = knowledgeService;
                this.courseQuestionHistoryService = courseQuestionHistoryService;
                this.personAttributeDtoMapper = personAttributeDtoMapper;
                this.quizEntryDtoMapper = quizEntryDtoMapper;
                this.initialCrafter = initialCrafter;
        }

        /**
         * GET /api/courses/current
         * Renvoie 200 + CourseDto si un cours IN_PROGRESS existe,
         * 204 No Content sinon
         */
        @GetMapping("/{userId}/current")
        public ResponseEntity<CourseDto> currentCourse(@PathVariable("userId") Long userId) {
                return courseService.getCurrentCourse(userId)
                                .map(course -> ResponseEntity.ok(courseDtoMapper.toDto(course)))
                                .orElseGet(() -> ResponseEntity.noContent().build());
        }

        /**
         * POST /api/courses
         * Crée un nouveau cours
         */
        @PostMapping("/create")
        public ResponseEntity<CourseDto> createCourse(@RequestBody CreateCourseDto dto) {
                Course courseToBeCreated = courseDtoMapper.toModel(dto);
                Course created = courseService.createCourse(courseToBeCreated);
                CourseDto createdDto = courseDtoMapper.toDto(created);
                return ResponseEntity.status(201).body(createdDto);
        }

        // 1. Démarrer / récupérer la première question
        @GetMapping("/{courseId}/continue")
        public ResponseEntity<CourseQuestionDto> start(@PathVariable("courseId") Long courseId) {
                CourseQuestionDto dto = courseQuestionHistoryDtoMapper
                                .toReducedDto(courseService.continueCourse(courseId));
                return ResponseEntity.ok(dto);
        }

        // TODO : VERIFIER COMMENT LES COURSES DES UTILISATEURS SONT PROTEGES DES MODIFS
        // D'UN AUTRE UTILISATEUR
        // 2. Soumettre une réponse & récupérer la suite
        @PostMapping("/{courseId}/answer")
        public CourseAnswerAndNextQuestionDto answer(
                        @PathVariable("courseId") Long courseId,
                        @RequestBody CourseAnswerDto answerDto) {

                CourseQuestionHistory answerHistory = courseQuestionHistoryDtoMapper.toModel(answerDto);
                Course course = courseService.findById(answerHistory.getCourse().getId());
                answerHistory.setCourse(course);
                Integer unknown = knowledgeService.countByCourseAndStatus(answerHistory.getCourse(),
                                KnowledgeStatus.UNKNOWN);
                Integer discoveries = knowledgeService.countByCourseAndStatus(answerHistory.getCourse(),
                                KnowledgeStatus.DISCOVERED);
                Integer learned = knowledgeService.countByCourseAndStatus(answerHistory.getCourse(),
                                KnowledgeStatus.LEARNED);
                Integer mastered = knowledgeService.countByCourseAndStatus(answerHistory.getCourse(),
                                KnowledgeStatus.MASTERED);
                AnswerAndNextQuestion answerAndNextQuestion = courseService.answer(course, answerHistory);
                StatusCountsDto statusCounts = new StatusCountsDto(unknown, discoveries, learned, mastered);
                CourseAnswerAndNextQuestionDto dto = courseAnswerAndNextQuestionDtoMapper.toDto(answerAndNextQuestion,
                                statusCounts);
                logger.info("ANSWER DTO : " + dto);
                return dto;
        }

        @PostMapping("/{courseId}/questions/{questionId}/help")
        public ResponseEntity<List<PersonAttributeDto>> helpAndGetAttributes(
                        @PathVariable("courseId") Long courseId,
                        @PathVariable("questionId") Long questionId) {

                List<PersonAttributeDto> personAttributeDtoList = courseQuestionHistoryService
                                .markHelpAndGetAttributes(courseId, questionId).stream()
                                .map(personAttributeDtoMapper::toDto)
                                .toList();

                return ResponseEntity.ok(personAttributeDtoList);
        }

        @GetMapping("/{courseId}/training")
        public List<QuizEntryDto> getTrainingFromCourse(@PathVariable("courseId") Long courseId) {
                Course course = courseService.findById(courseId);
                List<QuizEntryDto> quizListDto = knowledgeService.findAllByCourse(course).stream()
                                .map(knowledge -> {
                                        // Suppose initialCrafter is injected as a dependency
                                        String initials = initialCrafter.computeInitials(knowledge.getPerson(),
                                                        course.getGameMode());
                                        return quizEntryDtoMapper.toDto(knowledge, initials);
                                }).toList();
                return quizListDto;

        }
}
