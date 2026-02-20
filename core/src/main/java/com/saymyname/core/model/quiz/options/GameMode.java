package com.saymyname.core.model.quiz.options;

import java.util.List;

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
public class GameMode {
    private Long id;
    private String title;
    private String description;
    private String operator;
    @Builder.Default
    private List<GameModeAttribute> gameModeAttributes = List.of();
    @Builder.Default
    private List<Attribute> attributes = List.of();
}
