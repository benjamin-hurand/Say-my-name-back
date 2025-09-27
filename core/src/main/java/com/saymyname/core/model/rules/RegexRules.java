// src/main/java/com/saymyname/core/model/rules/RegexRules.java
package com.saymyname.core.model.rules;

public class RegexRules {
    public String pattern; // ex "^[A-DÀ-ÄÂ].*$"
    public Integer minLength; // optionnel
    public Integer maxLength; // optionnel
    public Boolean caseInsensitive; // optionnel
}
