// core/src/main/java/com/saymyname/core/model/organization/Organization.java
package com.saymyname.core.model.organization;

import java.time.LocalDateTime;
import java.util.Objects;

public final class Organization {

    private final Long id;
    private final String key; // ex: "acme"
    private final String name; // ex: "ACME Inc."
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    private Organization(Builder b) {
        this.id = b.id;
        this.key = b.key;
        this.name = b.name;
        this.active = b.active;
        this.createdAt = b.createdAt;
        this.updatedAt = b.updatedAt;
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

        public Builder createdAt(LocalDateTime t) {
            this.createdAt = t;
            return this;
        }

        public Builder updatedAt(LocalDateTime t) {
            this.updatedAt = t;
            return this;
        }

        public Organization build() {
            return new Organization(this);
        }
    }

    public Long getId() {
        return id;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Organization that))
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
        return "Organization{id=%s,key='%s',name='%s',active=%s}"
                .formatted(id, key, name, active);
    }
}
