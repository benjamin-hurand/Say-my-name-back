// src/main/java/com/saymyname/persistence/entity/organization/ChangeRequestItemEntity.java
package com.saymyname.persistence.entity.organization;

import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "change_request_items")
public class ChangeRequestItemEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;

    /** Parent (enveloppe) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "change_request_id", nullable = false)
    private ChangeRequestEntity changeRequest;

    /** Action demandée */
    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 16)
    private ChangeAction action;

    /** UPDATE/DELETE : PA requis ; CREATE : null */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_attribute_id")
    private PersonAttributeEntity personAttribute;

    /** Valeur proposée (pour CREATE/UPDATE). NULL pour DELETE. */
    @Column(name = "proposed_value", length = 512)
    private String proposedValue;

    /** Statut de résolution (par item) */
    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_status", nullable = false, length = 16)
    private ChangeRequestItemStatus resolutionStatus = ChangeRequestItemStatus.PENDING;

    /** Commentaire de résolution (par item, optionnel) */
    @Column(name = "resolution_comment", length = 512)
    private String resolutionComment;

    // --- Constructeurs ---
    public ChangeRequestItemEntity() {
    }

    public ChangeRequestItemEntity(
            Long id,
            ChangeRequestEntity changeRequest,
            ChangeAction action,
            PersonAttributeEntity personAttribute,
            String proposedValue,
            ChangeRequestItemStatus resolutionStatus,
            String resolutionComment) {
        this.id = id;
        this.changeRequest = changeRequest;
        this.action = action;
        this.personAttribute = personAttribute;
        this.proposedValue = proposedValue;
        this.resolutionStatus = (resolutionStatus != null ? resolutionStatus : ChangeRequestItemStatus.PENDING);
        this.resolutionComment = resolutionComment;
    }

    // --- Getters / Setters ---
    public Long getId() {
        return id;
    }

    public ChangeRequestEntity getChangeRequest() {
        return changeRequest;
    }

    public void setChangeRequest(ChangeRequestEntity changeRequest) {
        this.changeRequest = changeRequest;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ChangeAction getAction() {
        return action;
    }

    public void setAction(ChangeAction action) {
        this.action = action;
    }

    public PersonAttributeEntity getPersonAttribute() {
        return personAttribute;
    }

    public void setPersonAttribute(PersonAttributeEntity personAttribute) {
        this.personAttribute = personAttribute;
    }

    public String getProposedValue() {
        return proposedValue;
    }

    public void setProposedValue(String proposedValue) {
        this.proposedValue = proposedValue;
    }

    public ChangeRequestItemStatus getResolutionStatus() {
        return resolutionStatus;
    }

    public void setResolutionStatus(ChangeRequestItemStatus resolutionStatus) {
        this.resolutionStatus = resolutionStatus;
    }

    public String getResolutionComment() {
        return resolutionComment;
    }

    public void setResolutionComment(String resolutionComment) {
        this.resolutionComment = resolutionComment;
    }

    // --- equals / hashCode sur id ---
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof ChangeRequestItemEntity))
            return false;
        ChangeRequestItemEntity that = (ChangeRequestItemEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    // toString limité
    @Override
    public String toString() {
        return "ChangeRequestItemEntity{" +
                "id=" + id +
                ", crId=" + (changeRequest != null ? changeRequest.getId() : null) +
                ", action=" + action +
                ", personAttributeId=" + (personAttribute != null ? personAttribute.getId() : null) +
                ", resolutionStatus=" + resolutionStatus +
                '}';
    }
}
