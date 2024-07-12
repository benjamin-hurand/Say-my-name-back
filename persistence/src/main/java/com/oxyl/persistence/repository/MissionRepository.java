package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.MissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<MissionEntity, Long> {
    
    List<MissionEntity> findByPerson_Id(Long personId);
    
    List<MissionEntity> findByCompany_Id(Long companyId);
    
    List<MissionEntity> findByMissionTitle(String missionTitle);
}
