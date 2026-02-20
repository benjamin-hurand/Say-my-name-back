package com.saymyname.persistence.entity.organization.subscription;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;
import com.saymyname.core.multitenancy.TenantContext;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@Embeddable
public class UserSubscriptionId implements Serializable {

    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    // Backward-compatible ctor for legacy call sites.
    public UserSubscriptionId(Long userId, Long personId) {
        this.tenantId = TenantContext.get();
        this.userId = userId;
        this.personId = personId;
    }
}
