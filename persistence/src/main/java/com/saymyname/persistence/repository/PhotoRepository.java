package com.saymyname.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.persistence.entity.PhotoEntity;

@Repository
public interface PhotoRepository extends JpaRepository<PhotoEntity, Long> {

    /**
     * Supprime la/les photos ayant un statut donné pour une personne.
     * Par construction (contrainte unique fonctionnelle), il ne devrait y en avoir
     * au plus qu'une en PENDING.
     */
    long deleteByPersonIdAndStatus(Long personId, PhotoStatus status);
}
