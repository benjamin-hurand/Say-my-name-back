package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.PhotoStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Modèle "plat" côté core.
 * - Listes toujours non null (vide par défaut).
 * - equals/hashCode basés uniquement sur id.
 *
 * NOTE:
 * Le lien User <-> Person est désormais porté par user_organizations.person_id,
 * donc Person ne contient plus de champ "user".
 */
public class Person {

    private Long id;

    private List<PersonAttribute> attributes = Collections.emptyList();
    private List<Photo> photos = Collections.emptyList();

    // Default constructor
    public Person() {
    }

    private Person(Builder builder) {
        this.id = builder.id;
        setAttributes(builder.attributes);
        setPhotos(builder.photos);
    }

    // --- Getters
    public Long getId() {
        return id;
    }

    public List<PersonAttribute> getAttributes() {
        return attributes;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    /**
     * Retourne la photo APPROVED de la personne s'il y en a une.
     */
    public Optional<Photo> getApprovedPhoto() {
        if (photos == null) {
            return Optional.empty();
        }
        return photos.stream()
                .filter(p -> p.getStatus() == PhotoStatus.APPROVED)
                .findFirst();
    }

    // --- Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setAttributes(List<PersonAttribute> attributes) {
        this.attributes = attributes != null ? new ArrayList<>(attributes) : Collections.emptyList();
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos != null ? new ArrayList<>(photos) : Collections.emptyList();
    }

    // --- Builder
    public static class Builder {
        private Long id;
        private List<PersonAttribute> attributes;
        private List<Photo> photos;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withAttributes(List<PersonAttribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder withPhotos(List<Photo> photos) {
            this.photos = photos;
            return this;
        }

        public Person build() {
            return new Person(this);
        }
    }

    // --- equals/hashCode sur id uniquement
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Person))
            return false;
        Person that = (Person) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", attributes=" + (attributes != null ? attributes.size() : 0) + " items" +
                ", photos=" + (photos != null ? photos.size() : 0) + " items" +
                '}';
    }
}
