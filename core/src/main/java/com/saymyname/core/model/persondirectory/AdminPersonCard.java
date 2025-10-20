// src/main/java/com/saymyname/core/model/persondirectory/AdminPersonCard.java
package com.saymyname.core.model.persondirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AdminPersonCard {

    private Long idPerson;
    private String photoStorageKey;

    private List<AttributeValueView> primaryAttributes = new ArrayList<>();
    private List<AttributeValueView> extraAttributes = new ArrayList<>();

    /** Indique si des change requests sont en attente pour cette personne. */
    private boolean hasPendingChangeRequests;

    public AdminPersonCard() {
    }

    private AdminPersonCard(Builder b) {
        this.idPerson = b.idPerson;
        this.photoStorageKey = b.photoStorageKey;
        this.primaryAttributes = b.primaryAttributes;
        this.extraAttributes = b.extraAttributes;
        this.hasPendingChangeRequests = b.hasPendingChangeRequests; // important
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

    public List<AttributeValueView> getExtraAttributes() {
        return extraAttributes;
    }

    public boolean isHasPendingChangeRequests() {
        return hasPendingChangeRequests;
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

    public void setExtraAttributes(List<AttributeValueView> extraAttributes) {
        this.extraAttributes = extraAttributes;
    }

    public void setHasPendingChangeRequests(boolean v) {
        this.hasPendingChangeRequests = v;
    }

    public static class Builder {
        private Long idPerson;
        private String photoStorageKey;
        private List<AttributeValueView> primaryAttributes = new ArrayList<>();
        private List<AttributeValueView> extraAttributes = new ArrayList<>();
        private boolean hasPendingChangeRequests;

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

        public Builder withExtraAttributes(List<AttributeValueView> v) {
            this.extraAttributes = v;
            return this;
        }

        public Builder addExtraAttribute(AttributeValueView v) {
            this.extraAttributes.add(v);
            return this;
        }

        public Builder withHasPendingChangeRequests(boolean v) {
            this.hasPendingChangeRequests = v;
            return this;
        }

        public AdminPersonCard build() {
            return new AdminPersonCard(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof AdminPersonCard that))
            return false;
        return hasPendingChangeRequests == that.hasPendingChangeRequests
                && Objects.equals(idPerson, that.idPerson)
                && Objects.equals(photoStorageKey, that.photoStorageKey)
                && Objects.equals(primaryAttributes, that.primaryAttributes)
                && Objects.equals(extraAttributes, that.extraAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPerson, photoStorageKey, primaryAttributes, extraAttributes, hasPendingChangeRequests);
    }

    @Override
    public String toString() {
        return "AdminPersonCard{" +
                "idPerson=" + idPerson +
                ", photoStorageKey='" + photoStorageKey + '\'' +
                ", primaryAttributes=" + primaryAttributes +
                ", extraAttributes=" + extraAttributes +
                ", hasPendingChangeRequests=" + hasPendingChangeRequests +
                '}';
    }
}
