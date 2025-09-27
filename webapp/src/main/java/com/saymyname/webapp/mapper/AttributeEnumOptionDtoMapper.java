package com.saymyname.webapp.mapper;

import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.webapp.dto.AttributeEnumOptionDto;
import org.springframework.stereotype.Component;

@Component
public class AttributeEnumOptionDtoMapper {

    public AttributeEnumOptionDto toDto(AttributeEnumOption m) {
        if (m == null)
            return null;
        return new AttributeEnumOptionDto(
                m.getId(),
                m.getAttributeId(),
                m.getCode(),
                m.getLabel(),
                m.getOrderIndex(),
                m.isActive());
    }

    public AttributeEnumOption toModel(AttributeEnumOptionDto dto) {
        if (dto == null)
            return null;
        AttributeEnumOption m = new AttributeEnumOption();
        m.setId(dto.id());
        m.setAttributeId(dto.attributeId());
        m.setCode(dto.code());
        m.setLabel(dto.label());
        m.setOrderIndex(dto.orderIndex() != null ? dto.orderIndex() : 100);
        m.setActive(dto.active() != null ? dto.active() : Boolean.TRUE);
        return m;
    }
}
