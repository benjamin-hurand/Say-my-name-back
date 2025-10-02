package com.saymyname.persistence.entity.organization;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserOrganizationId implements Serializable {
    private Long userId;
    private Long organizationId;

    public UserOrganizationId() {
    }

    public UserOrganizationId(Long userId, Long organizationId) {
        this.userId = userId;
        this.organizationId = organizationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserOrganizationId that))
            return false;
        return Objects.equals(userId, that.userId) &&
                Objects.equals(organizationId, that.organizationId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, organizationId);
    }

    @Override
    public String toString() {
        return "UserOrganizationId{" +
                "userId=" + userId +
                ", organizationId=" + organizationId +
                '}';
    }
}
