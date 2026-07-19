package com.saymyname.core.model.people;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.concept.ConceptPortabilityKind;
import com.saymyname.core.model.enums.concept.ConceptTenantUsagePolicy;

public class Concept {

    private Long id;
    private String code;
    private String iconKey;
    private ValueType valueType;
    private boolean derived;
    private ConceptPortabilityKind portabilityKind;
    private boolean identityComponentEligible;
    private ConceptTenantUsagePolicy tenantUsagePolicy;
    private CasingStrategy defaultCasingStrategy;

    public Concept() {
    }

    private Concept(Builder builder) {
        this.id = builder.id;
        this.code = builder.code;
        this.iconKey = builder.iconKey;
        this.valueType = builder.valueType;
        this.derived = builder.derived;
        this.portabilityKind = builder.portabilityKind;
        this.identityComponentEligible = builder.identityComponentEligible;
        this.tenantUsagePolicy = builder.tenantUsagePolicy;
        this.defaultCasingStrategy = builder.defaultCasingStrategy;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getIconKey() {
        return iconKey;
    }

    public ValueType getValueType() {
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

    public ConceptTenantUsagePolicy getTenantUsagePolicy() {
        return tenantUsagePolicy;
    }

    public CasingStrategy getDefaultCasingStrategy() {
        return defaultCasingStrategy;
    }

    public static class Builder {
        private Long id;
        private String code;
        private String iconKey;
        private ValueType valueType;
        private boolean derived;
        private ConceptPortabilityKind portabilityKind;
        private boolean identityComponentEligible;
        private ConceptTenantUsagePolicy tenantUsagePolicy;
        private CasingStrategy defaultCasingStrategy;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withCode(String code) {
            this.code = code;
            return this;
        }

        public Builder withIconKey(String iconKey) {
            this.iconKey = iconKey;
            return this;
        }

        public Builder withValueType(ValueType valueType) {
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

        public Builder withTenantUsagePolicy(ConceptTenantUsagePolicy tenantUsagePolicy) {
            this.tenantUsagePolicy = tenantUsagePolicy;
            return this;
        }

        public Builder withDefaultCasingStrategy(CasingStrategy defaultCasingStrategy) {
            this.defaultCasingStrategy = defaultCasingStrategy;
            return this;
        }

        public Concept build() {
            return new Concept(this);
        }
    }
}