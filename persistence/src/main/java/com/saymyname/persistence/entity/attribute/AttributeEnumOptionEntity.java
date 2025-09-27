// src/main/java/com/saymyname/persistence/entity/attribute/AttributeEnumOptionEntity.java
package com.saymyname.persistence.entity.attribute;

import jakarta.persistence.*;

@Entity
@Table(name = "attribute_enum_option", uniqueConstraints = @UniqueConstraint(name = "uq_attr_enum_code", columnNames = {
        "attribute_id", "code" }))
public class AttributeEnumOptionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK -> attributes.id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "attribute_id", nullable = false, foreignKey = @ForeignKey(name = "fk_attr_enum_option_attr"))
    private AttributeEntity attribute;

    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "label", nullable = false, length = 255)
    private String label;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 100;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    // Getters/Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AttributeEntity getAttribute() {
        return attribute;
    }

    public void setAttribute(AttributeEntity attribute) {
        this.attribute = attribute;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
