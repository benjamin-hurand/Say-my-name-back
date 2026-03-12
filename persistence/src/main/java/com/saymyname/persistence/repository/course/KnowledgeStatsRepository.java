package com.saymyname.persistence.repository.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.organization.course.KnowledgeStatsEntity;

@Repository
public interface KnowledgeStatsRepository extends JpaRepository<KnowledgeStatsEntity, Long> {

}
