// package com.saymyname.webapp.mapper.course;

// import org.springframework.stereotype.Component;

// import com.saymyname.core.model.course.CourseProgressionStep;
// import com.saymyname.webapp.dto.course.CourseProgressionStepDto;

// @Component
// public class CourseProgressionStepDtoMapper {

// public CourseProgressionStepDto toDto(CourseProgressionStep step) {
// return new CourseProgressionStepDto(
// step.getPersonId(),
// step.getPhotoUrl(),
// step.getDiscoveredCount(),
// step.getMasteredCount(),
// step.getCorrect(),
// step.getPool(),
// step.getFeedbackMessage());
// }

// public CourseProgressionStep toModel(CourseProgressionStepDto dto) {
// return new CourseProgressionStep.Builder()
// .withPersonId(dto.personId())
// .withPhotoUrl(dto.photoUrl())
// .withDiscoveredCount(dto.discoveredCount())
// .withMasteredCount(dto.masteredCount())
// .withCorrect(dto.correct())
// .withPool(dto.pool())
// .withFeedbackMessage(dto.feedbackMessage())
// .build();
// }
// }
