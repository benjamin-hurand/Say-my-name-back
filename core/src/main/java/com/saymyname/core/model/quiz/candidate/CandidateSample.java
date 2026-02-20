package com.saymyname.core.model.quiz.candidate;

import java.util.List;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class CandidateSample {
    @Builder.Default
    List<PayloadItem> items = List.of();
    Long targetPersonId;
    String targetStorageKey;

    public static CandidateSample ofSingle(PayloadItem target) {
        return CandidateSample.builder()
                .items(List.of(target))
                .targetPersonId(target.getPersonId())
                .targetStorageKey(target.getPhotoStorageKey())
                .build();
    }

    public static CandidateSample of(List<PayloadItem> items, Long targetPersonId) {
        String targetStorageKey = items.stream()
                .filter(i -> targetPersonId.equals(i.getPersonId()))
                .findFirst()
                .map(PayloadItem::getPhotoStorageKey)
                .orElse(null);

        return CandidateSample.builder()
                .items(items == null ? List.of() : items)
                .targetPersonId(targetPersonId)
                .targetStorageKey(targetStorageKey)
                .build();
    }

    public List<Long> personIds() {
        return items.stream().map(PayloadItem::getPersonId).toList();
    }

    public List<PayloadItem> distractors() {
        if (targetPersonId == null) {
            return items;
        }
        return items.stream().filter(i -> !targetPersonId.equals(i.getPersonId())).toList();
    }

    public PayloadItem target() {
        if (targetPersonId == null) {
            return items.isEmpty() ? null : items.get(0);
        }
        return items.stream().filter(i -> targetPersonId.equals(i.getPersonId())).findFirst().orElse(null);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Backward-compatible record-style accessors.
    public List<PayloadItem> items() {
        return items;
    }

    public Long targetPersonId() {
        return targetPersonId;
    }

    public String targetStorageKey() {
        return targetStorageKey;
    }
}
