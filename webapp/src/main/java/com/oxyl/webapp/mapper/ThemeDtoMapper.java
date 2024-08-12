package com.oxyl.webapp.mapper;

import com.oxyl.core.model.Theme;
import com.oxyl.webapp.dto.ThemeDto;
import org.springframework.stereotype.Component;

@Component
public class ThemeDtoMapper {

    private final ThemeAttributeDtoMapper themeAttributeDtoMapper;

    public ThemeDtoMapper(ThemeAttributeDtoMapper themeAttributeDtoMapper) {
        this.themeAttributeDtoMapper = themeAttributeDtoMapper;
    }

    public ThemeDto toDto(Theme theme) {
        return new ThemeDto(theme.getId(), theme.getTitle(), theme.getDescription(), theme.getThemeAttributes().stream().map(themeAttributeDtoMapper::toDto).toList());
    }
}
