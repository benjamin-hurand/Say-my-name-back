package com.saymyname.core.model.persondirectory;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Comparator;
import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PersonCard {
    Long idPerson;
    String photoStorageKey;
    @JsonProperty("attributes")
    @Builder.Default
    List<AttributeValueView> attributes = List.of();
    boolean followed;

    @JsonProperty("primaryAttributes")
    public List<AttributeValueView> getPrimaryAttributes() {
        return attributes.stream()
                .filter(a -> Boolean.TRUE.equals(a.getPrimaryField()))
                .sorted(ordering())
                .toList();
    }

    @JsonProperty("extraAttributes")
    public List<AttributeValueView> getExtraAttributes() {
        return attributes.stream()
                .filter(a -> !Boolean.TRUE.equals(a.getPrimaryField()))
                .sorted(ordering())
                .toList();
    }

    private static Comparator<AttributeValueView> ordering() {
        return Comparator
                .comparing((AttributeValueView v) -> v.getDisplayOrder() == null ? Integer.MAX_VALUE : v.getDisplayOrder())
                .thenComparing(v -> v.getAttributeId() == null ? Long.MAX_VALUE : v.getAttributeId())
                .thenComparing(v -> v.getValue() == null ? "" : v.getValue());
    }
}
