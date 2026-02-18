package com.saymyname.persistence.mapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.UserEmail;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;

@Component
public class UserEmailEntityMapper {

    /**
     * Mappe le modele metier -> entite JPA.
     * Remarque :
     * - Si userEmail.userId est present, on renseigne une reference UserEntity(id).
     * - added_at / updated_at : on ne set que si fourni ; sinon on laisse la BDD
     * appliquer ses DEFAULT.
     */
    public UserEmailEntity toEntity(UserEmail userEmail) {
        if (userEmail == null) {
            return null;
        }

        UserEmailEntity e = UserEmailEntity.builder().build();

        e.setId(userEmail.getId());
        e.setEmail(userEmail.getEmail());
        e.setPrimary(userEmail.isPrimary());
        e.setLoginAllowed(userEmail.isLoginAllowed());
        e.setRecoveryAllowed(userEmail.isRecoveryAllowed());

        // verified_at (nullable)
        e.setVerifiedAt(toLocalDateTime(userEmail.getVerifiedAt()));

        // added_at (DEFAULT CURRENT_TIMESTAMP cote DB) -> set seulement si fourni
        if (userEmail.getAddedAt() != null) {
            e.setAddedAt(toLocalDateTime(userEmail.getAddedAt()));
        }

        // updated_at (DEFAULT + ON UPDATE cote DB) -> set seulement si fourni
        if (userEmail.getUpdatedAt() != null) {
            e.setUpdatedAt(toLocalDateTime(userEmail.getUpdatedAt()));
        }

        // recovery_eligible_at (nullable)
        e.setRecoveryEligibleAt(toLocalDateTime(userEmail.getRecoveryEligibleAt()));

        // user (NOT NULL en BDD) : si on a l'id, on met une ref ; sinon laisse a null
        if (userEmail.getUserId() != null) {
            UserEntity u = UserEntity.builder()
                    .id(userEmail.getUserId())
                    .build();
            e.setUser(u);
        }

        return e;
    }

    /**
     * Variante pratique : mappe le modele -> entite en imposant l'owner UserEntity
     * deja connu.
     * L'owner fourni ecrase toute eventuelle userId du modele.
     */
    public UserEmailEntity toEntity(UserEmail userEmail, UserEntity owner) {
        UserEmailEntity e = toEntity(userEmail);
        if (e != null && owner != null) {
            e.setUser(owner);
        }
        return e;
    }

    /**
     * Mappe l'entite JPA -> modele metier.
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
                .verifiedAt(toInstant(e.getVerifiedAt()))
                .addedAt(toInstant(e.getAddedAt()))
                .updatedAt(toInstant(e.getUpdatedAt()))
                .recoveryEligibleAt(toInstant(e.getRecoveryEligibleAt()));

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

    private LocalDateTime toLocalDateTime(Instant value) {
        return value == null ? null : LocalDateTime.ofInstant(value, ZoneOffset.UTC);
    }

    private Instant toInstant(LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC);
    }
}
