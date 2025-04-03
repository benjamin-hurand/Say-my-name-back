package com.saymyname.core.model.enums;

public enum ChallengeSortCriterionType {
    CREATION_DATE("Date de création"),
    POPULARITY("Popularité"),
    LENGTH("Longueur"),
    PERFORMANCE("Performance");

    private final String label;

    ChallengeSortCriterionType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
