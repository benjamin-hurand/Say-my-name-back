// src/main/java/com/saymyname/webapp/controller/settings/ProfileSettingsController.java
package com.saymyname.webapp.controller.settings;

import java.security.Principal;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.service.UserService;
import com.saymyname.webapp.dto.settings.UpdateUserSettingsDto;
import com.saymyname.webapp.dto.settings.UserSettingsDto;
import com.saymyname.webapp.mapper.UserSettingsDtoMapper;

@RestController
@RequestMapping("/api/profile/settings")
public class ProfileSettingsController {

    private final UserService userService;
    private final UserSettingsDtoMapper userSettingsDtoMapper;

    public ProfileSettingsController(UserService userService, UserSettingsDtoMapper userSettingsDtoMapper) {
        this.userService = userService;
        this.userSettingsDtoMapper = userSettingsDtoMapper;
    }

    /**
     * GET /api/profile/settings : retourne les settings de l'utilisateur courant.
     */
    @GetMapping
    public ResponseEntity<UserSettingsDto> getMySettings(Principal principal) {
        // 1) récupérer le User (MODEL) à partir de l'identifiant d'auth
        User me = userService.getCurrentUserOrThrow(principal);
        // 2) exposer seulement les settings nécessaires
        return ResponseEntity.ok(userSettingsDtoMapper.toDto(me));
    }

    /**
     * PATCH /api/profile/settings : met à jour partiellement (ici: srsAlgorithm).
     */
    @PatchMapping
    public ResponseEntity<UserSettingsDto> patchMySettings(
            Principal principal,
            @RequestBody UpdateUserSettingsDto dto) {

        User me = userService.getCurrentUserOrThrow(principal);

        if (userSettingsDtoMapper.hasSrsUpdate(dto)) {
            SrsAlgorithm newAlgo = dto.srsAlgorithm();
            me = userService.updateSrsAlgorithm(me, newAlgo); // retourne le MODEL mis à jour
        }

        return ResponseEntity.ok(userSettingsDtoMapper.toDto(me));
    }
}
