// src/main/java/com/saymyname/webapp/controller/course/CourseRestController.java
package com.saymyname.webapp.controller.course;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseStats;
import com.saymyname.core.model.quiz.QuizAnswerResult;
import com.saymyname.core.model.quiz.QuizAnswerSubmission;
import com.saymyname.core.model.quiz.QuizQuestion;
import com.saymyname.core.util.InitialCrafter;
import com.saymyname.service.UserService;
import com.saymyname.service.course.CourseQuestionAttemptService;
import com.saymyname.service.course.CourseService;
import com.saymyname.service.course.KnowledgeService;
import com.saymyname.webapp.dto.PersonAttributeLiteDto;
import com.saymyname.webapp.dto.QuizEntryDto;
import com.saymyname.webapp.dto.course.CourseDto;
import com.saymyname.webapp.dto.course.CourseStatsDto;
import com.saymyname.webapp.dto.course.CreateCourseDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerRequestDto;
import com.saymyname.webapp.dto.quiz.QuizAnswerResultDto;
import com.saymyname.webapp.dto.quiz.QuizQuestionDto;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.QuizEntryDtoMapper;
import com.saymyname.webapp.mapper.course.CourseDtoMapper;
import com.saymyname.webapp.mapper.course.CourseStatsDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerResultDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizAnswerSubmissionDtoMapper;
import com.saymyname.webapp.mapper.quiz.QuizQuestionDtoMapper;

@RestController
@RequestMapping("/api/courses")
public class CourseRestController {

        private final CourseService courseService;
        private final UserService userService;

        private final CourseDtoMapper courseDtoMapper;
        private final CourseStatsDtoMapper courseStatsDtoMapper;

        private final KnowledgeService knowledgeService;
        private final CourseQuestionAttemptService CourseQuestionAttemptService;

        private final PersonAttributeDtoMapper personAttributeDtoMapper;
        private final QuizEntryDtoMapper quizEntryDtoMapper;
        private final InitialCrafter initialCrafter;

        /** ✅ Quiz Question -> DTO */
        private final QuizQuestionDtoMapper quizQuestionDtoMapper;

        /** ✅ Submission DTO -> Model */
        private final QuizAnswerSubmissionDtoMapper quizAnswerSubmissionDtoMapper;

        /** ✅ CourseAnswerResult -> DTO (contains next QuizQuestionDto) */
        private final QuizAnswerResultDtoMapper quizAnswerResultDtoMapper;

        public CourseRestController(
                        CourseService courseService,
                        UserService userService,
                        CourseDtoMapper courseDtoMapper,
                        CourseStatsDtoMapper courseStatsDtoMapper,
                        KnowledgeService knowledgeService,
                        CourseQuestionAttemptService CourseQuestionAttemptService,
                        PersonAttributeDtoMapper personAttributeDtoMapper,
                        QuizEntryDtoMapper quizEntryDtoMapper,
                        InitialCrafter initialCrafter,
                        QuizQuestionDtoMapper quizQuestionDtoMapper,
                        QuizAnswerSubmissionDtoMapper quizAnswerSubmissionDtoMapper,
                        QuizAnswerResultDtoMapper quizAnswerResultDtoMapper) {

                this.courseService = courseService;
                this.userService = userService;
                this.courseDtoMapper = courseDtoMapper;
                this.courseStatsDtoMapper = courseStatsDtoMapper;
                this.knowledgeService = knowledgeService;
                this.CourseQuestionAttemptService = CourseQuestionAttemptService;
                this.personAttributeDtoMapper = personAttributeDtoMapper;
                this.quizEntryDtoMapper = quizEntryDtoMapper;
                this.initialCrafter = initialCrafter;
                this.quizQuestionDtoMapper = quizQuestionDtoMapper;
                this.quizAnswerSubmissionDtoMapper = quizAnswerSubmissionDtoMapper;
                this.quizAnswerResultDtoMapper = quizAnswerResultDtoMapper;
        }

        // ----------------- COURSES LIST / META -----------------

        @GetMapping("/me/current")
        public ResponseEntity<CourseDto> currentCourse() {
                Optional<Course> c = courseService.getLastUsedCourse();
                return c.map(course -> ResponseEntity.ok(courseDtoMapper.toDto(course)))
                                .orElseGet(() -> ResponseEntity.noContent().build());
        }

        @GetMapping("/me")
        public ResponseEntity<List<CourseDto>> listByUser() {
                var list = courseService.findAllByUser().stream()
                                .map(courseDtoMapper::toDto)
                                .toList();
                return ResponseEntity.ok(list);
        }

        @PostMapping("/create")
        public ResponseEntity<CourseDto> createCourse(@RequestBody CreateCourseDto dto) {
                Course created = courseService.createCourse(courseDtoMapper.toModel(dto));
                return ResponseEntity.status(201).body(courseDtoMapper.toDto(created));
        }

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

        @PostMapping("/{courseId}/focus")
        public ResponseEntity<Void> focus(@PathVariable("courseId") Long courseId) {
                courseService.touchLastAccessed(courseId);
                return ResponseEntity.noContent().build();
        }

        // ----------------- COURSE FLOW (OPTION A) -----------------

        /**
         * ✅ Option A: returns QuizQuestionDto directly
         * Backend persists CourseQuestionAttempt and builds QuizQuestion
         * (format-aware).
         */
        @GetMapping("/{courseId}/continue")
        public ResponseEntity<QuizQuestionDto> start(@PathVariable("courseId") Long courseId) {

                QuizQuestion q = courseService.continueCourse(courseId);
                return ResponseEntity.ok(quizQuestionDtoMapper.toDto(q));
        }

        /**
         *
         * Uses CourseService.answer signature:
         * answer(Course course, Long CourseQuestionAttemptId, QuizAnswerSubmission
         * submission)
         */
        @PostMapping("/{courseId}/answer")
        public QuizAnswerResultDto answer(
                        @PathVariable("courseId") Long courseId,
                        @RequestBody QuizAnswerRequestDto answerDto) {

                Course course = courseService.findById(courseId);

                QuizAnswerSubmission submission = quizAnswerSubmissionDtoMapper.toModel(answerDto.submission());

                QuizAnswerResult res = courseService.answer(
                                course,
                                answerDto.questionHandle(),
                                submission);

                return quizAnswerResultDtoMapper.toDto(res);
        }

        @PostMapping("/{courseId}/questions/{questionId}/help")
        public ResponseEntity<List<PersonAttributeLiteDto>> helpAndGetAttributes(
                        @PathVariable("courseId") Long courseId,
                        @PathVariable("questionId") Long questionId) {

                var list = CourseQuestionAttemptService
                                .markHelpAndGetAttributes(courseId, questionId).stream()
                                .map(personAttributeDtoMapper::toLiteDto)
                                .toList();
                return ResponseEntity.ok(list);
        }

        // ----------------- TRAINING VIEW (FROM COURSE) -----------------

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

        // ----------------- STATS -----------------

        @GetMapping("/{courseId}/stats")
        public ResponseEntity<CourseStatsDto> stats(@PathVariable("courseId") Long courseId) {
                CourseStats cs = courseService.getStats(courseId);
                return ResponseEntity.ok(courseStatsDtoMapper.toDto(cs));
        }

        @GetMapping("/me/stats")
        public ResponseEntity<List<CourseStatsDto>> statsByUser() {
                var list = courseService.getStatsForUser().stream()
                                .map(courseStatsDtoMapper::toDto)
                                .toList();
                return ResponseEntity.ok(list);
        }

}
