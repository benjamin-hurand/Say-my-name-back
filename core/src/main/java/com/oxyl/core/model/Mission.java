package com.oxyl.core.model;

import java.time.LocalDate;
import java.util.Objects;

public class Mission {
 private long id;
 private String title;
 private String type;
 private LocalDate startDate;
 private LocalDate endDate;
 private String description;
 private Person person;
 private Company company;

 // Default constructor
 public Mission() {}

 private Mission(Builder builder) {
  this.id = builder.id;
  this.title = builder.title;
  this.type = builder.type;
  this.startDate = builder.startDate;
  this.endDate = builder.endDate;
  this.description = builder.description;
  this.person = builder.person;
  this.company = builder.company;
 }

 public long getId() {
  return id;
 }

 public void setId(long id) {
  this.id = id;
 }

 public String getTitle() {
  return title;
 }

 public void setTitle(String title) {
  this.title = title;
 }

 public String getType() {
  return type;
 }

 public void setType(String type) {
  this.type = type;
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

 public String getDescription() {
  return description;
 }

 public void setDescription(String description) {
  this.description = description;
 }

 public Person getPerson() {
  return person;
 }

 public void setPerson(Person person) {
  this.person = person;
 }

 public Company getCompany() {
  return company;
 }

 public void setCompany(Company company) {
  this.company = company;
 }

 public static class Builder {
  private long id;
  private String title;
  private String type;
  private LocalDate startDate;
  private LocalDate endDate;
  private String description;
  private Person person;
  private Company company;

  public Builder withId(long id) {
   this.id = id;
   return this;
  }

  public Builder withTitle(String title) {
   this.title = title;
   return this;
  }

  public Builder withType(String type) {
   this.type = type;
   return this;
  }

  public Builder withStartDate(LocalDate startDate) {
   this.startDate = startDate;
   return this;
  }

  public Builder withEndDate(LocalDate endDate) {
   this.endDate = endDate;
   return this;
  }

  public Builder withDescription(String description) {
   this.description = description;
   return this;
  }

  public Builder withPerson(Person person) {
   this.person = person;
   return this;
  }

  public Builder withCompany(Company company) {
   this.company = company;
   return this;
  }

  public Mission build() {
   return new Mission(this);
  }
 }

 @Override
 public boolean equals(Object o) {
  if (this == o) return true;
  if (!(o instanceof Mission)) return false;
  Mission mission = (Mission) o;
  return id == mission.id &&
          Objects.equals(title, mission.title) &&
          Objects.equals(type, mission.type) &&
          Objects.equals(startDate, mission.startDate) &&
          Objects.equals(endDate, mission.endDate) &&
          Objects.equals(description, mission.description) &&
          Objects.equals(person, mission.person) &&
          Objects.equals(company, mission.company);
 }

 @Override
 public int hashCode() {
  return Objects.hash(id, title, type, startDate, endDate, description, person, company);
 }

 @Override
 public String toString() {
  return "Mission{" +
          "id=" + id +
          ", title='" + title + '\'' +
          ", type='" + type + '\'' +
          ", startDate=" + startDate +
          ", endDate=" + endDate +
          ", description='" + description + '\'' +
          ", person=" + person +
          ", company=" + company +
          '}';
 }
}
