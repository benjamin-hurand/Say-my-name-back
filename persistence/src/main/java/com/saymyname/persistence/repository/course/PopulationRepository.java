package com.saymyname.persistence.repository.course;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saymyname.persistence.entity.course.PopulationEntity;

@Repository
public interface PopulationRepository extends JpaRepository<PopulationEntity, Long> {

}
