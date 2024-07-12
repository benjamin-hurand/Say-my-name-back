package com.oxyl.persistence.dao;

import com.oxyl.core.model.Promotion;
import com.oxyl.persistence.mapper.PromotionEntityMapper;
import com.oxyl.persistence.repository.PromotionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class PromotionDao {

    private final PromotionRepository promotionRepository;
    private final PromotionEntityMapper promotionEntityMapper;

    public PromotionDao(PromotionRepository promotionRepository, PromotionEntityMapper promotionEntityMapper) {
        this.promotionRepository = promotionRepository;
        this.promotionEntityMapper = promotionEntityMapper;
    }

    @Transactional
    public List<Promotion> findAll() {
        return promotionRepository.findAll().stream().map(promotionEntityMapper::toModel).toList();
    }
}
