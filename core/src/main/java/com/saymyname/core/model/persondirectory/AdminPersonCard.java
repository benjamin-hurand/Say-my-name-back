// src/main/java/com/saymyname/core/model/persondirectory/AdminPersonCard.java
package com.saymyname.core.model.persondirectory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class AdminPersonCard {

    private Long idPerson;
    private String photoStorageKey;
    @JsonProperty("attributes")
    private List<AttributeValueView> attributes = new ArrayList<>();

    /** Change requests en attente ? */
    private boolean hasPendingChangeRequests;

    public AdminPersonCard() {
    }

    private AdminPersonCard(Builder b) {
        this.idPerson = b.idPerson;
        this.photoStorageKey = b.photoStorageKey;
        this.attributes = b.attributes;
        this.hasPendingChangeRequests = b.hasPendingChangeRequests;
    }

    public Long getIdPerson() {
        return idPerson;
    }

    public String getPhotoStorageKey() {
        return photoStorageKey;
    }

    public List<AttributeValueView> getAttributes() {
        return attributes;
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

    public void setAttributes(List<AttributeValueView> attributes) {
        this.attributes = attributes;
    }

    public void setHasPendingChangeRequests(boolean v) {
        this.hasPendingChangeRequests = v;
    }

    // --------- Rétro-compat JSON (legacy fields) ---------
    @JsonProperty("primaryAttributes")
    public List<AttributeValueView> getPrimaryAttributes() {
        return attributes.stream()
                .filter(a -> Boolean.TRUE.equals(a.getPrimaryField()))
                .sorted(ordering())
                .collect(Collectors.toList());
    }

    @JsonProperty("extraAttributes")
    public List<AttributeValueView> getExtraAttributes() {
        return attributes.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getPrimaryField()))
                .sorted(ordering())
                .collect(Collectors.toList());
    }

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
        private boolean hasPendingChangeRequests;

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

        public Builder withHasPendingChangeRequests(boolean v) {
            this.hasPendingChangeRequests = v;
            return this;
        }

        // Helpers de compat
        public Builder withPrimaryAttributes(List<AttributeValueView> primaries) {
            if (primaries != null)
                primaries.forEach(p -> {
                    if (p.getPrimaryField() == null || !p.getPrimaryField())
                        p.setPrimaryField(true);
                });
            this.attributes.addAll(primaries == null ? List.of() : primaries);
            return this;
        }

        public Builder withExtraAttributes(List<AttributeValueView> extras) {
            if (extras != null)
                extras.forEach(e -> {
                    if (e.getPrimaryField() == null || e.getPrimaryField())
                        e.setPrimaryField(false);
                });
            this.attributes.addAll(extras == null ? List.of() : extras);
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
                && Objects.equals(attributes, that.attributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idPerson, photoStorageKey, attributes, hasPendingChangeRequests);
    }

    @Override
    public String toString() {
        return "AdminPersonCard{" +
                "idPerson=" + idPerson +
                ", photoStorageKey='" + photoStorageKey + '\'' +
                ", attributes=" + attributes +
                ", hasPendingChangeRequests=" + hasPendingChangeRequests +
                '}';
    }
}
