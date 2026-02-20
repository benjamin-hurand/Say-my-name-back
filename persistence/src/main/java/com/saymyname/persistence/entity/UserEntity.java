// src/main/java/com/saymyname/persistence/entity/UserEntity.java
package com.saymyname.persistence.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.persistence.jpa.UuidBytesConverter;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
                @UniqueConstraint(name = "uk_users_public_id", columnNames = { "public_id" })
}, indexes = {
                @Index(name = "ix_users_display_name", columnList = "display_name")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class UserEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(nullable = false)
        @EqualsAndHashCode.Include
        @ToString.Include
        private Long id;

        @Convert(converter = UuidBytesConverter.class)
        @Column(name = "public_id", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
        private UUID publicId;

        @Column(name = "display_name", length = 50)
        @ToString.Include
        private String displayName;

        @Enumerated(EnumType.STRING)
        @Column(name = "srs_algorithm", nullable = false, length = 8)
        private SrsAlgorithm srsAlgorithm = SrsAlgorithm.SM2;

        @Column(name = "roles", length = 255)
        private String roles;

        @Column(name = "active", nullable = false)
        private boolean active = false;

        @Column(name = "auth_version", nullable = false)
        private int authVersion = 0;

        /**
         * DB handles DEFAULT CURRENT_TIMESTAMP + ON UPDATE CURRENT_TIMESTAMP.
         * Keep this field read-only from JPA.
         */
        @Setter(AccessLevel.NONE)
        @Column(name = "auth_updated_at", nullable = false, insertable = false, updatable = false)
        private LocalDateTime authUpdatedAt;

        @Setter(AccessLevel.NONE)
        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        @OrderBy("primary DESC, verifiedAt DESC, id ASC")
        private List<UserEmailEntity> emails = new ArrayList<>();

        /**
         * Set avoids MultipleBagFetchException when fetching emails + identities.
         * For transient entities, equals/hashCode are id-based, so id=null collisions
         * are possible if multiple new identities are added before persistence.
         */
        @Setter(AccessLevel.NONE)
        @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
        private Set<UserIdentityEntity> identities = new LinkedHashSet<>();

        public UserEntity(Long id) {
                this.id = id;
        }

        @PrePersist
        protected void onPrePersist() {
                if (this.publicId == null) {
                        this.publicId = UUID.randomUUID();
                }
                if (this.srsAlgorithm == null) {
                        this.srsAlgorithm = SrsAlgorithm.SM2;
                }
        }

        // -------- Helpers emails --------
        public void addEmail(UserEmailEntity email) {
                if (email == null)
                        return;
                this.emails.add(email);
                email.setUser(this);
        }

        public void removeEmail(UserEmailEntity email) {
                if (email == null)
                        return;
                this.emails.remove(email);
                email.setUser(null);
        }

        // -------- Helpers identities --------
        public void addIdentity(UserIdentityEntity identity) {
                if (identity == null)
                        return;
                this.identities.add(identity);
                identity.setUser(this);
        }

        public void removeIdentity(UserIdentityEntity identity) {
                if (identity == null)
                        return;
                this.identities.remove(identity);
                identity.setUser(null);
        }

        // -------- Transients --------
        @Transient
        public String getPrimaryEmailValue() {
                if (emails == null)
                        return null;
                for (UserEmailEntity e : emails) {
                        if (e != null && e.isPrimary())
                                return e.getEmail();
                }
                return null;
        }

        @Transient
        public boolean hasLocalPassword() {
                if (identities == null)
                        return false;
                return identities.stream().anyMatch(i -> i != null
                                && i.isEnabled()
                                && i.getProvider() == AuthProvider.LOCAL
                                && i.getPasswordHash() != null
                                && !i.getPasswordHash().isBlank());
        }

        public void setEmails(List<UserEmailEntity> emails) {
                this.emails.clear();
                if (emails != null) {
                        for (UserEmailEntity e : emails)
                                addEmail(e);
                }
        }

        public void setIdentities(Set<UserIdentityEntity> identities) {
                this.identities.clear();
                if (identities != null) {
                        for (UserIdentityEntity i : identities)
                                addIdentity(i);
                }
        }

        public void setIdentitiesFromList(List<UserIdentityEntity> identities) {
                this.identities.clear();
                if (identities != null) {
                        for (UserIdentityEntity i : identities)
                                addIdentity(i);
                }
        }

        // Backward-compatible boolean getter used by legacy code.
        public boolean getActive() {
                return active;
        }

        // Backward-compatible boolean accessor used by legacy code.
        public boolean isActive() {
                return active;
        }
}
