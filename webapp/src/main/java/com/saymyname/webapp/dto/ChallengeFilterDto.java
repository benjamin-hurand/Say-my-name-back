package com.saymyname.webapp.dto;

public record ChallengeFilterDto(
    Long attributeId,   // getFilterAttributeId()
    String attributeName, // getAttributeName()
    String filterType,    // getFilterType()
    String minValue,      // getMinFilterValue()
    String maxValue       // getMaxFilterValue()
) { }
