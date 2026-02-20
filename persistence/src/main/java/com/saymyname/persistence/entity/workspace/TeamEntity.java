package com.saymyname.persistence.entity.workspace;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "teams", uniqueConstraints = {
        @UniqueConstraint(name = "uq_team_ws_name", columnNames = {"workspace_id", "name"}),
        @UniqueConstraint(name = "uq_teams_ws_id", columnNames = {"workspace_id", "id"})
}, indexes = {
        @Index(name = "idx_team_ws_parent", columnList = "workspace_id,parent_team_id"),
        @Index(name = "fk_team_parent", columnList = "parent_team_id")
})
public class TeamEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false, foreignKey = @ForeignKey(name = "fk_team_workspace"))
    private WorkspaceEntity workspace;

    @Column(name = "parent_team_id")
    private Long parentTeamId;

    @Column(name = "name", nullable = false, length = 120)
    private String name;

    @Column(name = "description", length = 512)
    private String description;

    @Column(name = "display_order", nullable = false, columnDefinition = "int default 0")
    private int displayOrder;

    @Column(name = "is_active", nullable = false, columnDefinition = "tinyint(1) default 1")
    private boolean active;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "datetime default current_timestamp on update current_timestamp")
    private LocalDateTime updatedAt;
}