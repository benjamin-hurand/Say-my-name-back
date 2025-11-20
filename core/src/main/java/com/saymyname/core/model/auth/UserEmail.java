package com.saymyname.core.model.auth;

import java.time.LocalDateTime;
import java.util.Objects;

public class UserEmail {
    private Long id;
    private Long userId;
    private String email;
    private boolean primary; // correspond à is_primary
    private boolean loginAllowed; // is_login_allowed
    private boolean recoveryAllowed; // is_recovery_allowed
    private LocalDateTime verifiedAt;
    private LocalDateTime addedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime recoveryEligibleAt;

    public UserEmail() {
    }

    private UserEmail(Builder b) {
        this.id = b.id;
        this.userId = b.userId;
        this.email = b.email;
        this.primary = b.primary;
        this.loginAllowed = b.loginAllowed;
        this.recoveryAllowed = b.recoveryAllowed;
        this.verifiedAt = b.verifiedAt;
        this.addedAt = b.addedAt;
        this.updatedAt = b.updatedAt;
        this.recoveryEligibleAt = b.recoveryEligibleAt;
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
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

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    // Builder
    public static class Builder {
        private Long id;
        private Long userId;
        private String email;
        private boolean primary;
        private boolean loginAllowed;
        private boolean recoveryAllowed;
        private LocalDateTime verifiedAt;
        private LocalDateTime addedAt;
        private LocalDateTime updatedAt;
        private LocalDateTime recoveryEligibleAt;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withUserId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPrimary(boolean primary) {
            this.primary = primary;
            return this;
        }

        public Builder withLoginAllowed(boolean loginAllowed) {
            this.loginAllowed = loginAllowed;
            return this;
        }

        public Builder withRecoveryAllowed(boolean recoveryAllowed) {
            this.recoveryAllowed = recoveryAllowed;
            return this;
        }

        public Builder withVerifiedAt(LocalDateTime verifiedAt) {
            this.verifiedAt = verifiedAt;
            return this;
        }

        public Builder withAddedAt(LocalDateTime addedAt) {
            this.addedAt = addedAt;
            return this;
        }

        public Builder withUpdatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public Builder withRecoveryEligibleAt(LocalDateTime recoveryEligibleAt) {
            this.recoveryEligibleAt = recoveryEligibleAt;
            return this;
        }

        public UserEmail build() {
            return new UserEmail(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserEmail that))
            return false;
        return Objects.equals(id, that.id)
                && Objects.equals(userId, that.userId)
                && Objects.equals(email, that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, email);
    }

    @Override
    public String toString() {
        return "UserEmail{" +
                "id=" + id +
                ", userId=" + userId +
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
