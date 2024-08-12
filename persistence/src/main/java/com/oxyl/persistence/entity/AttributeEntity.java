package com.oxyl.persistence.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "attributes")
public class AttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "attribute_name", nullable = false, length = 255)
    private String attributeName;

    @Column(name = "unique", nullable = false)
    private boolean unique;

    @Column(name = "filter", nullable = false)
    private boolean filter;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public AttributeEntity() {}

    public AttributeEntity(String attributeName, boolean unique) {
        this.attributeName = attributeName;
        this.unique = unique;
    }

    public AttributeEntity(long id, String attributeName, boolean unique, boolean filter) {
        this.id = id;
        this.attributeName = attributeName;
        this.unique = unique;
        this.filter = filter;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isFilter() {
        return filter;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeEntity)) return false;
        AttributeEntity that = (AttributeEntity) o;
        return id == that.id &&
                unique == that.unique &&
                filter == that.filter &&
                Objects.equals(attributeName, that.attributeName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, attributeName, unique, filter);
    }

    @Override
    public String toString() {
        return "AttributeEntity{" +
                "id=" + id +
                ", attributeName='" + attributeName + '\'' +
                ", unique=" + unique +
                ", filter=" + filter +
                '}';
    }
}
