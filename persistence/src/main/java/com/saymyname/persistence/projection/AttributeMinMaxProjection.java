package com.saymyname.persistence.projection;

import java.text.SimpleDateFormat;
import java.util.Date;

public interface AttributeMinMaxProjection {
    Long getId();
    String getAttributeName();
    Boolean getUnique();
    Boolean getFilter();
    Boolean getSort();
    Boolean getInitializable();
    String getType();

    // Raw values coming from the query
    // For numbers, we expect integer values; for dates, a Date object.
    Integer getMinNumberValue();
    Integer getMaxNumberValue();
    Date getMinDateValue();
    Date getMaxDateValue();

    // Default methods to merge the values into one minValue / maxValue based on attribute type.
    default String getMinValue() {
        if ("number".equalsIgnoreCase(getType())) {
            return getMinNumberValue() != null ? String.valueOf(getMinNumberValue()) : null;
        } else if ("date".equalsIgnoreCase(getType())) {
            if (getMinDateValue() != null) {
                return new SimpleDateFormat("yyyy-MM-dd").format(getMinDateValue());
            }
        }
        return null;
    }

    default String getMaxValue() {
        if ("number".equalsIgnoreCase(getType())) {
            return getMaxNumberValue() != null ? String.valueOf(getMaxNumberValue()) : null;
        } else if ("date".equalsIgnoreCase(getType())) {
            if (getMaxDateValue() != null) {
                return new SimpleDateFormat("yyyy-MM-dd").format(getMaxDateValue());
            }
        }
        return null;
    }
}
