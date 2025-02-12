package com.saymyname.core.model.people;

import java.util.Objects;

public class Attribute {
    private long id;
    private String name;
    private boolean unique;
    private boolean filter;
    private boolean sort;
    private boolean initializable;

    // Constructeur par défaut
    public Attribute() {}

    // Constructeur privé utilisé par le Builder
    private Attribute(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.unique = builder.unique;
        this.filter = builder.filter;
        this.sort = builder.sort;
        this.initializable = builder.initializable;
    }

    // Getters
    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isFilter() {
        return filter;
    }

    public boolean isSort() {
        return sort;
    }

    public boolean isInitializable() {
        return initializable;
    }

    // Setters
    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public void setFilter(boolean filter) {
        this.filter = filter;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public void setInitializable(boolean initializable) {
        this.initializable = initializable;
    }

    // Builder Pattern
    public static class Builder {
        private long id;
        private String name;
        private boolean unique;
        private boolean filter;
        private boolean sort;
        private boolean initializable; // Nouveau champ

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withUnique(boolean unique) {
            this.unique = unique;
            return this;
        }

        public Builder withFilter(boolean filter) {
            this.filter = filter;
            return this;
        }

        public Builder withSort(boolean sort) {
            this.sort = sort;
            return this;
        }

        public Builder withInitializable(boolean initializable) {
            this.initializable = initializable;
            return this;
        }

        public Attribute build() {
            return new Attribute(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;
        Attribute attribute = (Attribute) o;
        return id == attribute.id &&
               unique == attribute.unique &&
               filter == attribute.filter &&
               sort == attribute.sort &&
               initializable == attribute.initializable &&
               Objects.equals(name, attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, unique, filter, sort, initializable);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unique=" + unique +
                ", filter=" + filter +
                ", sort=" + sort +
                ", initializable=" + initializable +
                '}';
    }
}
