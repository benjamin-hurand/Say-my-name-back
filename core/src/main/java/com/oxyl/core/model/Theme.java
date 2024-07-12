package com.oxyl.core.model;

import java.util.List;
import java.util.Objects;

public class Theme {
    private long id;
    private String title;
    private String description;
    private List<Attribute> attributes;

    public Theme() {}

    private Theme(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.description = builder.description;
        this.attributes = builder.attributes;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    public static class Builder {
        private long id;
        private String title;
        private String description;
        private List<Attribute> attributes;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withAttributes(List<Attribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Theme build() {
            return new Theme(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Theme)) return false;
        Theme theme = (Theme) o;
        return id == theme.id &&
                Objects.equals(title, theme.title) &&
                Objects.equals(description, theme.description) &&
                Objects.equals(attributes, theme.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, description, attributes);
    }

    @Override
    public String toString() {
        return "Theme{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", attributes=" + attributes +
                '}';
    }
}
