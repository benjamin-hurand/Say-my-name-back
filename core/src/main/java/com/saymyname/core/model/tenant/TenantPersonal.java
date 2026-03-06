package com.saymyname.core.model.tenant;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.tenant.TenantKind;

public final class TenantPersonal implements Tenant {

    private final Long id;
    private final Long ownerUserId;
    private final LocalDateTime createdAt;

    private TenantPersonal(Builder b) {
        this.id = b.id;
        this.ownerUserId = b.ownerUserId;
        this.createdAt = b.createdAt;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public TenantKind getKind() {
        return TenantKind.PERSONAL;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Long id;
        private Long ownerUserId;
        private LocalDateTime createdAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder ownerUserId(Long ownerUserId) {
            this.ownerUserId = ownerUserId;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TenantPersonal build() {
            return new TenantPersonal(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TenantPersonal that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}