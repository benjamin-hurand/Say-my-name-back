package com.saymyname.core.model.enums;

public enum UserPerformance {
    PAS_COMMENCE("Pas commencé"),
    ACHEVE("Achevé"),
    PAS_PARFAIT("Pas parfait"),
    REUSSI("Réussi"),
    PODIUM("Podium");

    private final String label;

    UserPerformance(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
