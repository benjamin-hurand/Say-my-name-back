package com.saymyname.core.model.quiz.options;

import com.saymyname.core.model.people.Attribute;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class GameModeAttribute {
    private Long id;
    private GameMode gameMode;
    private Attribute attribute;

    public Long getAttributeId() {
        return attribute != null ? attribute.getId() : null;
    }
}
