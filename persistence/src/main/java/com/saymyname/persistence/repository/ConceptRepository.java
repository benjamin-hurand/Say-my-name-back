package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.concept.ConceptEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConceptRepository extends JpaRepository<ConceptEntity, Long> {

    Optional<ConceptEntity> findByCode(String code);

    boolean existsByCode(String code);

    @Query("""
            select c
            from ConceptEntity c
            order by c.code asc
            """)
    List<ConceptEntity> findAllOrderByCodeAsc();
}