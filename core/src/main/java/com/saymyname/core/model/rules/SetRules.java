// src/main/java/com/saymyname/core/model/rules/SetRules.java
package com.saymyname.core.model.rules;

import java.util.List;

public class SetRules {
    public List<String> values; // autorisées
    public boolean strict = true; // si false => tolère hors-set (utile rare)
}
