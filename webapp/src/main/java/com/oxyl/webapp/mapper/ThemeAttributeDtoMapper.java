package com.oxyl.webapp.mapper;

import com.oxyl.core.model.ThemeAttribute;
import com.oxyl.webapp.dto.ThemeAttributeDto;
import org.springframework.stereotype.Component;

@Component
public class ThemeAttributeDtoMapper {

    private AttributeDtoMapper attributeDtoMapper;

    public ThemeAttributeDtoMapper(AttributeDtoMapper attributeDtoMapper) {
        this.attributeDtoMapper = attributeDtoMapper;
    }

    public ThemeAttributeDto toDto(ThemeAttribute themeAttribute) {
        return new ThemeAttributeDto(themeAttribute.getId(), themeAttribute.getOperator(), attributeDtoMapper.toDto(themeAttribute.getAttribute()));
    }

    public ThemeAttribute toModel(ThemeAttributeDto themeAttributeDto) {
        return new ThemeAttribute.Builder()
                .withId(themeAttributeDto.id())
                .withOperator(themeAttributeDto.operator())
                .withAttribute(attributeDtoMapper.toModel(themeAttributeDto.attribute()))
                .build();
    }

}
