package com.saymyname.persistence.entity;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
@Table(name = "user_emails", uniqueConstraints = {
        @UniqueConstraint(name = "uq_user_emails_email", columnNames = "email")
// l’unicité “un seul is_primary par user” est assurée en DB via la colonne
// générée primary_slot
})
public class UserEmailEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
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

    public UserEmailEntity() {
    }

    public UserEmailEntity(Long id,
            UserEntity user,
            String email,
            boolean primary,
            boolean loginAllowed,
            boolean recoveryAllowed,
            LocalDateTime verifiedAt,
            LocalDateTime addedAt,
            LocalDateTime updatedAt,
            LocalDateTime recoveryEligibleAt) {
        this.id = id;
        this.user = user;
        this.email = email;
        this.primary = primary;
        this.loginAllowed = loginAllowed;
        this.recoveryAllowed = recoveryAllowed;
        this.verifiedAt = verifiedAt;
        this.addedAt = addedAt;
        this.updatedAt = updatedAt;
        this.recoveryEligibleAt = recoveryEligibleAt;
    }

    // ===== Callbacks JPA pour timestamps =====

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

    // ===== Getters

    public Long getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getEmail() {
        return email;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isLoginAllowed() {
        return loginAllowed;
    }

    public boolean isRecoveryAllowed() {
        return recoveryAllowed;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getRecoveryEligibleAt() {
        return recoveryEligibleAt;
    }

    // ===== Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public void setLoginAllowed(boolean loginAllowed) {
        this.loginAllowed = loginAllowed;
    }

    public void setRecoveryAllowed(boolean recoveryAllowed) {
        this.recoveryAllowed = recoveryAllowed;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setRecoveryEligibleAt(LocalDateTime recoveryEligibleAt) {
        this.recoveryEligibleAt = recoveryEligibleAt;
    }

    // ===== equals / hashCode

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserEmailEntity that))
            return false;
        return Objects.equals(id, that.id) &&
                Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, email);
    }

    @Override
    public String toString() {
        return "UserEmailEntity{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", email='" + email + '\'' +
                ", primary=" + primary +
                ", loginAllowed=" + loginAllowed +
                ", recoveryAllowed=" + recoveryAllowed +
                ", verifiedAt=" + verifiedAt +
                ", addedAt=" + addedAt +
                ", updatedAt=" + updatedAt +
                ", recoveryEligibleAt=" + recoveryEligibleAt +
                '}';
    }
}
