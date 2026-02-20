// src/main/java/com/saymyname/persistence/entity/UserEmailEntity.java
package com.saymyname.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "user_emails", uniqueConstraints = {
                @UniqueConstraint(name = "uq_user_emails_email", columnNames = "email")
}, indexes = {
                @Index(name = "ix_user_emails_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
public class UserEmailEntity {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(nullable = false)
        @EqualsAndHashCode.Include
        @ToString.Include
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_emails_user"))
        private UserEntity user;

        @Column(name = "email", nullable = false, length = 320)
        @ToString.Include
        private String email;

        @Column(name = "is_primary", nullable = false)
        private boolean primary;

        @Column(name = "is_login_allowed", nullable = false)
        private boolean loginAllowed;

        @Column(name = "is_recovery_allowed", nullable = false)
        private boolean recoveryAllowed;

        @Column(name = "verified_at")
        private LocalDateTime verifiedAt;

        @Column(name = "added_at", nullable = false)
        private LocalDateTime addedAt;

        @Column(name = "updated_at", nullable = false)
        private LocalDateTime updatedAt;

        @Column(name = "recovery_eligible_at")
        private LocalDateTime recoveryEligibleAt;

        @PrePersist
        protected void onCreate() {
                LocalDateTime now = LocalDateTime.now();
                if (this.addedAt == null) {
                        this.addedAt = now;
                }
                if (this.updatedAt == null) {
                        this.updatedAt = now;
                }
        }

        @PreUpdate
        protected void onUpdate() {
                this.updatedAt = LocalDateTime.now();
        }
}
