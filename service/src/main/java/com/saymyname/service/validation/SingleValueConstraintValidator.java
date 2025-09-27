// src/main/java/com/saymyname/core/util/SingleValueConstraintValidator.java
package com.saymyname.service.validation;

import com.saymyname.core.model.people.Attribute;

public interface SingleValueConstraintValidator {
    /** Peut normaliser (trim, etc.). Retourne la valeur à persister. */
    String validateAndNormalize(Attribute attr, String rawValue);
}
