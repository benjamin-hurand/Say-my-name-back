package com.saymyname.persistence.dao.course;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.course.Course;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.course.CourseStatus;
import com.saymyname.persistence.entity.organization.course.CourseEntity;
import com.saymyname.persistence.mapper.course.CourseEntityMapper;
import com.saymyname.persistence.repository.UserRepository;
import com.saymyname.persistence.repository.course.CourseRepository;

@Repository
@Transactional
public class CourseDao {

    private final CourseRepository courseRepo;
    private final CourseEntityMapper courseEntityMapper;
    private final UserRepository userRepo;

    public CourseDao(CourseRepository courseRepo,
            CourseEntityMapper courseEntityMapper,
            UserRepository userRepo) {
        this.courseRepo = courseRepo;
        this.courseEntityMapper = courseEntityMapper;
        this.userRepo = userRepo;
    }

    public Optional<Course> getCurrentCourse(Long userId) {
        return courseRepo.findFirstByUserIdAndStatus(userId, CourseStatus.IN_PROGRESS)
                .map(courseEntityMapper::toShortModel);
    }

    @Transactional
    public Course saveCourse(Course course) {
        CourseEntity e = courseEntityMapper.toEntity(course);

        // ✅ user ref managée
        if (course.getUser() != null && course.getUser().getId() != null) {
            e.setUser(userRepo.getReferenceById(course.getUser().getId()));
        }

        CourseEntity saved = courseRepo.save(e);
        return courseEntityMapper.toShortModel(saved);
    }

    public Optional<Course> findById(Long courseId) {
        return courseRepo.findById(courseId).map(courseEntityMapper::toModel);
    }

    public List<Course> findAllByUserAndStatusesOrderedByLastAccess(Long userId, Collection<CourseStatus> statuses) {
        return courseRepo.findByUserIdAndStatusInOrderByLastAccessedAtDesc(userId, statuses)
                .stream().map(courseEntityMapper::toShortModel).toList();
    }

    public List<Course> findAllByUserAndStatusesOrderedByUpdatedAt(Long userId, Collection<CourseStatus> statuses) {
        return courseRepo.findByUserIdAndStatusInOrderByUpdatedAtDesc(userId, statuses)
                .stream().map(courseEntityMapper::toShortModel).toList();
    }

    public void touchLastAccessed(Long courseId, LocalDateTime at) {
        courseRepo.touchLastAccessed(courseId, at);
    }

    public Optional<Course> findLastAccessedFirstActive(Long userId, Collection<CourseStatus> statuses) {
        return courseRepo.findFirstByUserIdAndStatusInOrderByLastAccessedAtDesc(userId, statuses)
                .map(courseEntityMapper::toShortModel);
    }

    /** Retourne Map<targetAttributeId, courseCount> pour l'impact de suppression. */
    @Transactional(readOnly = true)
    public Map<Long, Long> countByTargetAttributeIds(Collection<Long> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty())
            return Collections.emptyMap();
        List<Object[]> rows = courseRepo.countByTargetAttributeIds(attributeIds);
        if (rows == null || rows.isEmpty())
            return Collections.emptyMap();
        Map<Long, Long> map = new HashMap<>(rows.size());
        for (Object[] r : rows) {
            Long id = r[0] != null ? ((Number) r[0]).longValue() : null;
            Long count = r[1] != null ? ((Number) r[1]).longValue() : 0L;
            if (id != null)
                map.put(id, count);
        }
        return map;
    }

    public Optional<Course> findFirstByUserScopeAndStatus(Long userId,
            Long targetAttributeId,
            PopulationScope populationScope,
            CourseStatus status) {
        return courseRepo
                .findFirstByUserIdAndTargetAttributeIdAndPopulationScopeAndStatus(
                        userId, targetAttributeId, populationScope, status)
                .map(courseEntityMapper::toShortModel);
    }
}