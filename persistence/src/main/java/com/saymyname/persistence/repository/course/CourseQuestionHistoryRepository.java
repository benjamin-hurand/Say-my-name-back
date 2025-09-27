package com.saymyname.persistence.repository.course;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.course.CourseQuestionHistoryEntity;

@Repository
public interface CourseQuestionHistoryRepository extends JpaRepository<CourseQuestionHistoryEntity, Long> {

    void deleteByCourseId(Long courseId);

    long countByCourseId(Long courseId);

    long countByCourseIdAndAnsweredAtAfter(Long courseId, LocalDateTime after);

    @Query("select max(c.answeredAt) from CourseQuestionHistoryEntity c where c.course.id = :courseId")
    LocalDateTime findLastAnsweredAt(@Param("courseId") Long courseId);

}
