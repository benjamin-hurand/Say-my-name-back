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

    public Set<String> getActiveCodesByAttributeId(Long attributeId) {
        return dao.findActiveCodesByAttributeId(attributeId);
    }

    /**
     * Reconciles an attribute's options against a fixed, backend-owned set
     * (currently used for GENDER). Unlike {@link #replaceActiveOptions}, matching
     * is done by stable code rather than label, so relabeling a system option
     * never creates a duplicate, and any option whose code isn't in
     * {@code systemOptions} is deactivated — the admin cannot append custom
     * values to a system-managed enum.
     */
    public void synchronizeSystemOptions(Long attributeId, List<AttributeEnumOption> systemOptions) {
        if (attributeId == null || systemOptions == null) {
            return;
        }

        List<AttributeEnumOption> existing = dao.findAllOptionsByAttributeId(attributeId);
        Map<String, AttributeEnumOption> existingByCode = new HashMap<>();
        for (AttributeEnumOption option : existing) {
            existingByCode.put(option.getCode(), option);
            option.setActive(false);
        }

        List<AttributeEnumOption> toSave = new ArrayList<>(existing);
        for (AttributeEnumOption system : systemOptions) {
            AttributeEnumOption option = existingByCode.get(system.getCode());
            if (option == null) {
                toSave.add(new AttributeEnumOption(null, attributeId, system.getCode(), system.getLabel(),
                        system.getOrderIndex(), true));
            } else {
                option.setLabel(system.getLabel());
                option.setOrderIndex(system.getOrderIndex());
                option.setActive(true);
            }
        }

        dao.saveAll(toSave);
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
