package com.oxyl.persistence.dao;

import com.oxyl.core.model.Theme;
import com.oxyl.persistence.mapper.ThemeEntityMapper;
import com.oxyl.persistence.repository.ThemeRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@Transactional
public class ThemeDao {
    private ThemeRepository themeRepository;
    private ThemeEntityMapper themeEntityMapper;

    public ThemeDao(ThemeRepository themeRepository, ThemeEntityMapper themeEntityMapper) {
        this.themeRepository = themeRepository;
        this.themeEntityMapper = themeEntityMapper;
    }

    public List<Theme> findAll() {
        return themeRepository.findAll().stream().map(themeEntityMapper::toModel).toList();
    }
}
