package com.saymyname.core.model.people;

import java.util.List;
import java.util.Optional;

import com.saymyname.core.model.enums.PhotoStatus;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Person {
    Long id;
    @Builder.Default
    List<Fact> facts = List.of();
    @Builder.Default
    List<Photo> photos = List.of();

    /**
     * Retourne la photo APPROVED de la personne s'il y en a une.
     */
    public Optional<Photo> getApprovedPhoto() {
        if (photos == null) {
            return Optional.empty();
        }
        return photos.stream()
                .filter(p -> p.getStatus() == PhotoStatus.APPROVED)
                .findFirst();
    }
}
