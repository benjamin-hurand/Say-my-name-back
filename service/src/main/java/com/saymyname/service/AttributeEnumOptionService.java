// src/main/java/com/saymyname/service/AttributeEnumOptionService.java
package com.saymyname.service;

import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.persistence.dao.AttributeEnumOptionDao;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.saymyname.core.exception.common.ValidationException;

@Service
public class AttributeEnumOptionService {

    private final AttributeEnumOptionDao dao;

    public AttributeEnumOptionService(AttributeEnumOptionDao dao) {
        this.dao = dao;
    }

    public Map<Long, List<AttributeEnumOption>> getActiveOptionsByAttributeIds(Collection<Long> attributeIds) {
        return dao.findActiveOptionsByAttributeIds(attributeIds);
    }

    public List<AttributeEnumOption> getActiveOptionsByAttributeId(Long attributeId) {
        return dao.findActiveOptionsByAttributeId(attributeId);
    }

    public void replaceActiveOptions(Long attributeId, List<String> requestedLabels) {
        if (attributeId == null || requestedLabels == null) {
            return;
        }

        List<String> labels = normalizeLabels(requestedLabels);
        List<AttributeEnumOption> existing = dao.findAllOptionsByAttributeId(attributeId);
        Map<String, AttributeEnumOption> existingByLabel = new HashMap<>();
        Set<String> usedCodes = new HashSet<>();
        for (AttributeEnumOption option : existing) {
            existingByLabel.put(option.getLabel().trim().toLowerCase(Locale.ROOT), option);
            usedCodes.add(option.getCode().trim().toLowerCase(Locale.ROOT));
            option.setActive(false);
        }

        List<AttributeEnumOption> toSave = new ArrayList<>(existing);
        for (int index = 0; index < labels.size(); index++) {
            String label = labels.get(index);
            String key = label.toLowerCase(Locale.ROOT);
            AttributeEnumOption option = existingByLabel.get(key);
            if (option == null) {
                String code = label;
                if (code.length() > 64) {
                    throw new ValidationException("Le code d'une option ENUM ne peut pas dépasser 64 caractères");
                }
                if (!usedCodes.add(code.toLowerCase(Locale.ROOT))) {
                    throw new ValidationException("Deux options ENUM ne peuvent pas partager le même code");
                }
                option = new AttributeEnumOption(null, attributeId, code, label, index, true);
                toSave.add(option);
            } else {
                option.setLabel(label);
                option.setOrderIndex(index);
                option.setActive(true);
            }
        }

        dao.saveAll(toSave);
    }

    private static List<String> normalizeLabels(List<String> labels) {
        List<String> normalized = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (String raw : labels) {
            String label = raw == null ? "" : raw.trim();
            if (label.isEmpty()) {
                continue;
            }
            String key = label.toLowerCase(Locale.ROOT);
            if (!seen.add(key)) {
                throw new ValidationException("Les options ENUM doivent être uniques");
            }
            normalized.add(label);
        }
        return normalized;
    }
}
