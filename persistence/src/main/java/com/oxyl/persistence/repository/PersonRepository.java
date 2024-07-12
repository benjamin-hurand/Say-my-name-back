package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long> {
    @Query("SELECT p FROM PersonEntity p JOIN p.promotions pp JOIN pp.promotion promo WHERE promo.year = :year")
    List<PersonEntity> findByPromotionYear(@Param("year")Integer year);
}
