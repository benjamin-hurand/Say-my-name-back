package com.oxyl.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "themes_attributes")
public class ThemeAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "operator")
    private String operator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "theme_id")
    private ThemeEntity theme;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private AttributeEntity attribute;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public ThemeAttributeEntity() {}

    public ThemeAttributeEntity(ThemeEntity theme, AttributeEntity attribute) {
        this.theme = theme;
        this.attribute = attribute;
    }

    public ThemeAttributeEntity(long id, String operator, AttributeEntity attribute) {
        this.id = id;
        this.operator = operator;
        this.attribute = attribute;
    }

    public ThemeAttributeEntity(long id, String operator, ThemeEntity theme, AttributeEntity attribute) {
        this.id = id;
        this.operator = operator;
        this.theme = theme;
        this.attribute = attribute;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public ThemeEntity getTheme() {
        return theme;
    }

    public void setTheme(ThemeEntity theme) {
        this.theme = theme;
    }

    public AttributeEntity getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeEntity attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThemeAttributeEntity)) return false;
        ThemeAttributeEntity that = (ThemeAttributeEntity) o;
        return id == that.id &&
                Objects.equals(theme, that.theme) &&
                Objects.equals(attribute, that.attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, theme, attribute);
    }

    @Override
    public String toString() {
        return "ThemeAttributesEntity{" +
                "id=" + id +
                ", theme=" + theme +
                ", attribute=" + attribute +
                '}';
    }
}
