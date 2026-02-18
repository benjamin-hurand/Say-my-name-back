package com.saymyname.persistence.entity.organization.leaderboard;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "xp_events", indexes = {
        @Index(name = "idx_xp_event_source", columnList = "source_type,source_id"),
        @Index(name = "idx_xp_event_key", columnList = "event_key"),
        @Index(name = "idx_xp_tenant_user_created", columnList = "tenant_id,user_id,created_at,id"),
        @Index(name = "idx_xp_tenant_created", columnList = "tenant_id,created_at,id")
})
public class XpEventEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_xp_user"))
    private UserEntity user;

    @Column(name = "event_id", nullable = false, columnDefinition = "binary(16)")
    private byte[] eventId;

    @Column(name = "event_key", nullable = false, length = 64)
    private String eventKey;

    @Column(name = "source_type", nullable = false, length = 32)
    private String sourceType;

    @Column(name = "source_id")
    private Long sourceId;

    @Column(name = "delta_xp", nullable = false)
    private int deltaXp;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}