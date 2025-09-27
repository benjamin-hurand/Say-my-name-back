package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

import com.saymyname.persistence.entity.attribute.AttributeEntity;

@Entity
@Table(name = "persons_attributes")
public class PersonAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeEntity attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @Column(name = "value", length = 255)
    private String value;

    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;

    @Column(name = "valid_to")
    private LocalDateTime validTo;

    /** MySQL: tinyint(1) NOT NULL DEFAULT 0 */
    @Column(name = "is_pending_delete", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean pendingDelete = false;

    public PersonAttributeEntity() {
        // boolean primitif => false par défaut
    }

    public PersonAttributeEntity(
            Long id,
            AttributeEntity attribute,
            PersonEntity person,
            String value,
            LocalDateTime validFrom,
            LocalDateTime validTo,
            boolean pendingDelete) {
        this.id = id;
        this.attribute = attribute;
        this.person = person;
        this.value = value;
        this.validFrom = validFrom;
        this.validTo = validTo;
        this.pendingDelete = pendingDelete;
    }

    // --- Getters / Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttributeEntity getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeEntity attribute) {
        this.attribute = attribute;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public LocalDateTime getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDateTime validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDateTime getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDateTime validTo) {
        this.validTo = validTo;
    }

    public boolean isPendingDelete() {
        return pendingDelete;
    }

    public void setPendingDelete(boolean pendingDelete) {
        this.pendingDelete = pendingDelete;
    }

    // --- equals / hashCode basés sur l'id ---
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PersonAttributeEntity))
            return false;
        PersonAttributeEntity that = (PersonAttributeEntity) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "PersonAttributeEntity{" +
                "id=" + id +
                ", attribute=" + (attribute != null ? attribute.getId() : null) +
                ", person=" + (person != null ? person.getId() : null) +
                ", value='" + value + '\'' +
                ", validFrom=" + validFrom +
                ", validTo=" + validTo +
                ", pendingDelete=" + pendingDelete +
                '}';
    }
}
