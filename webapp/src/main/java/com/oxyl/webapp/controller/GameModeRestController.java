package com.oxyl.webapp.controller;

import com.oxyl.service.GameModeService;
import com.oxyl.webapp.dto.GameModeDto;
import com.oxyl.webapp.mapper.GameModeDtoMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class GameModeRestController {

    private final GameModeService gameModeService;
    private final GameModeDtoMapper gameModeDtoMapper;

    public GameModeRestController(GameModeService gameModeService, GameModeDtoMapper gameModeDtoMapper) {
        this.gameModeService = gameModeService;
        this.gameModeDtoMapper = gameModeDtoMapper;
    }

    @GetMapping("/api/gamemodes")
    public ResponseEntity<List<GameModeDto>> getQuizGameModes() {
        List<GameModeDto> gameModes = gameModeService.getAllGameModes().stream().map(gameModeDtoMapper::toDto).toList();
        return ResponseEntity.ok(gameModes);
    }
}
