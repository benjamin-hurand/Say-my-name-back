package com.saymyname.persistence.entity.organization.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class UserSubscriptionId implements Serializable {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    public UserSubscriptionId() {
    }

    public UserSubscriptionId(Long tenantId, Long userId, Long personId) {
        this.tenantId = tenantId;
        this.userId = userId;
        this.personId = personId;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getUserId() {
        return userId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof UserSubscriptionId that))
            return false;
        return Objects.equals(tenantId, that.tenantId)
                && Objects.equals(userId, that.userId)
                && Objects.equals(personId, that.personId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tenantId, userId, personId);
    }

    @Override
    public String toString() {
        return "UserSubscriptionId{" +
                "tenantId=" + tenantId +
                ", userId=" + userId +
                ", personId=" + personId +
                '}';
    }
}