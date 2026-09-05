package com.saymyname.core.model.people;

/**
 * Single source of truth for whether an attribute can be used as a filter or
 * a sort key. These capabilities are derived purely from {@link ValueType}:
 * the admin no longer configures them per attribute (see product decision on
 * the "Champs" admin page simplification). Every caller that needs to know
 * whether an attribute is filterable/sortable — endpoints, search guards,
 * persistence — must go through this class instead of reading a stored
 * {@code filter}/{@code sort} column or re-implementing the type switch.
 */
public final class AttributeCapabilities {

    private AttributeCapabilities() {
    }

    public static boolean isFilterable(ValueType type) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case TEXT -> false;
            case ENUM, BOOLEAN, NUMBER, DATE, DATETIME -> true;
        };
    }

    public static boolean isSortable(ValueType type) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case TEXT -> false;
            case ENUM, BOOLEAN, NUMBER, DATE, DATETIME -> true;
        };
    }

    public static boolean isFilterable(Attribute attribute) {
        return attribute != null && isFilterable(attribute.getType());
    }

    public static boolean isSortable(Attribute attribute) {
        return attribute != null && isSortable(attribute.getType());
    }
}
