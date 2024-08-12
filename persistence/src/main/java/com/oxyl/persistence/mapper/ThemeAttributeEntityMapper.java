package com.oxyl.persistence.mapper;

import com.oxyl.core.model.ThemeAttribute;
import com.oxyl.persistence.entity.ThemeAttributeEntity;
import org.springframework.stereotype.Component;

@Component
public class ThemeAttributeEntityMapper {

    private final AttributeEntityMapper attributeEntityMapper;

    public ThemeAttributeEntityMapper(AttributeEntityMapper attributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
    }

    public ThemeAttributeEntity toEntity(ThemeAttribute themeAttribute) {
        return new ThemeAttributeEntity(themeAttribute.getId(), themeAttribute.getOperator(), attributeEntityMapper.toEntity(themeAttribute.getAttribute()));
    }

    public ThemeAttribute toModel(ThemeAttributeEntity themeAttributeEntity) {
        return new ThemeAttribute.Builder()
                .withId(themeAttributeEntity.getId())
                .withOperator(themeAttributeEntity.getOperator())
                .withAttribute(attributeEntityMapper.toModel(themeAttributeEntity.getAttribute()))
                .build();
    }
}
