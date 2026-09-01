package com.saymyname.core.model.people;

import java.util.Set;

public final class ConceptCodes {

    public static final String FIRST_NAME = "FIRST_NAME";
    public static final String LAST_NAME = "LAST_NAME";
    public static final String GENDER = "GENDER";
    public static final String IDENTITY = "IDENTITY";

    public static final Set<String> ACTIVE = Set.of(FIRST_NAME, LAST_NAME, GENDER, IDENTITY);

    private ConceptCodes() {
    }
}
