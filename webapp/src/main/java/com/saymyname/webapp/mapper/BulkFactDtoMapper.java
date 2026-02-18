// src/main/java/com/saymyname/webapp/mapper/BulkFactDtoMapper.java
package com.saymyname.webapp.mapper;

import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Fact;
import com.saymyname.webapp.dto.profile.BulkFactRequest;

@Component
public class BulkFactDtoMapper {

    public List<Fact> toCreateModels(List<BulkFactRequest.CreateItem> src) {
        if (src == null)
            return Collections.emptyList();
        return src.stream()
                .map(c -> Fact.builder()
                        .value(c.value())
                        .build())
                .toList();
    }

    public List<Fact> toUpdateModels(List<BulkFactRequest.UpdateItem> src) {
        if (src == null)
            return Collections.emptyList();
        return src.stream()
                .map(u -> Fact.builder()
                        .id(u.id())
                        .value(u.value())
                        .build())
                .toList();
    }

    public List<Fact> toDeleteModels(List<BulkFactRequest.DeleteItem> src) {
        if (src == null)
            return Collections.emptyList();
        return src.stream()
                .map(d -> Fact.builder()
                        .id(d.id())
                        .build())
                .toList();
    }
}
