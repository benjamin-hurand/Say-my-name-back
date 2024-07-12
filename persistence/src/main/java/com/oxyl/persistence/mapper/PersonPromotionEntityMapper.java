package com.oxyl.persistence.mapper;

import com.oxyl.core.model.PersonPromotion;
import com.oxyl.persistence.entity.PersonPromotionEntity;
import com.oxyl.persistence.entity.PromotionEntity;
import org.springframework.stereotype.Component;

@Component
public class PersonPromotionEntityMapper {

    private final PromotionEntityMapper promotionEntityMapper;

    public PersonPromotionEntityMapper(PromotionEntityMapper promotionEntityMapper) {
        this.promotionEntityMapper = promotionEntityMapper;
    }

    public PersonPromotionEntity toEntity(PersonPromotion personPromotion) {
        return new PersonPromotionEntity(personPromotion.getId(),
                promotionEntityMapper.toEntity(personPromotion.getPromotion()),
                personPromotion.getType());
    }

    public PersonPromotion toModel(PersonPromotionEntity personPromotionEntity) {
        return new PersonPromotion.Builder()
                .id(personPromotionEntity.getId())
                .promotion(promotionEntityMapper.toModel(personPromotionEntity.getPromotion()))
                .type(personPromotionEntity.getType())
                .build();
    }

}
