package com.saymyname.persistence.entity.organization;

import java.time.LocalDateTime;

import com.saymyname.core.model.enums.tenant.OrgType;
import com.saymyname.persistence.entity.TenantEntity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tenant_orgs")
@PrimaryKeyJoinColumn(name = "tenant_id", foreignKey = @ForeignKey(name = "fk_tenant_org_tenant"))
@DiscriminatorValue("ORG")
public class TenantOrgEntity extends TenantEntity {

    @Column(name = "org_key", nullable = false, length = 255)
    private String orgKey;

    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "org_type", nullable = false, length = 16)
    private OrgType orgType;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}