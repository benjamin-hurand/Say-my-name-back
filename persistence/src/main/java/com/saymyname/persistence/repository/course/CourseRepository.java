package com.saymyname.persistence.repository.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.persistence.entity.course.CourseEntity;

import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    Optional<CourseEntity> findFirstByUserIdAndStatus(Long userId, CourseStatus status);
}
