package com.saymyname.persistence.entity;

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

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "photo_id")
    private PhotoEntity photo;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private UserEntity user;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<PersonAttributeEntity> attributes;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public PersonEntity() {}

    public PersonEntity(long id, String firstName, String lastName, PhotoEntity photo, UserEntity user, List<PersonAttributeEntity> attributes) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.photo = photo;
        this.user = user;
        this.attributes = attributes;
    }

    public PersonEntity(String firstName, String lastName, PhotoEntity photo, UserEntity user, List<PersonAttributeEntity> attributes) {
        this.firstName = firstName;
        this.lastName = lastName;
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

    public PhotoEntity getphoto() {
        return photo;
    }

    public void setphoto(PhotoEntity photo) {
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
        if (this == o) return true;
        if (!(o instanceof PersonEntity)) return false;
        PersonEntity that = (PersonEntity) o;
        return id == that.id &&
                Objects.equals(firstName, that.firstName) &&
                Objects.equals(lastName, that.lastName) &&
                Objects.equals(photo, that.photo) &&
                Objects.equals(user, that.user) &&
                Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, firstName, lastName, photo, user, attributes);
    }

    @Override
    public String toString() {
        return "PersonEntity{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", photo=" + photo +
                ", user=" + user +
                ", attributes=" + attributes +
                '}';
    }
}
