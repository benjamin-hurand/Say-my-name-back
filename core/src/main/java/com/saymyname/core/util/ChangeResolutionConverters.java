// src/main/java/com/saymyname/core/util/ChangeResolutionConverters.java
package com.saymyname.core.util;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.enums.ChangeRequestItemStatus;
import com.saymyname.core.model.enums.ChangeResolutionDecision;

/** Conversions entre la décision (commande) et le statut persistant. */
@Component
public class ChangeResolutionConverters {

    public static ChangeRequestItemStatus toStatus(ChangeResolutionDecision d) {
        return switch (d) {
            case APPROVE -> ChangeRequestItemStatus.APPROVED;
            case REJECT -> ChangeRequestItemStatus.REJECTED;
        };
    }

    public static ChangeResolutionDecision fromStatus(ChangeRequestItemStatus s) {
        return switch (s) {
            case APPROVED -> ChangeResolutionDecision.APPROVE;
            case REJECTED -> ChangeResolutionDecision.REJECT;
            case CANCELED -> throw new IllegalArgumentException("CANCELED n'est pas une décision admin");
            case PENDING -> throw new IllegalArgumentException("PENDING n'est pas une décision admin");
        };
    }
}
