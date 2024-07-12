package com.oxyl.core.model;

import java.util.List;
import java.util.Objects;

public class Promotion {
    private long id;
    private Integer month;
    private Integer year;
    private List<Person> persons;

    public Promotion() {}

    private Promotion(Builder builder) {
        this.id = builder.id;
        this.month = builder.month;
        this.year = builder.year;
        this.persons = builder.persons;
    }

    public long getId() {
        return id;
    }

    public Integer getMonth() {
        return month;
    }

    public Integer getYear() {
        return year;
    }

    public List<Person> getPersons() {
        return persons;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public void setPersons(List<Person> persons) {
        this.persons = persons;
    }

    public static class Builder {
        private long id;
        private Integer month;
        private Integer year;
        private List<Person> persons;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withMonth(Integer month) {
            this.month = month;
            return this;
        }

        public Builder withYear(Integer year) {
            this.year = year;
            return this;
        }

        public Builder withPersons(List<Person> persons) {
            this.persons = persons;
            return this;
        }

        public Promotion build() {
            return new Promotion(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Promotion)) return false;
        Promotion that = (Promotion) o;
        return id == that.id &&
                Objects.equals(month, that.month) &&
                Objects.equals(year, that.year) &&
                Objects.equals(persons, that.persons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, month, year, persons);
    }

    @Override
    public String toString() {
        return "Promotion{" +
                "id=" + id +
                ", month='" + month + '\'' +
                ", year='" + year + '\'' +
                ", persons=" + persons +
                '}';
    }
}
