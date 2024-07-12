package com.oxyl.core.model;

import java.util.Objects;

public class Company {
    private long id;
    private String name;
    private String service;
    private String description;
    private String url;

    public Company() {}

    private Company(Builder builder) {
        this.id = builder.id;
        this.name = builder.name;
        this.service = builder.service;
        this.description = builder.description;
        this.url = builder.url;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getService() {
        return service;
    }

    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return url;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setService(String service) {
        this.service = service;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public static class Builder {
        private long id;
        private String name;
        private String service;
        private String description;
        private String url;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withService(String service) {
            this.service = service;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withUrl(String url) {
            this.url = url;
            return this;
        }

        public Company build() {
            return new Company(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Company)) return false;
        Company company = (Company) o;
        return id == company.id && Objects.equals(name, company.name) && Objects.equals(service, company.service) && Objects.equals(description, company.description) && Objects.equals(url, company.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, service, description, url);
    }

    @Override
    public String toString() {
        return "Company{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", service='" + service + '\'' +
                ", description='" + description + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
