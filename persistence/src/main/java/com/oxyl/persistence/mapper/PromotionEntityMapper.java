package com.oxyl.persistence.mapper;

import com.oxyl.core.model.people.Promotion;
import com.oxyl.persistence.entity.PromotionEntity;
import org.springframework.stereotype.Component;

@Component
public class PromotionEntityMapper {

    public PromotionEntity toEntity(Promotion promotion) {
        if(promotion == null) return null;
        return new PromotionEntity(promotion.getId(), promotion.getMonth(), promotion.getYear());
    }

    public Promotion toGameModel(PromotionEntity promotionEntity) {
        if(promotionEntity == null) return null;
        return new Promotion.Builder()
                .withId(promotionEntity.getId())
                .withMonth(promotionEntity.getMonth())
                .withYear(promotionEntity.getYear())
                .build();
    }


}
