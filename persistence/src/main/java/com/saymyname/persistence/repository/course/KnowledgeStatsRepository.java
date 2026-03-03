package com.saymyname.persistence.repository.course;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.persistence.entity.organization.course.KnowledgeStatsEntity;

@Repository
public interface KnowledgeStatsRepository extends JpaRepository<KnowledgeStatsEntity, Long> {

}
