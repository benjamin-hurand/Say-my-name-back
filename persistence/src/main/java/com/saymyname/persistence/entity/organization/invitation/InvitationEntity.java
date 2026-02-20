package com.saymyname.persistence.entity.organization.invitation;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Table(name = "invitations", uniqueConstraints = {
        @UniqueConstraint(name = "uq_invit_token", columnNames = {"token_hash"})
}, indexes = {
        @Index(name = "idx_invit_tenant", columnList = "tenant_id"),
        @Index(name = "fk_invit_created_by", columnList = "created_by"),
        @Index(name = "fk_invit_accepted_by", columnList = "accepted_by")
})
public class InvitationEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 16)
    private InvitationType type;

    @Column(name = "label", length = 80)
    private String label;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "constraints_json", columnDefinition = "json")
    private String constraintsJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 64)
    private InvitationRole role;

    @Column(name = "email", length = 320)
    private String email;

    @Column(name = "person_id")
    private Long personId;

    @Column(name = "token_hash", nullable = false, columnDefinition = "varbinary(32)")
    private byte[] tokenHash;

    @Column(name = "pin_hash_phc", length = 255)
    private String pinHashPhc;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "uses_count", nullable = false)
    private int usesCount;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_invit_created_by"))
    private UserEntity createdBy;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "accepted_by", foreignKey = @ForeignKey(name = "fk_invit_accepted_by"))
    private UserEntity acceptedBy;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    public enum InvitationRole {
        VIEWER,
        EDITOR,
        ADMIN,
        OWNER
    }
}
