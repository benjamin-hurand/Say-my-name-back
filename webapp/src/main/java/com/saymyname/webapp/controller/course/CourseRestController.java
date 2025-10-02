package com.saymyname.webapp.controller.course;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.AnswerAndNextQuestion;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.CourseStats;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.infra.mail.ConsoleMailer;
import com.saymyname.service.UserService;
import com.saymyname.service.course.CourseQuestionHistoryService;
import com.saymyname.service.course.CourseService;
import com.saymyname.service.course.KnowledgeService;
import com.saymyname.webapp.dto.PersonAttributeLiteDto;
import com.saymyname.webapp.dto.QuizEntryDto;
import com.saymyname.webapp.dto.course.CourseAnswerAndNextQuestionDto;
import com.saymyname.webapp.dto.course.CourseAnswerDto;
import com.saymyname.webapp.dto.course.CourseDto;
import com.saymyname.webapp.dto.course.CourseStatsDto;
import com.saymyname.webapp.dto.course.CourseQuestionDto;
import com.saymyname.webapp.dto.course.CreateCourseDto;
import com.saymyname.webapp.dto.course.StatusCountsDto;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.QuizEntryDtoMapper;
import com.saymyname.webapp.mapper.course.CourseAnswerAndNextQuestionDtoMapper;
import com.saymyname.webapp.mapper.course.CourseDtoMapper;
import com.saymyname.webapp.mapper.course.CourseQuestionHistoryDtoMapper;
import com.saymyname.webapp.mapper.course.CourseStatsDtoMapper;

@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

        private final CourseService courseService;
        private final UserService userService;
        private final CourseDtoMapper courseDtoMapper;
        private final CourseStatsDtoMapper courseStatsDtoMapper;
        private final CourseQuestionHistoryDtoMapper courseQuestionHistoryDtoMapper;
        private final CourseAnswerAndNextQuestionDtoMapper courseAnswerAndNextQuestionDtoMapper;
        private final KnowledgeService knowledgeService;
        private final CourseQuestionHistoryService courseQuestionHistoryService;
        private final PersonAttributeDtoMapper personAttributeDtoMapper;
        private final QuizEntryDtoMapper quizEntryDtoMapper;
        private final InitialCrafter initialCrafter;
        private final Logger logger = LoggerFactory.getLogger(CourseRestController.class);

        public CourseRestController(
                        CourseService courseService,
                        UserService userService,
                        CourseDtoMapper courseDtoMapper,
                        CourseStatsDtoMapper courseStatsDtoMapper,
                        CourseQuestionHistoryDtoMapper courseQuestionHistoryDtoMapper,
                        CourseAnswerAndNextQuestionDtoMapper courseAnswerAndNextQuestionDtoMapper,
                        KnowledgeService knowledgeService,
                        CourseQuestionHistoryService courseQuestionHistoryService,
                        PersonAttributeDtoMapper personAttributeDtoMapper,
                        QuizEntryDtoMapper quizEntryDtoMapper,
                        InitialCrafter initialCrafter) {
                this.courseService = courseService;
                this.userService = userService;
                this.courseDtoMapper = courseDtoMapper;
                this.courseStatsDtoMapper = courseStatsDtoMapper;
                this.courseQuestionHistoryDtoMapper = courseQuestionHistoryDtoMapper;
                this.courseAnswerAndNextQuestionDtoMapper = courseAnswerAndNextQuestionDtoMapper;
                this.knowledgeService = knowledgeService;
                this.courseQuestionHistoryService = courseQuestionHistoryService;
                this.personAttributeDtoMapper = personAttributeDtoMapper;
                this.quizEntryDtoMapper = quizEntryDtoMapper;
                this.initialCrafter = initialCrafter;
        }

        /**
         * Renvoie le dernier cours focal (lastAccessedAt) parmi les actifs, sinon
         * fallback. 204 si rien.
         */
        @GetMapping("/{userId}/current")
        public ResponseEntity<CourseDto> currentCourse(@PathVariable("userId") Long userId) {
                logger.info("userId pour current course: {}", userId);
                Optional<Course> c = courseService.getLastUsedCourse(userId);
                return c.map(course -> ResponseEntity.ok(courseDtoMapper.toDto(course)))
                                .orElseGet(() -> ResponseEntity.noContent().build());
        }

        /** Tous les cours ACTIFS de l’utilisateur (IN_PROGRESS). */
        @GetMapping("/user/{userId}")
        public ResponseEntity<List<CourseDto>> listByUser(@PathVariable("userId") Long userId) {
                var list = courseService.findAllByUser(userId).stream()
                                .map(courseDtoMapper::toDto)
                                .toList();
                return ResponseEntity.ok(list);
        }

        /**
         * Créer un nouveau cours (échoue si IN_PROGRESS déjà présent pour
         * (user,mode,scope)).
         */
        @PostMapping("/create")
        public ResponseEntity<CourseDto> createCourse(@RequestBody CreateCourseDto dto) {
                Course created = courseService.createCourse(courseDtoMapper.toModel(dto));
                return ResponseEntity.status(201).body(courseDtoMapper.toDto(created));
        }

        /** Créer ou reprendre l’IN_PROGRESS existant pour (user,mode,scope). */
        @PostMapping("/create-or-resume")
        public ResponseEntity<CourseDto> createOrResume(@RequestBody CreateCourseDto dto) {
                var course = courseService.createOrResume(courseDtoMapper.toModel(dto));
                return ResponseEntity.ok(courseDtoMapper.toDto(course));
        }

        @PostMapping("/{courseId}/restart")
        public ResponseEntity<CourseDto> restart(@PathVariable("courseId") Long courseId, Principal principal) {
                User user = userService.getCurrentUserOrThrow(principal);
                Course restarted = courseService.restartCourse(courseId, user.getId());
                return ResponseEntity.ok(courseDtoMapper.toDto(restarted));
        }

        /**
         * Marque explicitement le cours comme “focus” (utilisé au clic depuis le
         * hub/menu).
         */
        @PostMapping("/{courseId}/focus")
        public ResponseEntity<Void> focus(@PathVariable("courseId") Long courseId) {
                courseService.touchLastAccessed(courseId);
                return ResponseEntity.noContent().build();
        }

        /** Démarre / récupère la prochaine question (marque aussi le focus). */
        @GetMapping("/{courseId}/continue")
        public ResponseEntity<CourseQuestionDto> start(@PathVariable("courseId") Long courseId) {
                var next = courseService.continueCourse(courseId);
                return ResponseEntity.ok(courseQuestionHistoryDtoMapper.toReducedDto(next));
        }

        @PostMapping("/{courseId}/answer")
        public CourseAnswerAndNextQuestionDto answer(
                        @PathVariable("courseId") Long courseId,
                        @RequestBody CourseAnswerDto answerDto) {

                CourseQuestionHistory answerHistory = courseQuestionHistoryDtoMapper.toModel(answerDto);
                Course course = courseService.findById(answerHistory.getCourse().getId());
                answerHistory.setCourse(course);

                AnswerAndNextQuestion res = courseService.answer(course, answerHistory);

                Integer unknown = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.UNKNOWN);
                Integer discovered = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.DISCOVERED);
                Integer learned = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.LEARNED);
                Integer mastered = knowledgeService.countByCourseAndStatus(course, KnowledgeStatus.MASTERED);

                StatusCountsDto statusCounts = new StatusCountsDto(unknown, discovered, learned, mastered);
                return courseAnswerAndNextQuestionDtoMapper.toDto(res, statusCounts);
        }

        @PostMapping("/{courseId}/questions/{questionId}/help")
        public ResponseEntity<List<PersonAttributeLiteDto>> helpAndGetAttributes(
                        @PathVariable("courseId") Long courseId,
                        @PathVariable("questionId") Long questionId) {
                var list = courseQuestionHistoryService
                                .markHelpAndGetAttributes(courseId, questionId).stream()
                                .map(personAttributeDtoMapper::toLiteDto)
                                .toList();
                return ResponseEntity.ok(list);
        }

        @GetMapping("/{courseId}/training")
        public List<QuizEntryDto> getTrainingFromCourse(@PathVariable("courseId") Long courseId) {
                Course course = courseService.findById(courseId);
                return knowledgeService.findAllByCourse(course).stream()
                                .map(k -> {
                                        String initials = initialCrafter.computeInitials(k.getPerson(),
                                                        course.getGameMode());
                                        return quizEntryDtoMapper.toDto(k, initials);
                                })
                                .toList();
        }

        @GetMapping("/{courseId}/stats")
        public ResponseEntity<CourseStatsDto> stats(@PathVariable("courseId") Long courseId) {
                CourseStats cs = courseService.getStats(courseId);
                return ResponseEntity.ok(courseStatsDtoMapper.toDto(cs));
        }

        @GetMapping("/user/{userId}/stats")
        public ResponseEntity<List<CourseStatsDto>> statsByUser(@PathVariable("userId") Long userId) {
                var list = courseService.getStatsForUser(userId).stream()
                                .map(courseStatsDtoMapper::toDto)
                                .toList();
                return ResponseEntity.ok(list);
        }
}
