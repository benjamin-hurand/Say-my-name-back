package com.saymyname.persistence.entity.organization;

import jakarta.persistence.*;
import java.util.Objects;

import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "game_modes_attributes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_gma_org_gamemode_attribute", columnNames = { "organization_id", "game_mode_id",
                "attribute_id" })
})
public class GameModeAttributeEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_mode_id", nullable = false)
    private GameModeEntity gameMode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false)
    private AttributeEntity attribute;

    public GameModeAttributeEntity() {
    }

    public GameModeAttributeEntity(AttributeEntity attribute) {
        this.attribute = attribute;
    }

    public Long getId() {
        return id;
    }

    public GameModeEntity getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameModeEntity gameMode) {
        this.gameMode = gameMode;
    }

    public AttributeEntity getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeEntity attribute) {
        this.attribute = attribute;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GameModeAttributeEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
