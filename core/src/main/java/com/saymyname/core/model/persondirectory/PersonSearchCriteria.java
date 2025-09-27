// src/main/java/com/saymyname/core/model/persondirectory/PersonSearchCriteria.java
package com.saymyname.core.model.persondirectory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saymyname.core.model.enums.FollowFilter;

public class PersonSearchCriteria {

    private List<AttributeFilter> filters = new ArrayList<>();
    private List<SortDirective> sort = new ArrayList<>();
    private FollowFilter followFilter;
    /**
     * NOUVEAU : inclure les attributs “contexte” (filtres, tris attributaires,
     * catégories) dans la réponse
     */
    private boolean includeContextAttributes;

    public PersonSearchCriteria() {
    }

    private PersonSearchCriteria(Builder builder) {
        this.filters = builder.filters;
        this.sort = builder.sort;
        this.followFilter = builder.followFilter;
        this.includeContextAttributes = builder.includeContextAttributes;
    }

    // Getters
    public List<AttributeFilter> getFilters() {
        return filters;
    }

    public List<SortDirective> getSort() {
        return sort;
    }

    public FollowFilter getFollowFilter() {
        return followFilter;
    }

    public boolean isIncludeContextAttributes() {
        return includeContextAttributes;
    }

    // Setters
    public void setFilters(List<AttributeFilter> filters) {
        this.filters = filters;
    }

    public void setSort(List<SortDirective> sort) {
        this.sort = sort;
    }

    public void setFollowFilter(FollowFilter followFilter) {
        this.followFilter = followFilter;
    }

    public void setIncludeContextAttributes(boolean includeContextAttributes) {
        this.includeContextAttributes = includeContextAttributes;
    }

    // ====== Inner: AttributeFilter ======
    public static class AttributeFilter {
        private Long attributeId;
        /** "IN" | "LIKE" | "RANGE" (libre, validé au mapper/service) */
        private String operator;
        /** Pour IN/LIKE: valeurs; pour RANGE: [min, max] (date/num) */
        private List<String> values = new ArrayList<>();

        public AttributeFilter() {
        }

        private AttributeFilter(AttributeFilter.Builder builder) {
            this.attributeId = builder.attributeId;
            this.operator = builder.operator;
            this.values = builder.values;
        }

        // Getters
        public Long getAttributeId() {
            return attributeId;
        }

        public String getOperator() {
            return operator;
        }

        public List<String> getValues() {
            return values;
        }

        // Setters
        public void setAttributeId(Long attributeId) {
            this.attributeId = attributeId;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }

        public void setValues(List<String> values) {
            this.values = values;
        }

        // Builder
        public static class Builder {
            private Long attributeId;
            private String operator;
            private List<String> values = new ArrayList<>();

            public Builder withAttributeId(Long attributeId) {
                this.attributeId = attributeId;
                return this;
            }

            public Builder withOperator(String operator) {
                this.operator = operator;
                return this;
            }

            public Builder withValues(List<String> values) {
                this.values = values;
                return this;
            }

            public AttributeFilter build() {
                return new AttributeFilter(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof AttributeFilter that))
                return false;
            return Objects.equals(attributeId, that.attributeId)
                    && Objects.equals(operator, that.operator)
                    && Objects.equals(values, that.values);
        }

        @Override
        public int hashCode() {
            return Objects.hash(attributeId, operator, values);
        }

        @Override
        public String toString() {
            return "AttributeFilter{" +
                    "attributeId=" + attributeId +
                    ", operator='" + operator + '\'' +
                    ", values=" + values +
                    '}';
        }
    }

    // ====== Inner: SortDirective ======
    public static class SortDirective {
        /** "ATTRIBUTE" | "FIELD" */
        private String kind;
        /** requis si kind=ATTRIBUTE */
        private Long attributeId;
        /** ex: "id" (ou "createdAt" si dispo) */
        private String field;
        /** "ASC" | "DESC" */
        private String direction;

        public SortDirective() {
        }

        private SortDirective(SortDirective.Builder builder) {
            this.kind = builder.kind;
            this.attributeId = builder.attributeId;
            this.field = builder.field;
            this.direction = builder.direction;
        }

        // Getters
        public String getKind() {
            return kind;
        }

        public Long getAttributeId() {
            return attributeId;
        }

        public String getField() {
            return field;
        }

        public String getDirection() {
            return direction;
        }

        // Setters
        public void setKind(String kind) {
            this.kind = kind;
        }

        public void setAttributeId(Long attributeId) {
            this.attributeId = attributeId;
        }

        public void setField(String field) {
            this.field = field;
        }

        public void setDirection(String direction) {
            this.direction = direction;
        }

        // Builder
        public static class Builder {
            private String kind;
            private Long attributeId;
            private String field;
            private String direction;

            public Builder withKind(String kind) {
                this.kind = kind;
                return this;
            }

            public Builder withAttributeId(Long attributeId) {
                this.attributeId = attributeId;
                return this;
            }

            public Builder withField(String field) {
                this.field = field;
                return this;
            }

            public Builder withDirection(String direction) {
                this.direction = direction;
                return this;
            }

            public SortDirective build() {
                return new SortDirective(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof SortDirective that))
                return false;
            return Objects.equals(kind, that.kind)
                    && Objects.equals(attributeId, that.attributeId)
                    && Objects.equals(field, that.field)
                    && Objects.equals(direction, that.direction);
        }

        @Override
        public int hashCode() {
            return Objects.hash(kind, attributeId, field, direction);
        }

        @Override
        public String toString() {
            return "SortDirective{" +
                    "kind='" + kind + '\'' +
                    ", attributeId=" + attributeId +
                    ", field='" + field + '\'' +
                    ", direction='" + direction + '\'' +
                    '}';
        }
    }

    // ====== Builder principal ======
    public static class Builder {
        private List<AttributeFilter> filters = new ArrayList<>();
        private List<SortDirective> sort = new ArrayList<>();
        private FollowFilter followFilter;
        private boolean includeContextAttributes;

        public Builder withFilters(List<AttributeFilter> filters) {
            this.filters = filters;
            return this;
        }

        public Builder withSort(List<SortDirective> sort) {
            this.sort = sort;
            return this;
        }

        public Builder withFollowFilter(FollowFilter followFilter) {
            this.followFilter = followFilter;
            return this;
        }

        public Builder withIncludeContextAttributes(boolean includeContextAttributes) {
            this.includeContextAttributes = includeContextAttributes;
            return this;
        }

        public PersonSearchCriteria build() {
            return new PersonSearchCriteria(this);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof PersonSearchCriteria that))
            return false;
        return followFilter == that.followFilter
                && includeContextAttributes == that.includeContextAttributes
                && Objects.equals(filters, that.filters)
                && Objects.equals(sort, that.sort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filters, sort, followFilter, includeContextAttributes);
    }

    @Override
    public String toString() {
        return "PersonSearchCriteria{" +
                "filters=" + filters +
                ", sort=" + sort +
                ", followFilter=" + followFilter +
                ", includeContextAttributes=" + includeContextAttributes +
                '}';
    }
}
