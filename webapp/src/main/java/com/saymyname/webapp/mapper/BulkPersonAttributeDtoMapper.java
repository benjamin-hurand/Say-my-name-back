// src/main/java/com/saymyname/webapp/mapper/BulkPersonAttributeDtoMapper.java
package com.saymyname.webapp.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.PersonAttribute;
import com.saymyname.webapp.dto.profile.BulkPersonAttributeRequest;

@Component
public class BulkPersonAttributeDtoMapper {

    public List<PersonAttribute> toCreateModels(List<BulkPersonAttributeRequest.CreateItem> src) {
        if (src == null)
            return Collections.emptyList();
        return src.stream()
                .map(c -> PersonAttribute.builder()
                        .value(c.value())
                        .build())
                .toList();
    }

    public List<PersonAttribute> toUpdateModels(List<BulkPersonAttributeRequest.UpdateItem> src) {
        if (src == null)
            return Collections.emptyList();
        return src.stream()
                .map(u -> PersonAttribute.builder()
                        .id(u.id())
                        .value(u.value())
                        .build())
                .toList();
    }

    public List<PersonAttribute> toDeleteModels(List<BulkPersonAttributeRequest.DeleteItem> src) {
        if (src == null)
            return Collections.emptyList();
        return src.stream()
                .map(d -> PersonAttribute.builder()
                        .id(d.id())
                        .build())
                .toList();
    }
}
