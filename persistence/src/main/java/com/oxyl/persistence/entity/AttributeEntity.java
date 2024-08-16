package com.oxyl.persistence.entity;

import jakarta.persistence.*;
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

    @Column(name = "sort", nullable = false)
    private boolean sort;

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

    public AttributeEntity(long id, String attributeName, boolean unique, boolean filter, boolean sort) {
        this.id = id;
        this.attributeName = attributeName;
        this.unique = unique;
        this.filter = filter;
        this.sort = sort;
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

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AttributeEntity that)) return false;
        return getId() == that.getId() && isUnique() == that.isUnique() && isFilter() == that.isFilter() && isSort() == that.isSort() && Objects.equals(getAttributeName(), that.getAttributeName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getAttributeName(), isUnique(), isFilter(), isSort());
    }

    @Override
    public String toString() {
        return "AttributeEntity{" +
                "id=" + id +
                ", attributeName='" + attributeName + '\'' +
                ", unique=" + unique +
                ", filter=" + filter +
                ", sort=" + sort +
                '}';
    }
}
