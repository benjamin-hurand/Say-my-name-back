// src/main/java/com/saymyname/core/model/rules/RangeRules.java
package com.saymyname.core.model.rules;

public class RangeRules {
    // NUMBER: min/max numériques ; DATE/DATETIME: ISO-8601
    public String min; // ex "1970" ou "2000-01-01"
    public String max; // ex "2050" ou "2030-12-31"
    public boolean inclusive = true;
    public Integer step; // optionnel (NUMBER)
}
