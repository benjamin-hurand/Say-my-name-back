package com.oxyl.core.model.people;

import java.util.Objects;

public class Attribute {
    private long id;
    private String name;
    private boolean unique;
    private boolean filter;
    private boolean sort;

    public Attribute() {}

    private Attribute(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.unique = builder.unique;
        this.filter = builder.filter;
        this.sort = builder.sort;
    }

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

    public static class Builder {
        private long id;
        private String name;
        private boolean unique;
        private boolean filter;
        private boolean sort;

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

        public Attribute build() {
            return new Attribute(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Attribute)) return false;
        Attribute attribute = (Attribute) o;
        return id == attribute.id && unique == attribute.unique && filter == attribute.filter && sort == attribute.sort && Objects.equals(name, attribute.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, unique, filter);
    }

    @Override
    public String toString() {
        return "Attribute{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", unique=" + unique +
                ", filter=" + filter +
                ", sort=" + sort +
                '}';
    }
}
