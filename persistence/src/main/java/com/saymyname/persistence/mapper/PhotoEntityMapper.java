package com.saymyname.persistence.mapper;

import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.people.Photo;
import com.saymyname.core.model.common.User;
import com.saymyname.persistence.entity.PersonEntity;
import com.saymyname.persistence.entity.PhotoEntity;
import com.saymyname.persistence.entity.UserEntity;
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

        Photo.Builder b = new Photo.Builder()
                .withId(e.getId())
                .withStorageKey(e.getStorageKey())
                .withStatus(e.getStatus())
                .withSubmittedAt(e.getSubmittedAt())
                .withApprovedAt(e.getApprovedAt());

        // submitted_by -> User
        if (e.getSubmittedBy() != null && e.getSubmittedBy().getId() != null) {
            User submitter = new User();
            submitter.setId(e.getSubmittedBy().getId());
            submitter.setUsername(e.getSubmittedBy().getUsername()); // si dispo
            b.withSubmittedBy(submitter);
        }

        // approved_by -> User
        if (e.getApprovedBy() != null && e.getApprovedBy().getId() != null) {
            User approver = new User();
            approver.setId(e.getApprovedBy().getId());
            approver.setUsername(e.getApprovedBy().getUsername()); // si dispo
            b.withApprovedBy(approver);
        }

        // person -> Person
        if (e.getPerson() != null && e.getPerson().getId() != null) {
            Person p = new Person();
            p.setId(e.getPerson().getId());
            b.withPerson(p);
        }

        return b.build();
    }
}
