// persistence/src/main/java/com/saymyname/persistence/entity/organization/OrganizationEntity.java
package com.saymyname.persistence.entity.organization;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "organizations", uniqueConstraints = @UniqueConstraint(name = "uk_organizations_org_key", columnNames = {
        "org_key" }), indexes = {
                @Index(name = "idx_organizations_active", columnList = "is_active"),
                @Index(name = "idx_organizations_org_key", columnList = "org_key")
        })
public class OrganizationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "key" est un mot réservé MySQL – Hibernate gère bien, mais on peut être
    // explicite
    @Column(name = "org_key", nullable = false, length = 64)
    private String key;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        // Laisse MySQL peupler created_at par défaut, mais on met une valeur pour JPA
        if (createdAt == null)
            createdAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        // MySQL mettra updated_at via ON UPDATE CURRENT_TIMESTAMP ; ce set ne nuit pas.
        updatedAt = LocalDateTime.now();
    }

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof OrganizationEntity that))
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
        return "OrganizationEntity{id=%s,key='%s',name='%s',active=%s}"
                .formatted(id, key, name, active);
    }
}
