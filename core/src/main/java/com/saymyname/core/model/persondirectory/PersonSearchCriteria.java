package com.saymyname.core.model.persondirectory;

import com.saymyname.core.model.enums.FollowFilter;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PersonSearchCriteria {
    @Builder.Default
    List<AttributeFilter> filters = List.of();
    @Builder.Default
    List<SortDirective> sort = List.of();
    FollowFilter followFilter;
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
