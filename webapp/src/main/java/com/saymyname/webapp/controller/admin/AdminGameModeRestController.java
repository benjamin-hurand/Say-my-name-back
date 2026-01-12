package com.saymyname.webapp.controller.admin;

import java.util.List;

import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.saymyname.service.GameModeService;
import com.saymyname.webapp.dto.gamemode.GameModeAdminDtos.*;
import com.saymyname.webapp.mapper.admin.AdminGameModeDtoMapper;

@PreAuthorize("@orgSecurity.hasRole(null, 'ADMIN') or @orgSecurity.hasRole(null, 'OWNER')")
@RestController
@RequestMapping("/api/admin/gamemodes")
public class AdminGameModeRestController {

    private final GameModeService gameModeService;
    private final AdminGameModeDtoMapper mapper;

    public AdminGameModeRestController(GameModeService gameModeService, AdminGameModeDtoMapper mapper) {
        this.gameModeService = gameModeService;
        this.mapper = mapper;
    }

    /** GET /api/admin/gamemodes */
    @GetMapping
    public List<GameModeResponseDto> list() {
        return gameModeService.getAllGameModes().stream()
                .map(mapper::toResponse)
                .toList();
    }

    /** POST /api/admin/gamemodes */
    @PostMapping
    public ResponseEntity<GameModeResponseDto> create(@RequestBody CreateGameModeRequestDto body) {
        var created = gameModeService.create(mapper.toModel(body));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(created));
    }

    /** PUT /api/admin/gamemodes/{id} */
    @PutMapping("/{id}")
    public GameModeResponseDto update(@PathVariable("id") Long id, @RequestBody UpdateGameModeRequestDto body) {
        var updated = gameModeService.update(mapper.toModel(body, id));
        return mapper.toResponse(updated);
    }

    /** DELETE /api/admin/gamemodes/{id} */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        gameModeService.delete(id);
    }

    /** PATCH /api/admin/gamemodes/{id}/attributes — remplace la liste entière */
    @PatchMapping("/{id}/attributes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replaceAttributes(
            @PathVariable("id") Long id,
            @RequestBody List<GameModeAttributeWriteDto> attributes) {
        gameModeService.replaceAttributes(id, mapper.toModelAttributes(attributes, null));
    }

    // --- mapping erreurs minimal ---
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(java.util.NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(java.util.NoSuchElementException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }
}
