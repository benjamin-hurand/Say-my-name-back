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
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_emails", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_emails_email", columnNames = {"email"})
}, indexes = {
        @Index(name = "ix_user_emails_user", columnList = "user_id")
})
public class UserEmailEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_user_emails_user"))
    private UserEntity user;

    @Column(name = "email", nullable = false, length = 320)
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
}