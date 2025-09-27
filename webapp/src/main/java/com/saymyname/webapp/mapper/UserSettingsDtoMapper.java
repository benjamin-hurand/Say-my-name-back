// src/main/java/com/saymyname/webapp/mapper/UserSettingsDtoMapper.java
package com.saymyname.webapp.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.webapp.dto.settings.UpdateUserSettingsDto;
import com.saymyname.webapp.dto.settings.UserSettingsDto;

@Component
public class UserSettingsDtoMapper {

    private UserSettingsDtoMapper() {
    }

    /** Extrait les settings (ici SRS) du modèle User pour exposer au front. */
    public UserSettingsDto toDto(User user) {
        return new UserSettingsDto(user.getSrsAlgorithm());
    }

    /**
     * Rien à mapper vers un Model "settings" dédié, on laisse le Controller/Service
     * gérer la valeur optionnelle.
     */
    public boolean hasSrsUpdate(UpdateUserSettingsDto dto) {
        return dto != null && dto.srsAlgorithm() != null;
    }
}
