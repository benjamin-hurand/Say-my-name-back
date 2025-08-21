package com.saymyname.persistence.repository;

import com.saymyname.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long>, PersonRepositoryCustom {
    List<PersonEntity> findByUserIsNull();

    Optional<PersonEntity> findByUserId(Long userId);
}
