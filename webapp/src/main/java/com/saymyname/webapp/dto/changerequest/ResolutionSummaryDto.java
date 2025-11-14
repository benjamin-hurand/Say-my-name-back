package com.saymyname.webapp.dto.changerequest;

public record ResolutionSummaryDto(
        int total,
        int approvedItems,
        int rejectedItems) {

}
