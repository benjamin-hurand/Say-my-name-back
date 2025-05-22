package com.saymyname.persistence.dao.course;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.CourseStatus;
import com.saymyname.persistence.entity.course.CourseEntity;
import com.saymyname.persistence.mapper.course.CourseEntityMapper;
import com.saymyname.persistence.repository.course.CourseRepository;

@Repository
@Transactional
public class CourseDao {

    private final CourseRepository courseRepo;
    private final CourseEntityMapper courseEntityMapper;

    public CourseDao(CourseRepository courseRepo,
            CourseEntityMapper courseEntityMapper) {
        this.courseRepo = courseRepo;
        this.courseEntityMapper = courseEntityMapper;
    }

    public Optional<Course> getCurrentCourse(long userId) {
        return courseRepo.findFirstByUserIdAndStatus(userId, CourseStatus.IN_PROGRESS)
                .map(courseEntityMapper::toModel);
    }

    public Course saveCourse(Course course) {
        CourseEntity courseToBeInserted = courseEntityMapper.toEntity(course);

        CourseEntity saved = courseRepo.save(courseToBeInserted);
        return courseEntityMapper.toModel(saved);
    }

    public Optional<Course> findById(Long courseId) {
        return courseRepo.findById(courseId)
                .map(courseEntityMapper::toModel);
    }

}
