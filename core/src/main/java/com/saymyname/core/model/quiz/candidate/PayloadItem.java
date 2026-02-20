package com.saymyname.core.model.quiz.candidate;

import java.util.Map;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class PayloadItem {
    Long personId;
    String photoStorageKey;
    @Builder.Default
    Map<Long, String> attributeValues = Map.of();

    public String attributeValue(Long attributeId) {
        return attributeValues.get(attributeId);
    }

    public boolean hasPhoto() {
        return photoStorageKey != null && !photoStorageKey.isBlank();
    }
}
