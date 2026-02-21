package com.saymyname.persistence.entity.organization;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import com.saymyname.core.model.enums.tenant.ScopeKind;
import com.saymyname.persistence.entity.organization.attribute.AttributeEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "facts", indexes = {
                @Index(name = "idx_facts_tenant", columnList = "tenant_id"),
                @Index(name = "idx_facts_tenant_person_attr", columnList = "tenant_id,person_id,attribute_id"),
                @Index(name = "idx_facts_tenant_attr", columnList = "tenant_id,attribute_id"),
                @Index(name = "idx_facts_workspace", columnList = "workspace_id"),
                @Index(name = "idx_facts_team", columnList = "team_id"),
                @Index(name = "idx_facts_tenant_workspace", columnList = "tenant_id,workspace_id"),
                @Index(name = "idx_facts_ws_team", columnList = "workspace_id,team_id")
})
public class FactEntity extends BaseTenantScoped {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @Enumerated(EnumType.STRING)
        @Column(name = "scope_kind", nullable = false, length = 16, columnDefinition = "enum('TENANT','WORKSPACE','TEAM')")
        private ScopeKind scopeKind;

        @Column(name = "workspace_id")
        private Long workspaceId;

        @Column(name = "team_id")
        private Long teamId;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
                        @JoinColumn(name = "person_id", referencedColumnName = "id", insertable = false, updatable = false)
        })
        private PersonEntity person;

        @Column(name = "person_id", nullable = false)
        private Long personId;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumns({
                        @JoinColumn(name = "tenant_id", referencedColumnName = "tenant_id", insertable = false, updatable = false),
                        @JoinColumn(name = "attribute_id", referencedColumnName = "id", insertable = false, updatable = false)
        })
        private AttributeEntity attribute;

        @Column(name = "attribute_id", nullable = false)
        private Long attributeId;

        @Column(name = "value", length = 255)
        private String value;

        @Column(name = "valid_from", nullable = false)
        private LocalDateTime validFrom;

        @Column(name = "valid_to")
        private LocalDateTime validTo;

        @Column(name = "is_deleted", nullable = false)
        private boolean deleted;
}
