package com.oxyl.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "promotions")
public class PromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "month")
    private Integer month;

    @Column(name = "year", nullable = false)
    private Integer year;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public PromotionEntity() {}

    public PromotionEntity(long id, Integer month, Integer year) {
        this.id = id;
        this.month = month;
        this.year = year;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PromotionEntity that)) return false;
        return getId() == that.getId() && Objects.equals(getMonth(), that.getMonth()) && Objects.equals(getYear(), that.getYear());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getMonth(), getYear());
    }

    @Override
    public String toString() {
        return "PromotionEntity{" +
                "id=" + id +
                ", month=" + month +
                ", year=" + year +
                '}';
    }
}
