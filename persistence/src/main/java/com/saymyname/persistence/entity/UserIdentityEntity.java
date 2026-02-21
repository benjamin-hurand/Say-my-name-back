// src/main/java/com/saymyname/persistence/entity/UserIdentityEntity.java
package com.saymyname.persistence.entity;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.AuthProvider;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_identities", uniqueConstraints = {
                @UniqueConstraint(name = "uq_ui_user_provider", columnNames = { "user_id", "provider" }),
                @UniqueConstraint(name = "uq_ui_provider_subject", columnNames = { "provider", "provider_subject" })
}, indexes = {
                @Index(name = "ix_ui_user", columnList = "user_id"),
                @Index(name = "ix_ui_provider", columnList = "provider"),
                @Index(name = "ix_ui_provider_subject", columnList = "provider,provider_subject")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class UserIdentityEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(nullable = false)
        @Setter(AccessLevel.NONE)
        @EqualsAndHashCode.Include
        @ToString.Include
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_ui_user"))
        private UserEntity user;

        @Enumerated(EnumType.STRING)
        @Column(name = "provider", nullable = false, length = 24)
        @ToString.Include
        private AuthProvider provider;

        @Column(name = "provider_subject", length = 191)
        private String providerSubject;

        @Column(name = "password_hash", length = 255)
        private String passwordHash;

        @Column(name = "enabled", nullable = false)
        @Builder.Default
        private boolean enabled = true;

        @Column(name = "created_at", nullable = false, updatable = false)
        @Setter(AccessLevel.NONE)
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false)
        @Setter(AccessLevel.NONE)
        private LocalDateTime updatedAt;

        @Column(name = "last_used_at")
        private LocalDateTime lastUsedAt;

        @PrePersist
        protected void onCreate() {
                LocalDateTime now = LocalDateTime.now();
                if (this.createdAt == null)
                        this.createdAt = now;
                if (this.updatedAt == null)
                        this.updatedAt = now;
        }

        @PreUpdate
        protected void onUpdate() {
                this.updatedAt = LocalDateTime.now();
        }

        public boolean isLocal() {
                return provider == AuthProvider.LOCAL;
        }
}
