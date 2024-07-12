package com.oxyl.persistence.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "themes")
public class ThemeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "theme_title", nullable = false, length = 255)
    private String themeTitle;

    @Column(name = "theme_description", columnDefinition = "TEXT")
    private String themeDescription;

    @ManyToMany
    @JoinTable(
            name = "themes_attributes",
            joinColumns = @JoinColumn(name = "theme_id"),
            inverseJoinColumns = @JoinColumn(name = "attribute_id")
    )
    private List<AttributeEntity> attributes;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public ThemeEntity() {}

    public ThemeEntity(String themeTitle, String themeDescription) {
        this.themeTitle = themeTitle;
        this.themeDescription = themeDescription;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getThemeTitle() {
        return themeTitle;
    }

    public void setThemeTitle(String themeTitle) {
        this.themeTitle = themeTitle;
    }

    public String getThemeDescription() {
        return themeDescription;
    }

    public void setThemeDescription(String themeDescription) {
        this.themeDescription = themeDescription;
    }

    public List<AttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<AttributeEntity> attributes) {
        this.attributes = attributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ThemeEntity)) return false;
        ThemeEntity that = (ThemeEntity) o;
        return id == that.id &&
                Objects.equals(themeTitle, that.themeTitle) &&
                Objects.equals(themeDescription, that.themeDescription) &&
                Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, themeTitle, themeDescription, attributes);
    }

    @Override
    public String toString() {
        return "ThemeEntity{" +
                "id=" + id +
                ", themeTitle='" + themeTitle + '\'' +
                ", themeDescription='" + themeDescription + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
