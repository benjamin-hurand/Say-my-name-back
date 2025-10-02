package com.saymyname.persistence.repository;

import com.saymyname.core.model.game.options.GameOptions;
import com.saymyname.persistence.entity.organization.PersonEntity;

import java.util.List;

public interface PersonRepositoryCustom {
    List<PersonEntity> findByOptions(GameOptions options, Long userId);
}
