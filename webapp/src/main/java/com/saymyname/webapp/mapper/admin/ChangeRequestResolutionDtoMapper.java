// src/main/java/com/saymyname/webapp/mapper/ChangeRequestResolutionDtoMapper.java
package com.saymyname.webapp.mapper.admin;

import java.util.List;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.people.BulkChangeRequestResolution;
import com.saymyname.core.model.people.ChangeRequestResolution;
import com.saymyname.core.model.people.ChangeRequestResolutionItem;
import com.saymyname.webapp.dto.admin.BulkResolveChangeRequestsDto;
import com.saymyname.webapp.dto.admin.ResolveChangeRequestDto;
import com.saymyname.webapp.dto.admin.ResolveChangeRequestItemDto;

@Component
public class ChangeRequestResolutionDtoMapper {

    /** Web DTO -> Core Model (commande). */
    public ChangeRequestResolution toModel(Long changeRequestId, User resolver, ResolveChangeRequestDto in) {
        if (changeRequestId == null)
            throw new IllegalArgumentException("changeRequestId is required");
        if (resolver == null || resolver.getId() == null)
            throw new IllegalArgumentException("resolver (with id) is required");

        List<ChangeRequestResolutionItem> items = (in != null && in.decisions() != null)
                ? in.decisions().stream().map(this::toItem).toList()
                : List.of();

        return new ChangeRequestResolution.Builder()
                .withChangeRequestId(changeRequestId)
                .withResolver(resolver)
                .withResolutionComment(in != null ? in.resolutionComment() : null)
                .withDecisions(items)
                .build();
    }

    private ChangeRequestResolutionItem toItem(ResolveChangeRequestItemDto d) {
        if (d == null)
            return null;
        return new ChangeRequestResolutionItem.Builder()
                .withItemId(d.itemId())
                .withDecision(d.decision())
                .withResolutionComment(d.resolutionComment())
                .build();
    }

    public BulkChangeRequestResolution toModel(BulkResolveChangeRequestsDto dto, User resolver) {
        return BulkChangeRequestResolution.builder()
                .resolver(resolver)
                .changeRequestIds(dto.ids())
                .decision(dto.decision())
                .resolutionComment(dto.resolutionComment())
                .build();
    }

    /* Optionnel: echo Model -> DTO */
    public ResolveChangeRequestDto toDto(ChangeRequestResolution model) {
        if (model == null)
            return new ResolveChangeRequestDto(null, List.of());
        List<ResolveChangeRequestItemDto> items = model.getDecisions().stream()
                .map(it -> new ResolveChangeRequestItemDto(it.getItemId(), it.getDecision(), it.getResolutionComment()))
                .toList();
        return new ResolveChangeRequestDto(model.getResolutionComment(), items);
    }
}
