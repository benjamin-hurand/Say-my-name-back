package com.saymyname.core.model.persondirectory;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class AdminPersonSearchCriteria {
    @Builder.Default
    List<AttributeFilter> filters = List.of();
    @Builder.Default
    List<SortDirective> sort = List.of();
    boolean includeContextAttributes;

    @Value
    @Builder(toBuilder = true)
    public static class AttributeFilter {
        Long attributeId;
        String operator;
        @Builder.Default
        List<String> values = List.of();
    }

    @Value
    @Builder(toBuilder = true)
    public static class SortDirective {
        String kind;
        Long attributeId;
        String field;
        String direction;
    }
}
