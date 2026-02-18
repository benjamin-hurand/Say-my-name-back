package com.saymyname.persistence.entity.organization.people;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "person_sources", uniqueConstraints = {
        @UniqueConstraint(name = "uq_ps_person_source", columnNames = {"person_id", "source_kind", "source_person_id"})
}, indexes = {
        @Index(name = "idx_ps_person", columnList = "person_id"),
        @Index(name = "fk_ps_tenant", columnList = "tenant_id")
})
public class PersonSourceEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "source_kind", nullable = false, length = 24)
    private String sourceKind;

    @Column(name = "source_person_id")
    private Long sourcePersonId;

    @Column(name = "source_snapshot_json", columnDefinition = "json")
    private String sourceSnapshotJson;

    @Column(name = "source_snapshot_hash", columnDefinition = "varbinary(32)")
    private byte[] sourceSnapshotHash;

    @Column(name = "copied_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime copiedAt;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;
}