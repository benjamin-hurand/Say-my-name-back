package com.saymyname.persistence.entity;

import com.saymyname.core.model.enums.ChangeAction;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "change_request_items")
public class ChangeRequestItemEntity {

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

    // --- Constructeurs ---
    public ChangeRequestItemEntity() {
    }

    public ChangeRequestItemEntity(
            Long id,
            ChangeRequestEntity changeRequest,
            ChangeAction action,
            PersonAttributeEntity personAttribute,
            String proposedValue) {
        this.id = id;
        this.changeRequest = changeRequest;
        this.action = action;
        this.personAttribute = personAttribute;
        this.proposedValue = proposedValue;
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
                '}';
    }
}
