package com.saymyname.persistence.entity;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.UUID;

import com.saymyname.persistence.jpa.UuidBytesConverter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_refresh_tokens", uniqueConstraints = {
                @UniqueConstraint(name = "uq_urt_token_id", columnNames = { "token_id" }),
                @UniqueConstraint(name = "uq_urt_token_hash", columnNames = { "token_hash" })
}, indexes = {
                @Index(name = "ix_urt_user", columnList = "user_id"),
                @Index(name = "ix_urt_user_expires", columnList = "user_id,expires_at"),
                @Index(name = "ix_urt_family", columnList = "family_id"),
                @Index(name = "ix_urt_user_device", columnList = "user_id,device_id")
})
public class UserRefreshTokenEntity {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_urt_user"))
        private UserEntity user;

        @Column(name = "token_id", nullable = false, length = 64)
        private String tokenId;

        @Column(name = "token_hash", nullable = false, columnDefinition = "binary(32)")
        private byte[] tokenHash;

        @Convert(converter = UuidBytesConverter.class)
        @Column(name = "family_id", nullable = false, columnDefinition = "binary(16)")
        private UUID familyId;

        @Column(name = "replaced_by_token_id", length = 64)
        private String replacedByTokenId;

        @Column(name = "created_at", nullable = false)
        private LocalDateTime createdAt;

        @Column(name = "expires_at", nullable = false)
        private LocalDateTime expiresAt;

        @Column(name = "last_used_at")
        private LocalDateTime lastUsedAt;

        @Column(name = "revoked_at")
        private LocalDateTime revokedAt;

        @Column(name = "revoke_reason", length = 64)
        private String revokeReason;

        @Column(name = "device_id", length = 128)
        private String deviceId;

        @Column(name = "device_name", length = 128)
        private String deviceName;

        @Column(name = "ip_created", length = 45)
        private String ipCreated;

        @Column(name = "ip_last_used", length = 45)
        private String ipLastUsed;

        @Column(name = "user_agent", length = 255)
        private String userAgent;

        @PrePersist
        protected void onCreate() {
                if (this.createdAt == null) {
                        this.createdAt = LocalDateTime.now();
                }
        }

        // -----------------------------
        // Helpers métier
        // -----------------------------

        @Transient
        public Long getUserIdSafe() {
                return user != null ? user.getId() : null;
        }

        @Transient
        public boolean isExpired() {
                return expiresAt != null && expiresAt.isBefore(LocalDateTime.now());
        }

        @Transient
        public boolean isRevoked() {
                return revokedAt != null;
        }

        @Transient
        public boolean isActive() {
                return !isRevoked() && !isExpired();
        }
}