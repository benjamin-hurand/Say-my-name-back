package com.saymyname.core.model.organization;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.MemberStatus;
import com.saymyname.core.model.enums.OrgRole;

/**
 * Projection métier "ligne de membre" pour l'écran admin Membres & Invitations.
 * Regroupe des infos User + UserOrganization (+ plus tard Person).
 */
public class OrgMemberRow {

    private Long userId;
    private Long tenantId;
    private String displayName;
    private String email;
    private OrgRole role;
    private Long personId;
    private String personLabel;
    private MemberStatus status;
    private LocalDateTime joinedAt;

    private OrgMemberRow(Builder builder) {
        this.userId = builder.userId;
        this.tenantId = builder.tenantId;
        this.displayName = builder.displayName;
        this.email = builder.email;
        this.role = builder.role;
        this.personId = builder.personId;
        this.personLabel = builder.personLabel;
        this.status = builder.status;
        this.joinedAt = builder.joinedAt;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public OrgRole getRole() {
        return role;
    }

    public Long getPersonId() {
        return personId;
    }

    public String getPersonLabel() {
        return personLabel;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long userId;
        private Long tenantId;
        private String displayName;
        private String email;
        private OrgRole role;
        private Long personId;
        private String personLabel;
        private MemberStatus status;
        private LocalDateTime joinedAt;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder displayName(String displayName) {
            this.displayName = displayName;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder role(OrgRole role) {
            this.role = role;
            return this;
        }

        public Builder personId(Long personId) {
            this.personId = personId;
            return this;
        }

        public Builder personLabel(String personLabel) {
            this.personLabel = personLabel;
            return this;
        }

        public Builder status(MemberStatus status) {
            this.status = status;
            return this;
        }

        public Builder joinedAt(LocalDateTime joinedAt) {
            this.joinedAt = joinedAt;
            return this;
        }

        public OrgMemberRow build() {
            return new OrgMemberRow(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrgMemberRow that))
            return false;
        return Objects.equals(userId, that.userId)
                && Objects.equals(tenantId, that.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, tenantId);
    }

    @Override
    public String toString() {
        return "OrgMemberRow{" +
                "userId=" + userId +
                ", tenantId=" + tenantId +
                ", displayName='" + displayName + '\'' +
                ", email='" + email + '\'' +
                ", role=" + role +
                ", personId=" + personId +
                ", personLabel='" + personLabel + '\'' +
                ", status=" + status +
                ", joinedAt=" + joinedAt +
                '}';
    }
}
