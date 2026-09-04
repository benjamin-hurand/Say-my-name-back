package com.saymyname.core.validation;

import com.saymyname.core.exception.common.ValidationException;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Concept;
import com.saymyname.core.model.people.ConceptCodes;
import com.saymyname.core.model.people.ValueType;

public final class AttributeDefinitionValidator {

    private AttributeDefinitionValidator() {
        // util class
    }

    public static void validate(Attribute attribute, Concept concept) {
        if (attribute == null) {
            throw new ValidationException("L'attribut est requis");
        }

        if (attribute.getMaxValues() < 1) {
            throw new ValidationException("maxValues doit être supérieur ou égal à 1");
        }

        if (attribute.isIdentitySource()) {
            validateIdentitySource(concept);
        }

        if (concept == null) {
            return;
        }

        if (attribute.getType() != concept.getValueType()) {
            throw new ValidationException("Le type de l'attribut doit correspondre au type du concept");
        }

        Integer requiredMaxValues = concept.getRequiredMaxValues();
        if (requiredMaxValues != null && !requiredMaxValues.equals(attribute.getMaxValues())) {
            throw new ValidationException(
                    "maxValues doit être égal à requiredMaxValues pour ce concept");
        }

        if (ConceptCodes.IDENTITY.equals(concept.getCode())) {
            validateIdentityAttribute(attribute);
        }
    }

    /**
     * MVP: seuls les concepts éligibles (FIRST_NAME, LAST_NAME) peuvent composer IDENTITY.
     * Un attribut custom (sans concept) ne peut plus être une source d'identité.
     */
    private static void validateIdentitySource(Concept concept) {
        if (concept == null || !concept.isIdentityComponentEligible()) {
            throw new ValidationException("Ce concept ne peut pas servir de source d'identité");
        }
    }

    private static void validateIdentityAttribute(Attribute attribute) {
        if (attribute.isIdentitySource()
                || attribute.getEditPolicy() != EditPolicy.DERIVED
                || attribute.getMaxValues() != 1
                || attribute.getType() != ValueType.TEXT) {
            throw new ValidationException(
                    "L'attribut IDENTITY doit être TEXT, DERIVED, maxValues=1 et ne pas être une source d'identité");
        }
    }
}
