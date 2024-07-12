package com.oxyl.persistence.entity;

import  com.oxyl.persistence.entity.PersonEntity;
import  com.oxyl.persistence.entity.CompanyEntity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "missions")
public class MissionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "mission_title", nullable = false, length = 50)
    private String missionTitle;

    @Column(name = "mission_type", length = 50)
    private String missionType;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "mission_description", columnDefinition = "TEXT")
    private String missionDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id")
    private PersonEntity person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private CompanyEntity company;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public MissionEntity() {}

    public MissionEntity(String missionTitle, String missionType, LocalDate startDate, LocalDate endDate, String missionDescription, PersonEntity person, CompanyEntity company) {
        this.missionTitle = missionTitle;
        this.missionType = missionType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.missionDescription = missionDescription;
        this.person = person;
        this.company = company;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMissionTitle() {
        return missionTitle;
    }

    public void setMissionTitle(String missionTitle) {
        this.missionTitle = missionTitle;
    }

    public String getMissionType() {
        return missionType;
    }

    public void setMissionType(String missionType) {
        this.missionType = missionType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getMissionDescription() {
        return missionDescription;
    }

    public void setMissionDescription(String missionDescription) {
        this.missionDescription = missionDescription;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public CompanyEntity getCompany() {
        return company;
    }

    public void setCompany(CompanyEntity company) {
        this.company = company;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MissionEntity)) return false;
        MissionEntity that = (MissionEntity) o;
        return id == that.id &&
                Objects.equals(missionTitle, that.missionTitle) &&
                Objects.equals(missionType, that.missionType) &&
                Objects.equals(startDate, that.startDate) &&
                Objects.equals(endDate, that.endDate) &&
                Objects.equals(missionDescription, that.missionDescription) &&
                Objects.equals(person, that.person) &&
                Objects.equals(company, that.company);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, missionTitle, missionType, startDate, endDate, missionDescription, person, company);
    }

    @Override
    public String toString() {
        return "MissionEntity{" +
                "id=" + id +
                ", missionTitle='" + missionTitle + '\'' +
                ", missionType='" + missionType + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", missionDescription='" + missionDescription + '\'' +
                ", person=" + person +
                ", company=" + company +
                '}';
    }
}
