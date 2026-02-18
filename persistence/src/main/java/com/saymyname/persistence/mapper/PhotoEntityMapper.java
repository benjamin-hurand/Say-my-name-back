package com.saymyname.persistence.mapper;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Photo;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.entity.organization.PhotoEntity;

import org.springframework.stereotype.Component;

@Component
public class PhotoEntityMapper {

    /**
     * Mappe le modèle métier -> entité JPA.
     * Note : si photo.submittedAt est null, on laisse la colonne à null pour que
     * MySQL applique son DEFAULT CURRENT_TIMESTAMP (insertable=false côté entity).
     */
    public PhotoEntity toEntity(Photo photo) {
        if (photo == null) {
            return null;
        }

        PhotoEntity entity = new PhotoEntity();

        entity.setId(photo.getId());
        entity.setStorageKey(photo.getStorageKey());
        entity.setStatus(photo.getStatus());
        entity.setApprovedAt(photo.getApprovedAt());

        // submitted_at : on ne set que si fourni (sinon laissé à la BDD)
        if (photo.getSubmittedAt() != null) {
            entity.setSubmittedAt(photo.getSubmittedAt());
        }

        // submitted_by -> UserEntity (NOT NULL en BDD)
        if (photo.getSubmittedBy() != null && photo.getSubmittedBy().getId() != null) {
            UserEntity submitter = new UserEntity();
            submitter.setId(photo.getSubmittedBy().getId());
            entity.setSubmittedBy(submitter);
        }

        // approved_by -> UserEntity (NULLABLE)
        if (photo.getApprovedBy() != null && photo.getApprovedBy().getId() != null) {
            UserEntity approver = new UserEntity();
            approver.setId(photo.getApprovedBy().getId());
            entity.setApprovedBy(approver);
        }

        // person -> PersonEntity (NOT NULL en BDD)
        if (photo.getPerson() != null && photo.getPerson().getId() != null) {
            PersonEntity personRef = new PersonEntity();
            personRef.setId(photo.getPerson().getId());
            entity.setPerson(personRef);
        }

        return entity;
    }

    /**
     * Mappe l’entité JPA -> modèle métier.
     */
    public Photo toModel(PhotoEntity e) {
        if (e == null) {
            return null;
        }

        Photo.Builder b = Photo.builder()
                .id(e.getId())
                .storageKey(e.getStorageKey())
                .status(e.getStatus())
                .submittedAt(e.getSubmittedAt())
                .approvedAt(e.getApprovedAt());

        // submitted_by -> User
        if (e.getSubmittedBy() != null && e.getSubmittedBy().getId() != null) {
            User submitter = new User();
            submitter.setId(e.getSubmittedBy().getId());
            submitter.setDisplayName(e.getSubmittedBy().getDisplayName()); // si dispo
            b.submittedBy(submitter);
        }

        // approved_by -> User
        if (e.getApprovedBy() != null && e.getApprovedBy().getId() != null) {
            User approver = new User();
            approver.setId(e.getApprovedBy().getId());
            approver.setDisplayName(e.getApprovedBy().getDisplayName()); // si dispo
            b.approvedBy(approver);
        }

        // person -> Person
        if (e.getPerson() != null && e.getPerson().getId() != null) {
            Person p = new Person();
            p.setId(e.getPerson().getId());
            b.person(p);
        }

        return b.build();
    }
}
