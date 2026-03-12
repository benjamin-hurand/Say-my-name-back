package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.PhotoReport;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PhotoReportEntity;

@Component
public class PhotoReportEntityMapper {

    /**
     * Domaine -> JPA
     * Note: created_at est géré par la BDD (DEFAULT CURRENT_TIMESTAMP).
     * On ne le force pas si null côté modèle.
     */
    public PhotoReportEntity toEntity(PhotoReport model) {
        if (model == null)
            return null;

        PhotoReportEntity e = new PhotoReportEntity();
        e.setId(model.getId());
        e.setReasonType(model.getReasonType());
        e.setReasonText(model.getReasonText());

        if (model.getPersonId() != null) {
            e.setPersonId(model.getPersonId());
        }

        // Reporter (NOT NULL en BDD)
        if (model.getReportedById() != null) {
            UserEntity reporterRef = new UserEntity();
            reporterRef.setId(model.getReportedById());
            e.setReportedBy(reporterRef);
        }

        // created_at : laissé à la base si null
        if (model.getCreatedAt() != null) {
            e.setCreatedAt(model.getCreatedAt());
        }

        return e;
    }

    /**
     * JPA -> Domaine
     */
    public PhotoReport toModel(PhotoReportEntity e) {
        if (e == null)
            return null;

        PhotoReport.Builder b = new PhotoReport.Builder()
                .withId(e.getId())
                .withReasonType(e.getReasonType())
                .withReasonText(e.getReasonText())
                .withCreatedAt(e.getCreatedAt());

        if (e.getPersonId() != null) {
            b.withPersonId(e.getPersonId());
        }

        if (e.getReportedBy() != null && e.getReportedBy().getId() != null) {
            b.withReportedById(e.getReportedBy().getId());
        }

        return b.build();
    }

    /**
     * Optionnel : mise à jour partielle d’une entité depuis le modèle
     * (utile pour PATCH).
     */
    public void updateEntityFromModel(PhotoReport src, PhotoReportEntity target) {
        if (src == null || target == null)
            return;

        if (src.getReasonType() != null) {
            target.setReasonType(src.getReasonType());
        }
        if (src.getReasonText() != null) {
            target.setReasonText(src.getReasonText());
        }
        if (src.getPersonId() != null) {
            target.setPersonId(src.getPersonId());
        }
        if (src.getReportedById() != null) {
            UserEntity u = new UserEntity();
            u.setId(src.getReportedById());
            target.setReportedBy(u);
        }
        // created_at : jamais écrasé (valeur historique)
    }
}
