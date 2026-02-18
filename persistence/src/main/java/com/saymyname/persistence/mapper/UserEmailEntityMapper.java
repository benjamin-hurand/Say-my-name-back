package com.saymyname.persistence.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;

@Component
public class UserEmailEntityMapper {

    /**
     * Mappe le modèle métier -> entité JPA.
     * Remarque :
     * - Si userEmail.userId est présent, on renseigne une référence UserEntity(id).
     * - added_at / updated_at : on ne set que si fourni ; sinon on laisse la BDD
     * appliquer ses DEFAULT.
     */
    public UserEmailEntity toEntity(UserEmail userEmail) {
        if (userEmail == null) {
            return null;
        }

        UserEmailEntity e = new UserEmailEntity();

        e.setId(userEmail.getId());
        e.setEmail(userEmail.getEmail());
        e.setPrimary(userEmail.isPrimary());
        e.setLoginAllowed(userEmail.isLoginAllowed());
        e.setRecoveryAllowed(userEmail.isRecoveryAllowed());

        // verified_at (nullable)
        e.setVerifiedAt(userEmail.getVerifiedAt());

        // added_at (DEFAULT CURRENT_TIMESTAMP côté DB) → set seulement si fourni
        if (userEmail.getAddedAt() != null) {
            e.setAddedAt(userEmail.getAddedAt());
        }

        // updated_at (DEFAULT + ON UPDATE côté DB) → set seulement si fourni
        if (userEmail.getUpdatedAt() != null) {
            e.setUpdatedAt(userEmail.getUpdatedAt());
        }

        // recovery_eligible_at (nullable)
        e.setRecoveryEligibleAt(userEmail.getRecoveryEligibleAt());

        // user (NOT NULL en BDD) : si on a l'id, on met une ref ; sinon laissé à null
        if (userEmail.getUserId() != null) {
            UserEntity u = new UserEntity();
            u.setId(userEmail.getUserId());
            e.setUser(u);
        }

        return e;
    }

    /**
     * Variante pratique : mappe le modèle -> entité en imposant l'owner UserEntity
     * déjà connu.
     * L’owner fourni écrase toute éventuelle userId du modèle.
     */
    public UserEmailEntity toEntity(UserEmail userEmail, UserEntity owner) {
        UserEmailEntity e = toEntity(userEmail);
        if (e != null && owner != null) {
            e.setUser(owner);
        }
        return e;
    }

    /**
     * Mappe l’entité JPA -> modèle métier.
     */
    public UserEmail toModel(UserEmailEntity e) {
        if (e == null) {
            return null;
        }

        UserEmail.Builder b = UserEmail.builder()
                .id(e.getId())
                .email(e.getEmail())
                .primary(e.isPrimary())
                .loginAllowed(e.isLoginAllowed())
                .recoveryAllowed(e.isRecoveryAllowed())
                .verifiedAt(e.getVerifiedAt())
                .addedAt(e.getAddedAt())
                .updatedAt(e.getUpdatedAt())
                .recoveryEligibleAt(e.getRecoveryEligibleAt());

        if (e.getUser() != null && e.getUser().getId() != null) {
            b.userId(e.getUser().getId());
        }

        return b.build();
    }

    // ---------- Helpers de liste (optionnels mais pratiques) ----------

    public List<UserEmailEntity> toEntityList(List<UserEmail> list, UserEntity owner) {
        if (list == null)
            return List.of();
        List<UserEmailEntity> out = new ArrayList<>(list.size());
        for (UserEmail m : list) {
            out.add(toEntity(m, owner));
        }
        return out;
    }

    public List<UserEmail> toModelList(List<UserEmailEntity> list) {
        if (list == null)
            return List.of();
        List<UserEmail> out = new ArrayList<>(list.size());
        for (UserEmailEntity e : list) {
            out.add(toModel(e));
        }
        return out;
    }
}
