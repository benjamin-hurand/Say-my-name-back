package com.saymyname.persistence.entity.organization.subscription;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "user_subscriptions", uniqueConstraints = {
        @UniqueConstraint(name = "uq_us_tenant_user_person", columnNames = { "tenant_id", "user_id", "person_id" })
}, indexes = {
        @Index(name = "idx_us_tenant_person", columnList = "tenant_id,person_id"),
        @Index(name = "fk_usn_user", columnList = "user_id")
})
public class UserSubscriptionEntity extends BaseTenantScoped {

    // ---------------------------------------------------------------------
    // PK
    // ---------------------------------------------------------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    @Column(name = "id")
    private Long id;

    // ---------------------------------------------------------------------
    // FK writer fields
    // ---------------------------------------------------------------------

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    // ---------------------------------------------------------------------
    // Relations (read-only)
    // ---------------------------------------------------------------------

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_usn_user"))
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_usn_person")),
            @JoinColumn(name = "person_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    private PersonEntity person;

    // ---------------------------------------------------------------------

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}