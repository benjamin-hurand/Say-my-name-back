package com.oxyl.webapp.controller;

import com.oxyl.service.ThemeService;
import com.oxyl.webapp.dto.ThemeDto;
import com.oxyl.webapp.mapper.ThemeDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ThemeRestController {

    private final ThemeService themeService;
    private final ThemeDtoMapper themeDtoMapper;

    public ThemeRestController(ThemeService themeService, ThemeDtoMapper themeDtoMapper) {
        this.themeService = themeService;
        this.themeDtoMapper = themeDtoMapper;
    }

    @GetMapping("/api/quiz/themes")
    public ResponseEntity<List<ThemeDto>> getQuizThemes() {
        List<ThemeDto> themes = themeService.getAllThemes().stream().map(themeDtoMapper::toDto).toList();
        return ResponseEntity.ok(themes);
    }
}
