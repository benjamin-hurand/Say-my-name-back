package com.saymyname.persistence.entity.workspace;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "workspace_persons", indexes = {
        @Index(name = "idx_wp_person", columnList = "person_id"),
        @Index(name = "idx_wp_workspace", columnList = "workspace_id"),
        @Index(name = "idx_wp_tenant_person", columnList = "tenant_id,person_id"),
        @Index(name = "idx_wp_tenant_ws", columnList = "tenant_id,workspace_id")
})
public class WorkspacePersonEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @EmbeddedId
    private WorkspacePersonId id;

    /**
     * READ-ONLY relation workspace tenant-safe (si tu as fk_wp_workspace_tenant).
     * Persist via: BaseTenantScoped.tenantId + id.workspaceId
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_wp_workspace_tenant")),
            @JoinColumn(name = "workspace_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    private WorkspaceEntity workspace;

    /**
     * READ-ONLY relation person tenant-safe via (tenant_id, person_id).
     * Persist via: BaseTenantScoped.tenantId + id.personId
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_wp_person_tenant")),
            @JoinColumn(name = "person_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    private PersonEntity person;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Source de vérité persistée. Permet d'écrire sans hydrater UserEntity.
     */
    @Column(name = "added_by")
    private Long addedById;

    /**
     * READ-ONLY relation.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_wp_added_by"))
    private UserEntity addedBy;
}