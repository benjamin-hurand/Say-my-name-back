package com.saymyname.webapp.mapper.course;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.course.CourseQuestionHistory;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.enums.DifficultyLevel;
import com.saymyname.core.model.enums.PoolType;
import com.saymyname.core.model.people.Person;
import com.saymyname.webapp.dto.course.CourseAnswerDto;
import com.saymyname.webapp.dto.course.CourseQuestionDto;
import com.saymyname.webapp.dto.course.CourseQuestionHistoryDto;

@Component
public class CourseQuestionHistoryDtoMapper {

    private final CourseDtoMapper courseDtoMapper;
    private final KnowledgeDtoMapper knowledgeDtoMapper;

    @Value("${photos.storage.public-base-url}")
    private String photosBaseUrl;

    public CourseQuestionHistoryDtoMapper(CourseDtoMapper courseDtoMapper, KnowledgeDtoMapper knowledgeDtoMapper) {
        this.courseDtoMapper = courseDtoMapper;
        this.knowledgeDtoMapper = knowledgeDtoMapper;
    }

    public CourseQuestionHistoryDto toDto(CourseQuestionHistory courseQuestionHistory) {
        return new CourseQuestionHistoryDto(
                courseQuestionHistory.getId(),
                courseDtoMapper.toDto(courseQuestionHistory.getCourse()),
                knowledgeDtoMapper.toDto(courseQuestionHistory.getKnowledge()),
                courseQuestionHistory.getQuestionRound(),
                courseQuestionHistory.getAskedAt(),
                courseQuestionHistory.getAnsweredAt(),
                courseQuestionHistory.getResponseTimeMs(),
                courseQuestionHistory.getUserAnswer(),
                courseQuestionHistory.isCorrect(),
                courseQuestionHistory.getPoolType(),
                courseQuestionHistory.isHelpUsed());
    }

    public CourseQuestionHistory toModel(CourseQuestionHistoryDto dto) {
        return new CourseQuestionHistory.Builder()
                .withId(dto.id())
                .withCourse(courseDtoMapper.toModel(dto.course()))
                .withKnowledge(knowledgeDtoMapper.toModel(dto.knowledge()))
                .withQuestionRound(dto.questionRound())
                .withAskedAt(dto.askedAt())
                .withAnsweredAt(dto.answeredAt())
                .withResponseTimeMs(dto.responseTimeMs())
                .withUserAnswer(dto.userAnswer())
                .withCorrect(dto.correct())
                .withPoolType(dto.poolType())
                .withHelpUsed(dto.helpUsed())
                .build();
    }

    public CourseQuestionHistory toModel(CourseAnswerDto dto) {
        return new CourseQuestionHistory.Builder()
                .withId(dto.courseQuestionId())
                .withCourse(new Course.Builder()
                        .withId(dto.courseId())
                        .build())
                .withUserAnswer(dto.answer())
                .build();
    }

    public CourseQuestionDto toReducedDto(CourseQuestionHistory courseQuestionHistory) {
        Person person = courseQuestionHistory.getKnowledge().getPerson();

        String fullUrl = person.getApprovedPhoto()
                .map(photo -> toPublicUrl(photo.getStorageKey()))
                .orElse(null);

        return new CourseQuestionDto(
                courseQuestionHistory.getId(),
                courseQuestionHistory.getQuestionRound(),
                person.getId(),
                fullUrl,
                courseQuestionHistory.getPoolType(),
                toDifficulty(courseQuestionHistory.getKnowledge(), courseQuestionHistory.getPoolType()));
    }

    private DifficultyLevel toDifficulty(Knowledge k, PoolType pool) {
        if (pool == PoolType.ERROR_RECENT)
            return DifficultyLevel.HARD;
        if (k.getEaseFactor().doubleValue() < 1.5)
            return DifficultyLevel.HARD;
        if (pool == PoolType.NEW)
            return DifficultyLevel.EASY;
        return DifficultyLevel.MEDIUM;
    }

    private String toPublicUrl(String storageKey) {
        if (storageKey == null || storageKey.isBlank())
            return null;
        return photosBaseUrl.endsWith("/") ? photosBaseUrl + storageKey : photosBaseUrl + "/" + storageKey;
    }
}
