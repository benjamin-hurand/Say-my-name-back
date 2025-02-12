package com.saymyname.persistence.repository;

import com.saymyname.persistence.dto.PersonBasicDto;
import com.saymyname.persistence.entity.PersonEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<PersonEntity, Long>, PersonRepositoryCustom {
    List<PersonEntity> findByUserIsNull();

    @Query("SELECT new com.saymyname.persistence.dto.PersonBasicDto(p.id, p.firstName, p.lastName) FROM PersonEntity p WHERE p.user IS NULL")
    List<PersonBasicDto> findPersonsBasicInfoWithoutUser();
}
