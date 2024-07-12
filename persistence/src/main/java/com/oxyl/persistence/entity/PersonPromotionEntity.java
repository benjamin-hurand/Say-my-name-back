package com.oxyl.persistence.entity;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name = "promotion_persons")
public class PersonPromotionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "person_id", nullable = false)
    private PersonEntity person;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    private PromotionEntity promotion;

    @Column(name = "type", nullable = false, length = 255)
    private String type;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public PersonPromotionEntity() {}

    public PersonPromotionEntity(long id, PersonEntity person, PromotionEntity promotion, String type) {
        this.id = id;
        this.person = person;
        this.promotion = promotion;
        this.type = type;
    }

    public PersonPromotionEntity(PersonEntity person, PromotionEntity promotion, String type) {
        this.person = person;
        this.promotion = promotion;
        this.type = type;
    }

    public PersonPromotionEntity(long id, PromotionEntity promotion, String type) {
        this.id = id;
        this.promotion = promotion;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    public PromotionEntity getPromotion() {
        return promotion;
    }

    public void setPromotion(PromotionEntity promotion) {
        this.promotion = promotion;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonPromotionEntity)) return false;
        PersonPromotionEntity that = (PersonPromotionEntity) o;
        return id == that.id &&
                Objects.equals(person, that.person) &&
                Objects.equals(promotion, that.promotion) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, person, promotion, type);
    }

    @Override
    public String toString() {
        return "PersonPromotionEntity{" +
                "id=" + id +
                ", person=" + person +
                ", promotion=" + promotion +
                ", type='" + type + '\'' +
                '}';
    }
}
