// src/main/java/com/saymyname/core/model/persondirectory/PersonCard.java
package com.saymyname.core.model.persondirectory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class PersonCard {

    private Long idPerson;
    private String photoStorageKey;

    @JsonProperty("attributes")
    private List<AttributeValueView> attributes = new ArrayList<>();

    /** Suivi (côté utilisateur non admin) */
    private boolean followed;

    public PersonCard() {
    }

    private PersonCard(Builder b) {
        this.idPerson = b.idPerson;
        this.photoStorageKey = b.photoStorageKey;
        this.attributes = b.attributes;
        this.followed = b.followed;
    }

    public Long getIdPerson() {
        return idPerson;
    }

    public String getPhotoStorageKey() {
        return photoStorageKey;
    }

    /** Accès direct à la liste unifiée (si tu veux aussi l’exposer côté JSON). */
    public List<AttributeValueView> getAttributes() {
        return attributes;
    }

    public boolean isFollowed() {
        return followed;
    }

    public void setIdPerson(Long idPerson) {
        this.idPerson = idPerson;
    }

    public void setPhotoStorageKey(String photoStorageKey) {
        this.photoStorageKey = photoStorageKey;
    }

    public void setAttributes(List<AttributeValueView> attributes) {
        this.attributes = attributes;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

    // --------- Rétro-compat JSON (legacy fields) ---------
    // Tant que le front consomme encore primary/extra, on les expose en JSON,
    // mais on les calcule à partir de la liste unifiée.

    @JsonProperty("primaryAttributes")
    public List<AttributeValueView> getPrimaryAttributes() {
        return attributes.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIdentitySource()))
                .sorted(ordering())
                .collect(Collectors.toList());
    }

    @JsonProperty("extraAttributes")
    public List<AttributeValueView> getExtraAttributes() {
        return attributes.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getIdentitySource()))
                .sorted(ordering())
                .collect(Collectors.toList());
    }

    // On peut empêcher la désérialisation de setters legacy pour éviter les
    // confusions.
    @JsonIgnore
    public void setPrimaryAttributes(List<AttributeValueView> ignored) {
    }

    @JsonIgnore
    public void setExtraAttributes(List<AttributeValueView> ignored) {
    }

    private static Comparator<AttributeValueView> ordering() {
        return Comparator
                .comparing(
                        (AttributeValueView v) -> v.getDisplayOrder() == null ? Integer.MAX_VALUE : v.getDisplayOrder())
                .thenComparing(v -> v.getAttributeId() == null ? Long.MAX_VALUE : v.getAttributeId())
                .thenComparing(v -> v.getValue() == null ? "" : v.getValue());
    }

    public static class Builder {
        private Long idPerson;
        private String photoStorageKey;
        private List<AttributeValueView> attributes = new ArrayList<>();
        private boolean followed;

        public Builder withIdPerson(Long v) {
            this.idPerson = v;
            return this;
        }

        public Builder withPhotoStorageKey(String v) {
            this.photoStorageKey = v;
            return this;
        }

        public Builder withAttributes(List<AttributeValueView> v) {
            this.attributes = v;
            return this;
        }

        public Builder addAttribute(AttributeValueView v) {
            this.attributes.add(v);
            return this;
        }

        public Builder withFollowed(boolean v) {
            this.followed = v;
            return this;
        }

        // Helpers de compat : si du code legacy appelle encore ces méthodes, on mappe
        // vers attributes
        public Builder withPrimaryAttributes(List<AttributeValueView> primaries) {
            if (primaries != null)
                primaries.forEach(p -> {
                    if (p.getIdentitySource() == null || !p.getIdentitySource())
                        p.setIdentitySource(true);
                });
            this.attributes.addAll(primaries == null ? List.of() : primaries);
            return this;
        }

        public Builder withExtraAttributes(List<AttributeValueView> extras) {
            if (extras != null)
                extras.forEach(e -> {
                    if (e.getIdentitySource() == null || e.getIdentitySource())
                        e.setIdentitySource(false);
                });
            this.attributes.addAll(extras == null ? List.of() : extras);
            return this;
        }

        public PersonCard build() {
            return new PersonCard(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PersonCard that))
            return false;
        return followed == that.followed
                && Objects.equals(idPerson, that.idPerson)
                && Objects.equals(photoStorageKey, that.photoStorageKey)
                && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPerson, photoStorageKey, attributes, followed);
    }

    @Override
    public String toString() {
        return "PersonCard{" +
                "idPerson=" + idPerson +
                ", photoStorageKey='" + photoStorageKey + '\'' +
                ", attributes=" + attributes +
                ", followed=" + followed +
                '}';
    }
}
