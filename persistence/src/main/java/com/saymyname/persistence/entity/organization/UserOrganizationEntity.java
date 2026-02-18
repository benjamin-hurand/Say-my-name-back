package com.saymyname.persistence.entity.organization;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import com.saymyname.persistence.entity.UserEmailEntity;
import com.saymyname.persistence.entity.UserEntity;
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
import java.time.LocalDateTime;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@FilterDef(name = "tenantFilter", parameters = @ParamDef(name = "tenantId", type = long.class))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_organizations", indexes = {
        @Index(name = "idx_uo_user_id", columnList = "user_id"),
        @Index(name = "idx_uo_tenant_status_role", columnList = "tenant_id,status,role"),
        @Index(name = "fk_uo_pref_email", columnList = "preferred_email_id")
})
public class UserOrganizationEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @EmbeddedId
    private UserOrganizationId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("tenantId")
    @JoinColumn(name = "tenant_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_uo_tenant_org"))
    private OrganizationEntity organization;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_uo_user"))
    private UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 32)
    private MemberRole role;

    @Column(name = "display_name", length = 80)
    private String displayName;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private MemberStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "preferred_email_id", foreignKey = @ForeignKey(name = "fk_uo_pref_email"))
    private UserEmailEntity preferredEmail;

    public enum MemberRole {
        VIEWER,
        EDITOR,
        ADMIN,
        OWNER
    }

    public enum PersonLinkStatus {
        NONE,
        PENDING,
        APPROVED,
        REJECTED
    }

    public enum MemberStatus {
        PENDING,
        ACTIVE,
        SUSPENDED,
        LEFT
    }
}