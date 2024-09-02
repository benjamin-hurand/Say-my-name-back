package com.oxyl.persistence.repository;

import com.oxyl.core.model.game.options.GameOptions;
import java.util.List;

public interface PhotoRepositoryCustom {
    List<Long> findPhotoIdsByDynamicFilters(GameOptions gameOptions);
}
