// package com.saymyname.webapp.mapper.course;

// import org.springframework.stereotype.Component;

// import com.saymyname.core.model.course.CourseProgressionStep;
// import com.saymyname.webapp.dto.course.CourseProgressionStepDto;

// @Component
// public class CourseProgressionStepDtoMapper {

// public CourseProgressionStepDto toDto(CourseProgressionStep step) {
// return new CourseProgressionStepDto(
// step.getPersonId(),
// step.getStorageKey(),
// step.getDiscoveredCount(),
// step.getMasteredCount(),
// step.getCorrect(),
// step.getPool(),
// step.getFeedbackMessage());
// }

// public CourseProgressionStep toModel(CourseProgressionStepDto dto) {
// return CourseProgressionStep.builder()
// .personId(dto.personId())
// .storageKey(dto.storageKey())
// .discoveredCount(dto.discoveredCount())
// .masteredCount(dto.masteredCount())
// .correct(dto.correct())
// .pool(dto.pool())
// .feedbackMessage(dto.feedbackMessage())
// .build();
// }
// }
