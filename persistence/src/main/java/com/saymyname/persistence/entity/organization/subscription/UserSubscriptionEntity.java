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
@Table(name = "user_subscriptions", indexes = {
        @Index(name = "idx_us_tenant_person", columnList = "tenant_id,person_id"),
        @Index(name = "idx_us_tenant_user", columnList = "tenant_id,user_id"),
        @Index(name = "fk_usn_user", columnList = "user_id")
})
@IdClass(UserSubscriptionKey.class)
public class UserSubscriptionEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @Column(name = "person_id", nullable = false)
    private Long personId;

    /**
     * Pour que @IdClass marche avec BaseTenantScoped :
     * on “remonte” tenantId comme propriété @Id via l’override du getter.
     */
    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @Override
    public Long getTenantId() {
        return super.getTenantId();
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_usn_user"))
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_usn_person")),
            @JoinColumn(name = "person_id", referencedColumnName = "id", insertable = false, updatable = false)
    })
    private PersonEntity person;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}