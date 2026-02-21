package com.saymyname.persistence.entity.organization;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import com.saymyname.persistence.entity.organization.TenantOrgEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "org_policies")
public class OrgPolicyEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @Column(name = "tenant_id", nullable = false)
    private Long tenantId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tenant_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_org_policies_tenant_org"))
    private TenantOrgEntity organization;

    @Column(name = "require_sso", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean requireSso;

    @Column(name = "external_needs_approval", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean externalNeedsApproval;

    @Column(name = "link_default_max_uses", nullable = false, columnDefinition = "int default 25")
    private int linkDefaultMaxUses;

    @Column(name = "link_default_ttl_days", nullable = false, columnDefinition = "int default 14")
    private int linkDefaultTtlDays;

    @Column(name = "allowed_domains_json", columnDefinition = "json")
    private String allowedDomainsJson;

    @Column(name = "blocked_domains_json", columnDefinition = "json")
    private String blockedDomainsJson;
}