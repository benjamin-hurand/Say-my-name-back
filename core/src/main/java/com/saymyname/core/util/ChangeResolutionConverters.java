// src/main/java/com/saymyname/core/util/ChangeResolutionConverters.java
package com.saymyname.core.util;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.ChangeItemResolutionStatus;
import com.saymyname.core.model.enums.ChangeResolutionDecision;

/** Conversions entre la décision (commande) et le statut persistant. */
@Component
public class ChangeResolutionConverters {

    public static ChangeItemResolutionStatus toStatus(ChangeResolutionDecision d) {
        return switch (d) {
            case APPROVE -> ChangeItemResolutionStatus.APPROVED;
            case REJECT -> ChangeItemResolutionStatus.REJECTED;
        };
    }

    public static ChangeResolutionDecision fromStatus(ChangeItemResolutionStatus s) {
        return switch (s) {
            case APPROVED -> ChangeResolutionDecision.APPROVE;
            case REJECTED -> ChangeResolutionDecision.REJECT;
            case CANCELED -> throw new IllegalArgumentException("CANCELED n'est pas une décision admin");
            case PENDING -> throw new IllegalArgumentException("PENDING n'est pas une décision admin");
        };
    }
}
