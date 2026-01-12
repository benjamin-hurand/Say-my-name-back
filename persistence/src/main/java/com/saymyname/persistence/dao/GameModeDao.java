package com.saymyname.persistence.dao;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.core.model.quiz.options.GameModeAttribute;
import com.saymyname.persistence.entity.organization.GameModeEntity;
import com.saymyname.persistence.entity.organization.GameModeAttributeEntity;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import com.saymyname.persistence.mapper.GameModeEntityMapper;
import com.saymyname.persistence.repository.AttributeRepository;
import com.saymyname.persistence.repository.GameModeRepository;

@Repository
@Transactional
public class GameModeDao {

    private final GameModeRepository gameModeRepository;
    private final AttributeRepository attributeRepository;
    private final GameModeEntityMapper mapper;

    public GameModeDao(
            GameModeRepository gameModeRepository,
            AttributeRepository attributeRepository,
            GameModeEntityMapper mapper) {
        this.gameModeRepository = gameModeRepository;
        this.attributeRepository = attributeRepository;
        this.mapper = mapper;
    }

    public List<GameMode> findAll() {
        return gameModeRepository.findAllInCurrentOrg().stream()
                .map(mapper::toModel)
                .toList();
    }

    public Optional<GameMode> findById(Long id) {
        if (id == null)
            return Optional.empty();
        return gameModeRepository.findByIdWithAttributesInCurrentOrg(id).map(mapper::toModel);
    }

    public Optional<GameMode> findByGameModeTitle(String title) {
        if (title == null || title.isBlank())
            return Optional.empty();
        return gameModeRepository.findByTitleInCurrentOrg(title.trim()).map(mapper::toModel);
    }

    public GameMode create(GameMode model) {
        String title = model.getTitle().trim();
        gameModeRepository.findByTitleInCurrentOrg(title).ifPresent(x -> {
            throw new IllegalArgumentException("GameMode title already exists in org: " + title);
        });

        GameModeEntity entity = mapper.toNewEntity(model);

        applyReplaceAttributes(entity, model.getGameModeAttributes());

        GameModeEntity saved = gameModeRepository.save(entity);

        // reload fetch pour renvoyer attrs complets
        return mapper.toModel(gameModeRepository.findByIdWithAttributesInCurrentOrg(saved.getId()).orElse(saved));
    }

    public GameMode update(GameMode model) {
        Long id = model.getId();
        GameModeEntity existing = gameModeRepository.findByIdWithAttributesInCurrentOrg(id)
                .orElseThrow(() -> new NoSuchElementException("GameMode not found: id=" + id));

        String title = model.getTitle().trim();
        gameModeRepository.findByTitleInCurrentOrg(title).ifPresent(other -> {
            if (!Objects.equals(other.getId(), id)) {
                throw new IllegalArgumentException("GameMode title already exists in org: " + title);
            }
        });

        mapper.updateEntity(existing, model);
        applyReplaceAttributes(existing, model.getGameModeAttributes());

        GameModeEntity saved = gameModeRepository.save(existing);
        return mapper.toModel(gameModeRepository.findByIdWithAttributesInCurrentOrg(saved.getId()).orElse(saved));
    }

    public boolean delete(Long id) {
        int deleted = gameModeRepository.deleteInCurrentOrg(id);
        return deleted > 0;
    }

    public void replaceAttributes(Long gameModeId, List<GameModeAttribute> attributes) {
        GameModeEntity existing = gameModeRepository.findByIdWithAttributesInCurrentOrg(gameModeId)
                .orElseThrow(() -> new NoSuchElementException("GameMode not found: id=" + gameModeId));

        applyReplaceAttributes(existing, attributes);
        gameModeRepository.save(existing);
    }

    // ===== Internal =====

    private void applyReplaceAttributes(GameModeEntity gm, List<GameModeAttribute> attrs) {

        List<GameModeAttribute> safeAttrs = (attrs == null) ? List.of() : attrs;

        List<Long> ids = safeAttrs.stream()
                .map(a -> a.getAttribute() != null ? a.getAttribute().getId() : null)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        List<AttributeEntity> found = ids.isEmpty()
                ? List.of()
                : attributeRepository.findAllByIdsInCurrentOrg(ids);

        Map<Long, AttributeEntity> byId = found.stream()
                .collect(Collectors.toMap(AttributeEntity::getId, a -> a));

        if (byId.size() != ids.size()) {
            Set<Long> missing = new HashSet<>(ids);
            missing.removeAll(byId.keySet());
            throw new IllegalArgumentException("Unknown attributeIds in org: " + missing);
        }

        gm.clearAttributes();
        for (Long attrId : ids) {
            GameModeAttributeEntity gma = new GameModeAttributeEntity();
            gma.setAttribute(byId.get(attrId));
            gm.addAttribute(gma);
        }
    }

}
