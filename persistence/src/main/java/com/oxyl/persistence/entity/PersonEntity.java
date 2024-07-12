package com.oxyl.persistence.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.List;

@Entity
@Table(name = "persons")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 255)
    private String lastName;

    @OneToMany(fetch= FetchType.LAZY, mappedBy = "person", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoEntity> photos;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PersonAttributeEntity> attributes;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PersonPromotionEntity> promotions;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public PersonEntity() {}

    public PersonEntity(long id, String firstName, String lastName, List<PhotoEntity> photos, UserEntity user, List<PersonAttributeEntity> attributes, List<PersonPromotionEntity> promotions) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photos = photos;
        this.user = user;
        this.attributes = attributes;
        this.promotions = promotions;
    }

    public PersonEntity(String firstName, String lastName, List<PhotoEntity> photos, UserEntity user, List<PersonAttributeEntity> attributes, List<PersonPromotionEntity> promotions) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.photos = photos;
        this.user = user;
        this.attributes = attributes;
        this.promotions = promotions;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<PhotoEntity> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoEntity> photos) {
        this.photos = photos;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<PersonAttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PersonAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    public List<PersonPromotionEntity> getPromotions() {
        return promotions;
    }

    public void setPromotions(List<PersonPromotionEntity> promotions) {
        this.promotions = promotions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PersonEntity)) return false;
        PersonEntity that = (PersonEntity) o;
        return id == that.id &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(photos, that.photos) &&
                Objects.equals(user, that.user) &&
                Objects.equals(attributes, that.attributes) &&
                Objects.equals(promotions, that.promotions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, photos, user, attributes, promotions);
    }

    @Override
    public String toString() {
        return "PersonEntity{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photos=" + photos +
                ", user=" + user +
                ", attributes=" + attributes +
                ", promotions=" + promotions +
                '}';
    }
}
