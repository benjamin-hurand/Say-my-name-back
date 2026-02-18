package com.saymyname.persistence.entity.organization.attribute;


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

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "attribute_enum_options", uniqueConstraints = {
        @UniqueConstraint(name = "uq_oa_enum_code", columnNames = {"attribute_id", "code"}),
        @UniqueConstraint(name = "uq_enum_tenant_attr_code", columnNames = {"tenant_id", "attribute_id", "code"})
}, indexes = {
        @Index(name = "idx_oa_enum_attr", columnList = "attribute_id"),
        @Index(name = "idx_enum_tenant_attr", columnList = "tenant_id,attribute_id")
})
public class AttributeEnumOptionEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "order_index", nullable = false)
    private int orderIndex;

    @Column(name = "active", nullable = false)
    private boolean active;
}