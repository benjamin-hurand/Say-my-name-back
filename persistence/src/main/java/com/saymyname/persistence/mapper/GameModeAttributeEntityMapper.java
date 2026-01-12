package com.saymyname.persistence.mapper;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.quiz.options.GameModeAttribute;
import com.saymyname.persistence.entity.organization.GameModeAttributeEntity;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;

@Component
public class GameModeAttributeEntityMapper {

    private final AttributeEntityMapper attributeEntityMapper;

    public GameModeAttributeEntityMapper(AttributeEntityMapper attributeEntityMapper) {
        this.attributeEntityMapper = attributeEntityMapper;
    }

    /**
     * Model -> Entity
     * Note: ne set PAS le gameMode ici, car en général on le fixe dans le DAO
     * (owner entity déjà connu).
     */
    public GameModeAttributeEntity toEntity(GameModeAttribute model) {
        if (model == null)
            return null;

        GameModeAttributeEntity e = new GameModeAttributeEntity();

        // id: idem, si tu n'as pas de setter id dans l'entity, ne pas le set.
        // Si tu as setId(), tu peux le décommenter.
        // e.setId(model.getId());

        // attribute
        if (model.getAttribute() != null) {
            // important : ton AttributeEntityMapper doit idéalement supporter un mapping
            // "id-only"
            AttributeEntity attrEntity = attributeEntityMapper.toEntity(model.getAttribute());
            e.setAttribute(attrEntity);
        }

        return e;
    }

    /**
     * Entity -> Model
     */
    public GameModeAttribute toModel(GameModeAttributeEntity entity) {
        if (entity == null)
            return null;

        Attribute attrModel = entity.getAttribute() == null
                ? null
                : attributeEntityMapper.toModel(entity.getAttribute());

        return new GameModeAttribute.Builder()
                .withId(entity.getId())
                .withAttribute(attrModel)
                .build();
    }
}
