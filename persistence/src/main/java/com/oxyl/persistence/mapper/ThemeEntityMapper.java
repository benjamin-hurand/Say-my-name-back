package com.oxyl.persistence.mapper;

import com.oxyl.core.model.Theme;
import com.oxyl.persistence.entity.ThemeEntity;
import org.springframework.stereotype.Component;

@Component
public class ThemeEntityMapper {

    private AttributeEntityMapper attributeEntityMapper;
    private ThemeAttributeEntityMapper themeAttributeEntityMapper;

    public ThemeEntityMapper(AttributeEntityMapper attributeEntityMapper, ThemeAttributeEntityMapper themeAttributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
        this.themeAttributeEntityMapper = themeAttributeEntityMapper;
    }

    public ThemeEntity toEntity(Theme theme) {
        return new ThemeEntity(theme.getId(), theme.getTitle(), theme.getDescription(), theme.getThemeAttributes().stream().map(themeAttributeEntityMapper::toEntity).toList());
    }

    public Theme toModel(ThemeEntity themeEntity) {
        return new Theme.Builder()
                .withId(themeEntity.getId())
                .withTitle(themeEntity.getThemeTitle())
                .withDescription(themeEntity.getThemeDescription())
                .withThemeAttributes(themeEntity.getThemeAttributes().stream().map(themeAttributeEntityMapper::toModel).toList())
                .build();
    }
}
