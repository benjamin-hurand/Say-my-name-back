package com.saymyname.persistence.entity.organization;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "photo_assignments", uniqueConstraints = {
        @UniqueConstraint(name = "uq_pa_scope", columnNames = {"tenant_id", "person_id", "scope_kind", "workspace_id", "team_id"})
}, indexes = {
        @Index(name = "idx_pa_tenant_person", columnList = "tenant_id,person_id"),
        @Index(name = "idx_pa_workspace", columnList = "workspace_id"),
        @Index(name = "idx_pa_team", columnList = "team_id"),
        @Index(name = "idx_pa_photo", columnList = "photo_id"),
        @Index(name = "idx_pa_tenant_photo", columnList = "tenant_id,photo_id")
})
public class PhotoAssignmentEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_kind", nullable = false, length = 16, columnDefinition = "enum('TENANT','WORKSPACE','TEAM')")
    private ScopeKind scopeKind;

    @Column(name = "workspace_id")
    private Long workspaceId;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "photo_id", nullable = false)
    private Long photoId;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    public enum ScopeKind {
        TENANT,
        WORKSPACE,
        TEAM
    }
}