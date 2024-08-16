package com.oxyl.webapp.mapper;

import com.oxyl.core.model.game.options.GameAttributeSort;
import com.oxyl.webapp.dto.GameAttributeSortDto;
import org.springframework.stereotype.Component;

@Component
public class GameAttributeSortDtoMapper {

    private final AttributeDtoMapper attributeDtoMapper;

    public GameAttributeSortDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public GameAttributeSort toModel(GameAttributeSortDto gameAttributeSortDto) {
        return new GameAttributeSort.Builder()
                .withId(gameAttributeSortDto.id())
                .withAttribute(attributeDtoMapper.toModel(gameAttributeSortDto.attribute()))
                .withOrder(gameAttributeSortDto.order())
                .build();
    }


    public GameAttributeSortDto toDto(GameAttributeSort gameAttributeSort) {
        return new GameAttributeSortDto(
                gameAttributeSort.getId(),
                attributeDtoMapper.toDto(gameAttributeSort.getAttribute()),
                gameAttributeSort.getOrder()
        );
    }

}
