package com.saymyname.core.model.tenant;

import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.core.model.enums.tenant.TenantKind;

public final class TenantOrg implements Tenant {

    private final Long id;
    private final String key;
    private final String name;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private TenantOrg(Builder b) {
        this.id = b.id;
        this.key = b.key;
        this.name = b.name;
        this.active = b.active;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public TenantKind getKind() {
        return TenantKind.ORG;
    }

    @Override
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Long id;
        private String key;
        private String name;
        private boolean active = true;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder updatedAt(LocalDateTime updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public TenantOrg build() {
            return new TenantOrg(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof TenantOrg that))
            return false;
        if (id != null && that.id != null)
            return Objects.equals(id, that.id);
        return Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : Objects.hash(key);
    }

    @Override
    public String toString() {
        return "TenantOrg{id=%s,key='%s',name='%s',active=%s}"
                .formatted(id, key, name, active);
    }
}