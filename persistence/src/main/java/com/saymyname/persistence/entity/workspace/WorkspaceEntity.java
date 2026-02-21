package com.saymyname.persistence.entity.workspace;

import com.saymyname.persistence.entity.TenantEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "workspaces", uniqueConstraints = {
                @UniqueConstraint(name = "uq_ws_tenant_name", columnNames = { "tenant_id", "name" })
}, indexes = {
                @Index(name = "idx_ws_tenant", columnList = "tenant_id")
})
public class WorkspaceEntity extends BaseTenantScoped {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        /**
         * Relation pratique (lecture). La colonne tenant_id est portée par
         * BaseTenantScoped.
         * Source de vérité = tenantId (BaseTenantScoped).
         */
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "tenant_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_ws_tenant"))
        private TenantEntity tenant;

        @Column(name = "name", length = 255)
        private String name;

        @Setter
        @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) default 1")
        private boolean active;

        @CreationTimestamp
        @Column(name = "created_at", nullable = false, updatable = false)
        private Instant createdAt;

        @UpdateTimestamp
        @Column(name = "updated_at", nullable = false)
        private Instant updatedAt;
}