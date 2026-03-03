package com.saymyname.persistence.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.saymyname.core.model.enums.EmailVerificationPurpose;
import com.saymyname.persistence.jpa.UuidBytesConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "email_verification_tokens", uniqueConstraints = {
                @UniqueConstraint(name = "uq_evt_public_id", columnNames = { "public_id" }),
                @UniqueConstraint(name = "uq_evt_token", columnNames = { "token_hash" })
}, indexes = {
                @Index(name = "ix_evt_user_email", columnList = "user_id,email"),
                @Index(name = "ix_evt_expires", columnList = "expires_at,consumed_at"),
                @Index(name = "ix_evt_last_sent", columnList = "last_sent_at")
})
public class EmailVerificationTokenEntity {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @Convert(converter = UuidBytesConverter.class)
        @Column(name = "public_id", columnDefinition = "BINARY(16)", nullable = false, unique = true, updatable = false)
        private UUID publicId;

        @Column(name = "user_id", nullable = false)
        private Long userId;

        @Column(name = "email", nullable = false, length = 320)
        private String email;

        @Column(name = "token_hash", nullable = false, columnDefinition = "varbinary(32)")
        private byte[] tokenHash;

        @Column(name = "code_hash_phc", length = 255)
        private String codeHashPhc;

        @Enumerated(EnumType.STRING)
        @Column(name = "purpose", nullable = false, length = 32)
        private EmailVerificationPurpose purpose;

        @Column(name = "make_primary_now", nullable = false)
        private boolean makePrimaryNow;

        @Column(name = "attempts", nullable = false)
        private int attempts;

        @Column(name = "resend_count", nullable = false)
        private int resendCount;

        @Column(name = "expires_at", nullable = false)
        private LocalDateTime expiresAt;

        @Column(name = "consumed_at")
        private LocalDateTime consumedAt;

        @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
        private LocalDateTime createdAt;

        @Column(name = "last_sent_at")
        private LocalDateTime lastSentAt;

        @PrePersist
        protected void onPrePersist() {
                if (this.publicId == null) {
                        this.publicId = UUID.randomUUID();
                }
                if (this.purpose == null) {
                        this.purpose = EmailVerificationPurpose.ADD_EMAIL;
                }
        }
}
