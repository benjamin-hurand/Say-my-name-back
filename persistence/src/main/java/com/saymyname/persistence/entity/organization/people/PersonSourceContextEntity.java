package com.saymyname.persistence.entity.organization.people;

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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "person_source_contexts", indexes = {
        @Index(name = "idx_psc_source", columnList = "person_source_id"),
        @Index(name = "idx_psc_ws_team", columnList = "workspace_id,team_id"),
        @Index(name = "fk_psc_team", columnList = "team_id")
})
public class PersonSourceContextEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "person_source_id", nullable = false, foreignKey = @ForeignKey(name = "fk_psc_person_source"))
    private PersonSourceEntity personSource;

    @Column(name = "context_kind", nullable = false, length = 24)
    private String contextKind;

    @Column(name = "workspace_id")
    private Long workspaceId;

    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "role_label", length = 120)
    private String roleLabel;

    @Column(name = "team_name_snapshot", length = 120)
    private String teamNameSnapshot;

    @Column(name = "workspace_name_snapshot", length = 255)
    private String workspaceNameSnapshot;

    @Column(name = "organization_name_snapshot", length = 255)
    private String organizationNameSnapshot;

    @Column(name = "payload_json", columnDefinition = "json")
    private String payloadJson;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;
}