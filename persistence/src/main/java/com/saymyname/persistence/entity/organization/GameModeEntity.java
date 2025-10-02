package com.saymyname.persistence.entity.organization;

import jakarta.persistence.*;
import java.util.List;
import java.util.Objects;

import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "game_modes")
public class GameModeEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "game_mode_title", nullable = false, length = 255)
    private String gameModeTitle;

    @Column(name = "game_mode_description", columnDefinition = "TEXT")
    private String gameModeDescription;

    @Column(name = "operator", nullable = false, length = 50)
    private String operator;

    @OneToMany(mappedBy = "gameMode")
    private List<GameModeAttributeEntity> gameModeAttributes;

    // Constructors, getters, setters, equals, hashCode, and toString methods

    public GameModeEntity() {
    }

    public GameModeEntity(String gameModeTitle, String gameModeDescription, String operator) {
        this.gameModeTitle = gameModeTitle;
        this.gameModeDescription = gameModeDescription;
        this.operator = operator;
    }

    public GameModeEntity(Long id, String gameModeTitle, String gameModeDescription, String operator,
            List<GameModeAttributeEntity> gameModeAttributes) {
        this.id = id;
        this.gameModeTitle = gameModeTitle;
        this.gameModeDescription = gameModeDescription;
        this.operator = operator;
        this.gameModeAttributes = gameModeAttributes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGameModeTitle() {
        return gameModeTitle;
    }

    public void setGameModeTitle(String gameModeTitle) {
        this.gameModeTitle = gameModeTitle;
    }

    public String getGameModeDescription() {
        return gameModeDescription;
    }

    public void setGameModeDescription(String gameModeDescription) {
        this.gameModeDescription = gameModeDescription;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public List<GameModeAttributeEntity> getGameModeAttributes() {
        return gameModeAttributes;
    }

    public void setGameModeAttributes(List<GameModeAttributeEntity> gameModeAttributes) {
        this.gameModeAttributes = gameModeAttributes;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GameModeEntity))
            return false;
        GameModeEntity that = (GameModeEntity) o;
        return id == that.id &&
                Objects.equals(gameModeTitle, that.gameModeTitle) &&
                Objects.equals(gameModeDescription, that.gameModeDescription) &&
                Objects.equals(operator, that.operator) &&
                Objects.equals(gameModeAttributes, that.gameModeAttributes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, gameModeTitle, gameModeDescription, operator, gameModeAttributes);
    }

    @Override
    public String toString() {
        return "GameModeEntity{" +
                "id=" + id +
                ", gameModeTitle='" + gameModeTitle + '\'' +
                ", gameModeDescription='" + gameModeDescription + '\'' +
                ", operator='" + operator + '\'' +
                ", gameModeAttributes=" + gameModeAttributes +
                '}';
    }
}
