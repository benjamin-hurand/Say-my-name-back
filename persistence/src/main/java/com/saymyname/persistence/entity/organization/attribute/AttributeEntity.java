package com.saymyname.persistence.entity.organization.attribute;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.persistence.entity.concept.ConceptEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "attributes", uniqueConstraints = {
                @UniqueConstraint(name = "uq_tenant_attr_name", columnNames = { "tenant_id", "attribute_name" }),
                @UniqueConstraint(name = "uq_attributes_tenant_id", columnNames = { "tenant_id", "id" })
}, indexes = {
                @Index(name = "idx_attr_tenant", columnList = "tenant_id"),
                @Index(name = "idx_attr_tenant_concept", columnList = "tenant_id,concept_id")
})
public class AttributeEntity extends BaseTenantScoped {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "concept_id", foreignKey = @ForeignKey(name = "fk_attributes_concept"))
        private ConceptEntity concept;

        @Column(name = "attribute_name", nullable = false, length = 255)
        private String attributeName;

        @Column(name = "display_order", nullable = false)
        private int displayOrder;

        /**
         * Policy locale d'identité actuelle.
         * À sortir plus tard vers une vraie config tenant-scoped d'identité.
         */
        @Column(name = "is_identity_source", nullable = false)
        private boolean identitySource;

        @Column(name = "is_category", nullable = false)
        private boolean category;

        @Column(name = "max_values", nullable = false)
        private int maxValues;

        @Column(name = "filter", nullable = false)
        private boolean filter;

        @Column(name = "sort", nullable = false)
        private boolean sort;

        @Column(name = "required", nullable = false)
        private boolean required;

        /**
         * Type local détaillé, plus fin que concept.valueType.
         * Exemples : EMAIL, URL, DATE.
         */
        @Enumerated(EnumType.STRING)
        @Column(name = "type", nullable = false, length = 50)
        private ValueType type;

        @Enumerated(EnumType.STRING)
        @Column(name = "edit_policy", nullable = false, length = 20)
        private EditPolicy editPolicy;

        @Enumerated(EnumType.STRING)
        @Column(name = "casing_strategy", nullable = false, length = 32)
        private CasingStrategy casingStrategy;

        @Enumerated(EnumType.STRING)
        @Column(name = "constraint_kind", nullable = false, length = 16)
        private ConstraintKind constraintKind;

        @Column(name = "constraint_payload", columnDefinition = "json")
        private String constraintPayload;
}