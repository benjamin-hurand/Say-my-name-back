package com.oxyl.service;

import com.oxyl.core.model.Theme;
import com.oxyl.persistence.dao.ThemeDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ThemeService {
    private ThemeDao themeDao;

    public ThemeService(ThemeDao themeDao) {
        this.themeDao = themeDao;
    }

    public List<Theme> getAllThemes() {
        return themeDao.findAll();
    }

}
