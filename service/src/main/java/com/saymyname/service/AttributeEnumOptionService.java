// src/main/java/com/saymyname/service/AttributeEnumOptionService.java
package com.saymyname.service;

import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.persistence.dao.AttributeEnumOptionDao;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Service
public class AttributeEnumOptionService {

    private final AttributeEnumOptionDao dao;

    public AttributeEnumOptionService(AttributeEnumOptionDao dao) {
        this.dao = dao;
    }

    public Map<Long, List<AttributeEnumOption>> getActiveOptionsByAttributeIds(Collection<Long> attributeIds) {
        return dao.findActiveOptionsByAttributeIds(attributeIds);
    }
}
