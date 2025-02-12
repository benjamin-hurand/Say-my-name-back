package com.saymyname.persistence.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
@Table(name = "photos")
public class PhotoEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToOne(mappedBy = "photo", fetch = FetchType.LAZY)
    private PersonEntity person;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public PhotoEntity() {}

    public PhotoEntity(long id, String photoUrl, LocalDateTime createdAt) {
        this.id = id;
        this.photoUrl = photoUrl;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public PersonEntity getPerson() {
        return person;
    }

    public void setPerson(PersonEntity person) {
        this.person = person;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PhotoEntity)) return false;
        PhotoEntity that = (PhotoEntity) o;
        return id == that.id &&
                Objects.equals(photoUrl, that.photoUrl) &&
                Objects.equals(createdAt, that.createdAt) &&
                Objects.equals(person, that.person);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, photoUrl, createdAt, person);
    }

    @Override
    public String toString() {
        return "PhotoEntity{" +
                "id=" + id +
                ", photoUrl='" + photoUrl + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", personEntity=" + person +
                '}';
    }
}
