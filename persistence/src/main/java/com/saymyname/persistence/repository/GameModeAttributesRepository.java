package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.GameModeAttributeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameModeAttributesRepository extends JpaRepository<GameModeAttributeEntity, Long> {
    
    List<GameModeAttributeEntity> findByGameMode_Id(Long gameModeId);
    
    List<GameModeAttributeEntity> findByAttribute_Id(Long attributeId);
}
