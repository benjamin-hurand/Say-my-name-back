package com.saymyname.persistence.entity.organization.invitation;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "invitation_usages", indexes = {
        @Index(name = "ix_usage_org_time", columnList = "organization_id,used_at"),
        @Index(name = "ix_usage_org_user", columnList = "organization_id,user_id"),
        @Index(name = "ix_usage_org_inv", columnList = "organization_id,invitation_id")
})
public class InvitationUsageEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ==== Colonnes scalaires (écrites par JPA) ====
    @Column(name = "invitation_id", nullable = false)
    private Long invitationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "person_id")
    private Long personId; // nullable

    @Column(name = "used_at", nullable = false, insertable = false)
    private LocalDateTime usedAt;

    @Column(name = "used_ip", columnDefinition = "varbinary(16)")
    private byte[] usedIp;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    // ==== Associations (lecture seule ; les colonnes sont mappées ailleurs) ====

    // FK composite → invitations(id, organization_id)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumns({
            @JoinColumn(name = "invitation_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", insertable = false, updatable = false, nullable = false)
    })
    private InvitationEntity invitation;

    // User qui consomme
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", insertable = false, updatable = false, nullable = false)
    private UserEntity user;

    // Person optionnelle (FK composite)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "person_id", referencedColumnName = "id", insertable = false, updatable = false),
            @JoinColumn(name = "organization_id", referencedColumnName = "organization_id", insertable = false, updatable = false)
    })
    private PersonEntity person;

    // ==== Getters / Setters ====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getInvitationId() {
        return invitationId;
    }

    public void setInvitationId(Long invitationId) {
        this.invitationId = invitationId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public LocalDateTime getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(LocalDateTime usedAt) {
        this.usedAt = usedAt;
    }

    public byte[] getUsedIp() {
        return usedIp;
    }

    public void setUsedIp(byte[] usedIp) {
        this.usedIp = usedIp;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public InvitationEntity getInvitation() {
        return invitation;
    }

    /** Associe l'entité parent et synchronise la FK scalaire. */
    public void setInvitation(InvitationEntity invitation) {
        this.invitation = invitation;
        this.invitationId = (invitation != null ? invitation.getId() : null);
        // NB: organization_id est géré par BaseOrgScoped ; on suppose déjà cohérent.
    }

    public UserEntity getUser() {
        return user;
    }

    /** Associe l'utilisateur et synchronise la FK scalaire. */
    public void setUser(UserEntity user) {
        this.user = user;
        this.userId = (user != null ? user.getId() : null);
    }

    public PersonEntity getPerson() {
        return person;
    }

    /** Associe la person et synchronise la FK scalaire. */
    public void setPerson(PersonEntity person) {
        this.person = person;
        this.personId = (person != null ? person.getId() : null);
    }

    // ==== equals / hashCode (id only) ====

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof InvitationUsageEntity))
            return false;
        InvitationUsageEntity that = (InvitationUsageEntity) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "InvitationUsageEntity{" +
                "id=" + id +
                ", organizationId=" + getOrganizationId() +
                ", invitationId=" + invitationId +
                ", userId=" + userId +
                ", personId=" + personId +
                ", usedAt=" + usedAt +
                '}';
    }
}
