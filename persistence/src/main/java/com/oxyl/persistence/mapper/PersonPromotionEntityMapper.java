package com.oxyl.persistence.mapper;

import com.oxyl.core.model.people.PersonPromotion;
import com.oxyl.persistence.entity.PersonPromotionEntity;
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

    public PersonPromotion toGameModel(PersonPromotionEntity personPromotionEntity) {
        return new PersonPromotion.Builder()
                .id(personPromotionEntity.getId())
                .promotion(promotionEntityMapper.toGameModel(personPromotionEntity.getPromotion()))
                .type(personPromotionEntity.getType())
                .build();
    }

}
