package com.saymyname.persistence.entity.workspace;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.saymyname.core.model.enums.PersonLinkStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceMemberStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceRole;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
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
@Table(name = "workspace_members", uniqueConstraints = {
        @UniqueConstraint(name = "uq_wm_workspace_person", columnNames = { "workspace_id", "person_id" })
}, indexes = {
        @Index(name = "ix_wm_ws_display_name", columnList = "workspace_id,display_name"),
        @Index(name = "ix_wm_ws_role", columnList = "workspace_id,role"),
        @Index(name = "ix_wm_ws_status", columnList = "workspace_id,status"),
        @Index(name = "ix_wm_pref_email", columnList = "preferred_email_id"),
        @Index(name = "idx_wm_tenant_ws_status", columnList = "tenant_id,workspace_id,status"),
        @Index(name = "idx_wm_tenant_user", columnList = "tenant_id,user_id"),
        @Index(name = "idx_wm_user", columnList = "user_id"),
        @Index(name = "fk_wm_person_tenant", columnList = "tenant_id,person_id")
})
public class WorkspaceMemberEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @EmbeddedId
    private WorkspaceMemberId id;

    /**
     * READ-ONLY relation (clé réelle = (tenant_id, workspace_id) en DB).
     * On persiste workspace_id via EmbeddedId + tenant_id via BaseTenantScoped.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_wm_workspace_tenant")),
            @JoinColumn(name = "workspace_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    @Getter // pas de setter (évite incohérences)
    private WorkspaceEntity workspace;

    /**
     * READ-ONLY relation (user_id est dans EmbeddedId).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_wm_user"))
    @Getter
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private WorkspaceRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private WorkspaceMemberStatus status;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "person_id")
    private Long personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_link_status", nullable = false, length = 16)
    private PersonLinkStatus personLinkStatus;

    @Column(name = "can_pick_person", nullable = false)
    private boolean canPickPerson;

    @Column(name = "can_create_person", nullable = false)
    private boolean canCreatePerson;

    @Column(name = "pick_requires_approval", nullable = false)
    private boolean pickRequiresApproval;

    @Column(name = "create_requires_approval", nullable = false)
    private boolean createRequiresApproval;

    /**
     * Source de vérité persistée (permet de créer/modifier sans hydrater
     * UserEmailEntity).
     */
    @Column(name = "preferred_email_id")
    private Long preferredEmailId;

    /**
     * READ-ONLY relation (FK sur preferred_email_id)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_email_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_wm_pref_email"))
    @Getter
    private UserEmailEntity preferredEmail;
}