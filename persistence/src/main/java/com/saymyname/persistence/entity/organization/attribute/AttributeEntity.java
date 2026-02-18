package com.saymyname.persistence.entity.organization.attribute;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "attributes", uniqueConstraints = {
        @UniqueConstraint(name = "uq_tenant_attr_name", columnNames = {"tenant_id", "attribute_name"}),
        @UniqueConstraint(name = "uq_attributes_tenant_id", columnNames = {"tenant_id", "id"})
}, indexes = {
        @Index(name = "idx_attr_tenant", columnList = "tenant_id")
})
public class AttributeEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "attribute_name", nullable = false, length = 255)
    private String attributeName;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "primary_field", nullable = false)
    private boolean primaryField;

    @Column(name = "is_category", nullable = false)
    private boolean category;

    @Column(name = "max_values", nullable = false)
    private int maxValues;

    @Column(name = "filter", nullable = false)
    private boolean filter;

    @Column(name = "sort", nullable = false)
    private boolean sort;

    @Column(name = "initializable", nullable = false)
    private boolean initializable;

    @Column(name = "required", nullable = false)
    private boolean required;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

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
