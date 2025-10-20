// src/main/java/com/saymyname/core/model/persondirectory/PagePersonRow.java
package com.saymyname.core.model.persondirectory;

import java.util.Objects;

/** Données minimales pour la page (id + storageKey de la photo approuvée). */
public class PagePersonRow {
    private Long personId;
    private String photoStorageKey;

    public PagePersonRow() {
    }

    public PagePersonRow(Long personId, String photoStorageKey) {
        this.personId = personId;
        this.photoStorageKey = photoStorageKey;
    }

    public Long getPersonId() {
        return personId;
    }

    public void setPersonId(Long personId) {
        this.personId = personId;
    }

    public String getPhotoStorageKey() {
        return photoStorageKey;
    }

    public void setPhotoStorageKey(String photoStorageKey) {
        this.photoStorageKey = photoStorageKey;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PagePersonRow that))
            return false;
        return Objects.equals(personId, that.personId)
                && Objects.equals(photoStorageKey, that.photoStorageKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(personId, photoStorageKey);
    }

    @Override
    public String toString() {
        return "PagePersonRow{personId=" + personId + ", photoStorageKey='" + photoStorageKey + "'}";
    }
}
