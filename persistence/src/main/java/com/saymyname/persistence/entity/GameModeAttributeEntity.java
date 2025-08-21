package com.saymyname.persistence.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "game_modes_attributes")
public class GameModeAttributeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_mode_id")
    private GameModeEntity gameMode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_id")
    private AttributeEntity attribute;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public GameModeAttributeEntity() {
    }

    public GameModeAttributeEntity(GameModeEntity gameMode, AttributeEntity attribute) {
        this.gameMode = gameMode;
        this.attribute = attribute;
    }

    public GameModeAttributeEntity(Long id, AttributeEntity attribute) {
        this.id = id;
        this.attribute = attribute;
    }

    public GameModeAttributeEntity(Long id, GameModeEntity gameMode, AttributeEntity attribute) {
        this.id = id;
        this.gameMode = gameMode;
        this.attribute = attribute;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
        if (!(o instanceof GameModeAttributeEntity))
            return false;
        GameModeAttributeEntity that = (GameModeAttributeEntity) o;
        return id == that.id &&
                Objects.equals(gameMode, that.gameMode) &&
                Objects.equals(attribute, that.attribute);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gameMode, attribute);
    }

    @Override
    public String toString() {
        return "GameModeAttributesEntity{" +
                "id=" + id +
                ", gameMode=" + gameMode +
                ", attribute=" + attribute +
                '}';
    }
}
