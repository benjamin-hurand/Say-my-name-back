package com.saymyname.persistence.dao;

import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.persistence.entity.organization.attribute.AttributeEnumOptionEntity;
import com.saymyname.persistence.mapper.AttributeEnumOptionEntityMapper;
import com.saymyname.persistence.repository.AttributeEnumOptionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class AttributeEnumOptionDao {

    private final AttributeEnumOptionRepository repo;
    private final AttributeEnumOptionEntityMapper mapper;

    public AttributeEnumOptionDao(AttributeEnumOptionRepository repo,
            AttributeEnumOptionEntityMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Set<String> findActiveCodesByAttributeId(Long attributeId) {
        return repo.findActiveCodesByAttributeId(attributeId);
    }

    @Transactional(readOnly = true)
    public Set<String> findAllCodesByAttributeId(Long attributeId) {
        return repo.findAllCodesByAttributeId(attributeId);
    }

    @Transactional(readOnly = true)
    public List<AttributeEnumOption> findActiveOptionsByAttributeId(Long attributeId) {
        List<AttributeEnumOptionEntity> entities = repo
                .findByAttribute_IdAndActiveTrueOrderByOrderIndexAscLabelAsc(attributeId);
        return entities.stream().map(mapper::toModel).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Map<Long, List<AttributeEnumOption>> findActiveOptionsByAttributeIds(Collection<Long> attributeIds) {
        if (attributeIds == null || attributeIds.isEmpty())
            return Collections.emptyMap();
        List<AttributeEnumOptionEntity> entities = repo
                .findByAttribute_IdInAndActiveTrueOrderByAttribute_IdAscOrderIndexAscLabelAsc(attributeIds);

        return entities.stream()
                .map(mapper::toModel)
                .collect(Collectors.groupingBy(AttributeEnumOption::getAttributeId, Collectors.toList()));
    }
}
