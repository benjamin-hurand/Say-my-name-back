// src/main/java/com/saymyname/core/model/persondirectory/PersonCard.java
package com.saymyname.core.model.persondirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PersonCard {

    private Long idPerson;

    /** Clé de stockage canonique de la photo (original) */
    private String photoStorageKey;

    private List<PrimaryAttribute> primaryAttributes = new ArrayList<>();
    private boolean followed;

    /** Attributs "contexte" (filtres/tri/catégories) */
    private List<PersonAttributeExtra> extraAttributes = new ArrayList<>();

    public PersonCard() {
    }

    private PersonCard(Builder builder) {
        this.idPerson = builder.idPerson;
        this.photoStorageKey = builder.photoStorageKey;
        this.primaryAttributes = builder.primaryAttributes;
        this.followed = builder.followed;
        this.extraAttributes = builder.extraAttributes;
    }

    // Getters
    public Long getIdPerson() {
        return idPerson;
    }

    public String getPhotoStorageKey() {
        return photoStorageKey;
    }

    public List<PrimaryAttribute> getPrimaryAttributes() {
        return primaryAttributes;
    }

    public boolean isFollowed() {
        return followed;
    }

    public List<PersonAttributeExtra> getExtraAttributes() {
        return extraAttributes;
    }

    // Setters
    public void setIdPerson(Long idPerson) {
        this.idPerson = idPerson;
    }

    public void setPhotoStorageKey(String photoStorageKey) {
        this.photoStorageKey = photoStorageKey;
    }

    public void setPrimaryAttributes(List<PrimaryAttribute> primaryAttributes) {
        this.primaryAttributes = primaryAttributes;
    }

    public void setFollowed(boolean followed) {
        this.followed = followed;
    }

    public void setExtraAttributes(List<PersonAttributeExtra> extraAttributes) {
        this.extraAttributes = extraAttributes;
    }

    // ====== Inner: PrimaryAttribute (riche) ======
    public static class PrimaryAttribute {
        private Long personAttributeId;
        private Long attributeId;
        private String value;
        private Integer displayOrder;
        private boolean primary;

        public PrimaryAttribute() {
        }

        private PrimaryAttribute(Builder builder) {
            this.personAttributeId = builder.personAttributeId;
            this.attributeId = builder.attributeId;
            this.value = builder.value;
            this.displayOrder = builder.displayOrder;
            this.primary = builder.primary;
        }

        public Long getPersonAttributeId() {
            return personAttributeId;
        }

        public Long getAttributeId() {
            return attributeId;
        }

        public String getValue() {
            return value;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public boolean isPrimary() {
            return primary;
        }

        public void setPersonAttributeId(Long personAttributeId) {
            this.personAttributeId = personAttributeId;
        }

        public void setAttributeId(Long attributeId) {
            this.attributeId = attributeId;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }

        public void setPrimary(boolean primary) {
            this.primary = primary;
        }

        public static class Builder {
            private Long personAttributeId;
            private Long attributeId;
            private String value;
            private Integer displayOrder;
            private boolean primary = true;

            public Builder withPersonAttributeId(Long personAttributeId) {
                this.personAttributeId = personAttributeId;
                return this;
            }

            public Builder withAttributeId(Long attributeId) {
                this.attributeId = attributeId;
                return this;
            }

            public Builder withValue(String value) {
                this.value = value;
                return this;
            }

            public Builder withDisplayOrder(Integer displayOrder) {
                this.displayOrder = displayOrder;
                return this;
            }

            public Builder withPrimary(boolean primary) {
                this.primary = primary;
                return this;
            }

            public PrimaryAttribute build() {
                return new PrimaryAttribute(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PrimaryAttribute that))
                return false;
            return primary == that.primary
                    && Objects.equals(personAttributeId, that.personAttributeId)
                    && Objects.equals(attributeId, that.attributeId)
                    && Objects.equals(value, that.value)
                    && Objects.equals(displayOrder, that.displayOrder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(personAttributeId, attributeId, value, displayOrder, primary);
        }

        @Override
        public String toString() {
            return "PrimaryAttribute{personAttributeId=" + personAttributeId + ", attributeId=" + attributeId
                    + ", value='" + value + "', displayOrder=" + displayOrder + ", primary=" + primary + '}';
        }
    }

    /** Attribut léger pour l’UI */
    public static class PersonAttributeExtra {
        private Long attributeId;
        private String value;
        private Integer displayOrder;

        public PersonAttributeExtra() {
        }

        private PersonAttributeExtra(Builder builder) {
            this.attributeId = builder.attributeId;
            this.value = builder.value;
            this.displayOrder = builder.displayOrder;
        }

        public Long getAttributeId() {
            return attributeId;
        }

        public String getValue() {
            return value;
        }

        public Integer getDisplayOrder() {
            return displayOrder;
        }

        public void setAttributeId(Long attributeId) {
            this.attributeId = attributeId;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public void setDisplayOrder(Integer displayOrder) {
            this.displayOrder = displayOrder;
        }

        public static class Builder {
            private Long attributeId;
            private String value;
            private Integer displayOrder;

            public Builder withAttributeId(Long attributeId) {
                this.attributeId = attributeId;
                return this;
            }

            public Builder withValue(String value) {
                this.value = value;
                return this;
            }

            public Builder withDisplayOrder(Integer displayOrder) {
                this.displayOrder = displayOrder;
                return this;
            }

            public PersonAttributeExtra build() {
                return new PersonAttributeExtra(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof PersonAttributeExtra that))
                return false;
            return Objects.equals(attributeId, that.attributeId)
                    && Objects.equals(value, that.value)
                    && Objects.equals(displayOrder, that.displayOrder);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attributeId, value, displayOrder);
        }

        @Override
        public String toString() {
            return "PersonAttributeExtra{attributeId=" + attributeId + ", value='" + value + "', displayOrder="
                    + displayOrder + '}';
        }
    }

    // ====== Builder principal ======
    public static class Builder {
        private Long idPerson;
        private String photoStorageKey;
        private List<PrimaryAttribute> primaryAttributes = new ArrayList<>();
        private boolean followed;
        private List<PersonAttributeExtra> extraAttributes = new ArrayList<>();

        public Builder withIdPerson(Long idPerson) {
            this.idPerson = idPerson;
            return this;
        }

        public Builder withPhotoStorageKey(String photoStorageKey) {
            this.photoStorageKey = photoStorageKey;
            return this;
        }

        public Builder withPrimaryAttributes(List<PrimaryAttribute> primaryAttributes) {
            this.primaryAttributes = primaryAttributes;
            return this;
        }

        public Builder addPrimaryAttribute(PrimaryAttribute attr) {
            this.primaryAttributes.add(attr);
            return this;
        }

        public Builder withFollowed(boolean followed) {
            this.followed = followed;
            return this;
        }

        public Builder withExtraAttributes(List<PersonAttributeExtra> extraAttributes) {
            this.extraAttributes = extraAttributes;
            return this;
        }

        public Builder addExtraAttribute(PersonAttributeExtra extra) {
            this.extraAttributes.add(extra);
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
