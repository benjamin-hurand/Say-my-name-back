package com.saymyname.persistence.entity;

import java.util.List;
import java.util.Objects;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "persons")
public class PersonEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "photo_id")
    private PhotoEntity photo;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PersonAttributeEntity> attributes;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public PersonEntity() {
    }

    public PersonEntity(long id, String firstName, String lastName, PhotoEntity photo, UserEntity user,
            List<PersonAttributeEntity> attributes) {
        this.id = id;
        this.photo = photo;
        this.user = user;
        this.attributes = attributes;
    }

    public PersonEntity(PhotoEntity photo, UserEntity user, List<PersonAttributeEntity> attributes) {
        this.photo = photo;
        this.user = user;
        this.attributes = attributes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public PhotoEntity getPhoto() {
        return photo;
    }

    public void setPhoto(PhotoEntity photo) {
        this.photo = photo;
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

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PersonEntity))
            return false;
        PersonEntity that = (PersonEntity) o;
        return id == that.id &&
                Objects.equals(photo, that.photo) &&
                Objects.equals(user, that.user) &&
                Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, photo, user, attributes);
    }

    @Override
    public String toString() {
        return "PersonEntity{" +
                "id=" + id +
                ", photo=" + photo +
                ", user=" + user +
                ", attributes=" + attributes +
                '}';
    }
}
