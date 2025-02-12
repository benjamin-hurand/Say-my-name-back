package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "persons_attributes")
public class PersonAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private AttributeEntity attribute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private PersonEntity person;

    @Column(name = "value", length = 255)
    private String value;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public PersonAttributeEntity() {}

    public PersonAttributeEntity(long id, AttributeEntity attribute, String value) {
        this.id = id;
        this.attribute = attribute;
        this.value = value;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonAttributeEntity)) return false;
        PersonAttributeEntity that = (PersonAttributeEntity) o;
        return id == that.id &&
                Objects.equals(attribute, that.attribute) &&
                Objects.equals(person, that.person) &&
                Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attribute, person, value);
    }

    @Override
    public String toString() {
        return "PersonAttributeEntity{" +
                "id=" + id +
                ", attribute=" + attribute +
                ", person=" + person +
                ", value='" + value + '\'' +
                '}';
    }
}
