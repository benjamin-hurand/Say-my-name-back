package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.concept.ConceptPortabilityKind;
import com.saymyname.core.model.enums.concept.ConceptValueType;

public class Concept {

    private Long id;
    private String code;
    private ConceptValueType valueType;
    private boolean derived;
    private ConceptPortabilityKind portabilityKind;
    private boolean identityComponentEligible;

    public Concept() {
    }

    private Concept(Builder builder) {
        this.id = builder.id;
        this.code = builder.code;
        this.valueType = builder.valueType;
        this.derived = builder.derived;
        this.portabilityKind = builder.portabilityKind;
        this.identityComponentEligible = builder.identityComponentEligible;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public ConceptValueType getValueType() {
        return valueType;
    }

    public boolean isDerived() {
        return derived;
    }

    public ConceptPortabilityKind getPortabilityKind() {
        return portabilityKind;
    }

    public boolean isIdentityComponentEligible() {
        return identityComponentEligible;
    }

    public static class Builder {
        private Long id;
        private String code;
        private ConceptValueType valueType;
        private boolean derived;
        private ConceptPortabilityKind portabilityKind;
        private boolean identityComponentEligible;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withValueType(ConceptValueType valueType) {
            this.valueType = valueType;
            return this;
        }

        public Builder withDerived(boolean derived) {
            this.derived = derived;
            return this;
        }

        public Builder withPortabilityKind(ConceptPortabilityKind portabilityKind) {
            this.portabilityKind = portabilityKind;
            return this;
        }

        public Builder withIdentityComponentEligible(boolean identityComponentEligible) {
            this.identityComponentEligible = identityComponentEligible;
            return this;
        }

        public Concept build() {
            return new Concept(this);
        }
    }
}