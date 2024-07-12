package com.oxyl.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "companies")
public class CompanyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "company_name", nullable = false, length = 255)
    private String companyName;

    @Column(name = "company_service", length = 255)
    private String companyService;

    @Column(name = "company_description", columnDefinition = "TEXT")
    private String companyDescription;

    @Column(name = "company_url", length = 255)
    private String companyUrl;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public CompanyEntity() {}

    public CompanyEntity(String companyName, String companyService, String companyDescription, String companyUrl) {
        this.companyName = companyName;
        this.companyService = companyService;
        this.companyDescription = companyDescription;
        this.companyUrl = companyUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyService() {
        return companyService;
    }

    public void setCompanyService(String companyService) {
        this.companyService = companyService;
    }

    public String getCompanyDescription() {
        return companyDescription;
    }

    public void setCompanyDescription(String companyDescription) {
        this.companyDescription = companyDescription;
    }

    public String getCompanyUrl() {
        return companyUrl;
    }

    public void setCompanyUrl(String companyUrl) {
        this.companyUrl = companyUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CompanyEntity that)) return false;
        return id == that.id &&
                Objects.equals(companyName, that.companyName) &&
                Objects.equals(companyService, that.companyService) &&
                Objects.equals(companyDescription, that.companyDescription) &&
                Objects.equals(companyUrl, that.companyUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, companyName, companyService, companyDescription, companyUrl);
    }

    @Override
    public String toString() {
        return "CompanyEntity{" +
                "id=" + id +
                ", companyName='" + companyName + '\'' +
                ", companyService='" + companyService + '\'' +
                ", companyDescription='" + companyDescription + '\'' +
                ", companyUrl='" + companyUrl + '\'' +
                '}';
    }
}
