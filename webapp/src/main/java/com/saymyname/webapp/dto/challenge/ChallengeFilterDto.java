package com.saymyname.webapp.dto.challenge;

import com.saymyname.core.model.people.AttributeType;

public record ChallengeFilterDto(
                Long attributeId, // getFilterAttributeId()
                String attributeName, // getAttributeName()
                AttributeType filterType, // getFilterType()
                String minValue, // getMinFilterValue()
                String maxValue // getMaxFilterValue()
) {
}
