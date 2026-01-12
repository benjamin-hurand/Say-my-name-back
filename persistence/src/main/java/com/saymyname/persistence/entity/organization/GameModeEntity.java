package com.saymyname.persistence.entity.organization;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.saymyname.persistence.multitenancy.BaseOrgScoped;

@Entity
@Table(name = "game_modes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_game_modes_org_title", columnNames = { "organization_id", "game_mode_title" })
})
public class GameModeEntity extends BaseOrgScoped {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Optionnel: utile pour If-Match / ETag */
    @Version
    private Long version;

    @Column(name = "game_mode_title", nullable = false, length = 255)
    private String gameModeTitle;

    @Column(name = "game_mode_description", columnDefinition = "TEXT")
    private String gameModeDescription;

    /** AND/OR (idéalement enum côté JPA, mais OK string) */
    @Column(name = "operator", nullable = false, length = 50)
    private String operator;

    @OneToMany(mappedBy = "gameMode", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("id ASC")
    private List<GameModeAttributeEntity> gameModeAttributes = new ArrayList<>();

    public GameModeEntity() {
    }

    // getters/setters

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getVersion() {
        return version;
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

    // Helpers (important avec orphanRemoval)
    public void clearAttributes() {
        for (var a : gameModeAttributes)
            a.setGameMode(null);
        gameModeAttributes.clear();
    }

    public void addAttribute(GameModeAttributeEntity attr) {
        gameModeAttributes.add(attr);
        attr.setGameMode(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof GameModeEntity that))
            return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
