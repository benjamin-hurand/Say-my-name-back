package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.PromotionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PromotionRepository extends JpaRepository<PromotionEntity, Long> {
    
    List<PromotionEntity> findByYear(int year);
    
    List<PromotionEntity> findByMonth(Integer month);
}
