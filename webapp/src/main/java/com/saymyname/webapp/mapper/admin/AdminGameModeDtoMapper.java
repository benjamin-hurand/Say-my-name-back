package com.saymyname.webapp.mapper.admin;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.options.GameModeAttribute;
import com.saymyname.webapp.dto.gamemode.GameModeAdminDtos.*;

@Component
public class AdminGameModeDtoMapper {

        public GameMode toModel(CreateGameModeRequestDto dto) {
                GameMode gm = new GameMode();
                gm.setTitle(dto.title());
                gm.setDescription(dto.description());
                gm.setOperator(dto.operator());
                gm.setGameModeAttributes(toModelAttributes(dto.attributes(), gm));
                return gm;
        }

        public GameMode toModel(UpdateGameModeRequestDto dto, Long pathId) {
                if (dto.id() == null || !Objects.equals(dto.id(), pathId)) {
                        throw new IllegalArgumentException("Payload id must match path id.");
                }
                GameMode gm = new GameMode();
                gm.setId(pathId);
                gm.setTitle(dto.title());
                gm.setDescription(dto.description());
                gm.setOperator(dto.operator());
                gm.setGameModeAttributes(toModelAttributes(dto.attributes(), gm));
                return gm;
        }

        public List<GameModeAttribute> toModelAttributes(List<GameModeAttributeWriteDto> attrs, GameMode owner) {
                if (attrs == null)
                        return List.of();

                return attrs.stream()
                                .filter(a -> a != null && a.attributeId() != null)
                                .map(a -> GameModeAttribute.builder()
                                                .id(a.id())
                                                .gameMode(owner) // important si ton modèle s'en sert
                                                .attribute(Attribute.builder().id(a.attributeId()).build())
                                                .build())
                                .toList();
        }

        public GameModeResponseDto toResponse(GameMode model) {
                List<GameModeAttributeResponseDto> attrs = model.getGameModeAttributes() == null
                                ? List.of()
                                : model.getGameModeAttributes().stream()
                                                .map(a -> new GameModeAttributeResponseDto(
                                                                a.getId(),
                                                                a.getAttribute() != null ? a.getAttribute().getId()
                                                                                : null))
                                                .toList();

                return new GameModeResponseDto(
                                model.getId(),
                                model.getTitle(),
                                model.getDescription(),
                                model.getOperator(),
                                attrs);
        }
}
