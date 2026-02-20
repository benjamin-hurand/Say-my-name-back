package com.saymyname.persistence.entity.workspace;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
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
@Table(name = "workspace_members", uniqueConstraints = {
        @UniqueConstraint(name = "uq_wm_workspace_person", columnNames = {"workspace_id", "person_id"})
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wm_workspace"))
    private WorkspaceEntity workspace;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wm_user"))
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private WorkspaceRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private WorkspaceMemberStatus status;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "person_id")
    private Long personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "person_link_status", nullable = false, length = 16, columnDefinition = "varchar(16) default 'NONE'")
    private PersonLinkStatus personLinkStatus;

    @Column(name = "can_pick_person", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean canPickPerson;

    @Column(name = "can_create_person", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean canCreatePerson;

    @Column(name = "pick_requires_approval", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean pickRequiresApproval;

    @Column(name = "create_requires_approval", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean createRequiresApproval;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_email_id", foreignKey = @ForeignKey(name = "fk_wm_pref_email"))
    private UserEmailEntity preferredEmail;

    public enum PersonLinkStatus {
        NONE,
        PENDING,
        APPROVED,
        REJECTED
    }
}
