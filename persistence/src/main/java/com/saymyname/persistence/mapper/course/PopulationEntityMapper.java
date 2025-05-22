package com.saymyname.persistence.mapper.course;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Population;
import com.saymyname.persistence.entity.course.PopulationEntity;
import com.saymyname.persistence.mapper.AttributeEntityMapper;
import com.saymyname.persistence.mapper.UserEntityMapper;

@Component
public class PopulationEntityMapper {

    private final AttributeEntityMapper attributeMapper;
    private final UserEntityMapper userMapper;

    @Autowired
    public PopulationEntityMapper(AttributeEntityMapper attributeMapper,
            UserEntityMapper userMapper) {
        this.attributeMapper = attributeMapper;
        this.userMapper = userMapper;
    }

    public PopulationEntity toEntity(Population model) {
        if (model == null)
            return null;
        PopulationEntity e = new PopulationEntity();
        e.setId(model.getId());
        e.setTitle(model.getTitle());
        e.setDescription(model.getDescription());
        e.setAttributeFilter(attributeMapper.toEntity(model.getAttributeFilter()));
        e.setMinValue(model.getMinValue());
        e.setMaxValue(model.getMaxValue());
        e.setCreatedBy(userMapper.toEntity(model.getCreatedBy()));
        e.setCountPersons(model.getCount());
        // createdAt est géré par la DB
        return e;
    }

    public Population toModel(PopulationEntity e) {
        if (e == null)
            return null;
        return new Population.Builder()
                .withId(e.getId())
                .withTitle(e.getTitle())
                .withDescription(e.getDescription())
                .withAttributeFilter(attributeMapper.toModel(e.getAttributeFilter()))
                .withMinValue(e.getMinValue())
                .withMaxValue(e.getMaxValue())
                .withCreatedBy(userMapper.toModel(e.getCreatedBy()))
                .withCount(e.getCountPersons())
                .build();
    }

    public List<Population> toModelList(List<PopulationEntity> entities) {
        return entities.stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    public List<PopulationEntity> toEntityList(List<Population> models) {
        return models.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
