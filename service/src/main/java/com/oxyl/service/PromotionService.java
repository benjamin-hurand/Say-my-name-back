package com.oxyl.service;

import com.oxyl.core.model.people.Promotion;
import com.oxyl.persistence.dao.PromotionDao;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromotionService {
    private final PromotionDao promotionDao;

    public PromotionService(PromotionDao promotionDao) {
        this.promotionDao = promotionDao;
    }

    public List<Promotion> findAll() {
        return promotionDao.findAll();
    }
}
