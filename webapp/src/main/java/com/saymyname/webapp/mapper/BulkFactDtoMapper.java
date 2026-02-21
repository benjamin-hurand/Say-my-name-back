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
                .map(c -> new Fact.Builder()
                        .withValue(c.value())
                        .build())
                .toList();
    }

    public List<Fact> toUpdateModels(List<BulkFactRequest.UpdateItem> src) {
        if (src == null)
            return Collections.emptyList();
        return src.stream()
                .map(u -> new Fact.Builder()
                        .withId(u.id())
                        .withValue(u.value())
                        .build())
                .toList();
    }

    public List<Fact> toDeleteModels(List<BulkFactRequest.DeleteItem> src) {
        if (src == null)
            return Collections.emptyList();
        return src.stream()
                .map(d -> new Fact.Builder()
                        .withId(d.id())
                        .build())
                .toList();
    }
}
