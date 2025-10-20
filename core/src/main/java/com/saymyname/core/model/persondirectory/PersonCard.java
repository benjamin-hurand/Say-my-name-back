// src/main/java/com/saymyname/core/model/persondirectory/PersonCard.java
package com.saymyname.core.model.persondirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonCard {

    private Long idPerson;
    private String photoStorageKey;

    /** Champs primaires (nom, prénom, etc.) */
    private List<AttributeValueView> primaryAttributes = new ArrayList<>();

    /** Suivi (côté utilisateur non admin) */
    private boolean followed;

    /** Attributs “contexte” (filtres/tri/catégories) */
    private List<AttributeValueView> extraAttributes = new ArrayList<>();

    public PersonCard() {
    }

    private PersonCard(Builder b) {
        this.idPerson = b.idPerson;
        this.photoStorageKey = b.photoStorageKey;
        this.primaryAttributes = b.primaryAttributes;
        this.followed = b.followed;
        this.extraAttributes = b.extraAttributes;
    }

    public Long getIdPerson() {
        return idPerson;
    }

    public String getPhotoStorageKey() {
        return photoStorageKey;
    }

    public List<AttributeValueView> getPrimaryAttributes() {
        return primaryAttributes;
    }

    public boolean isFollowed() {
        return followed;
    }

    public List<AttributeValueView> getExtraAttributes() {
        return extraAttributes;
    }

    public void setIdPerson(Long idPerson) {
        this.idPerson = idPerson;
    }

    public void setPhotoStorageKey(String photoStorageKey) {
        this.photoStorageKey = photoStorageKey;
    }

    public void setPrimaryAttributes(List<AttributeValueView> primaryAttributes) {
        this.primaryAttributes = primaryAttributes;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

    public void setExtraAttributes(List<AttributeValueView> extraAttributes) {
        this.extraAttributes = extraAttributes;
    }

    public static class Builder {
        private Long idPerson;
        private String photoStorageKey;
        private List<AttributeValueView> primaryAttributes = new ArrayList<>();
        private boolean followed;
        private List<AttributeValueView> extraAttributes = new ArrayList<>();

        public Builder withIdPerson(Long v) {
            this.idPerson = v;
            return this;
        }

        public Builder withPhotoStorageKey(String v) {
            this.photoStorageKey = v;
            return this;
        }

        public Builder withPrimaryAttributes(List<AttributeValueView> v) {
            this.primaryAttributes = v;
            return this;
        }

        public Builder addPrimaryAttribute(AttributeValueView v) {
            this.primaryAttributes.add(v);
            return this;
        }

        public Builder withFollowed(boolean v) {
            this.followed = v;
            return this;
        }

        public Builder withExtraAttributes(List<AttributeValueView> v) {
            this.extraAttributes = v;
            return this;
        }

        public Builder addExtraAttribute(AttributeValueView v) {
            this.extraAttributes.add(v);
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
                && Objects.equals(primaryAttributes, that.primaryAttributes)
                && Objects.equals(extraAttributes, that.extraAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPerson, photoStorageKey, primaryAttributes, followed, extraAttributes);
    }

    @Override
    public String toString() {
        return "PersonCard{" +
                "idPerson=" + idPerson +
                ", photoStorageKey='" + photoStorageKey + '\'' +
                ", primaryAttributes=" + primaryAttributes +
                ", followed=" + followed +
                ", extraAttributes=" + extraAttributes +
                '}';
    }
}
