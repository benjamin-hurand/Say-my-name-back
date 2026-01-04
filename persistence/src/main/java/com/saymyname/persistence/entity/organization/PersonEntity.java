package com.saymyname.persistence.entity.organization;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "persons")
public class PersonEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PersonAttributeEntity> attributes = new ArrayList<>();

    @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhotoEntity> photos = new ArrayList<>();

    // --- Constructors

    public PersonEntity() {
    }

    public PersonEntity(List<PersonAttributeEntity> attributes, List<PhotoEntity> photos) {
        this.attributes = attributes != null ? attributes : new ArrayList<>();
        this.photos = photos != null ? photos : new ArrayList<>();
    }

    // --- Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<PersonAttributeEntity> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<PersonAttributeEntity> attributes) {
        this.attributes = attributes != null ? attributes : new ArrayList<>();
    }

    public List<PhotoEntity> getPhotos() {
        return photos;
    }

    public void setPhotos(List<PhotoEntity> photos) {
        this.photos = photos != null ? photos : new ArrayList<>();
    }

    // --- equals & hashCode (basés uniquement sur id)

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PersonEntity))
            return false;
        PersonEntity that = (PersonEntity) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // --- toString (évite de charger les listes en LAZY)

    @Override
    public String toString() {
        return "PersonEntity{" +
                "id=" + id +
                ", attributesCount=" + (attributes != null ? attributes.size() : 0) +
                ", photosCount=" + (photos != null ? photos.size() : 0) +
                '}';
    }
}
