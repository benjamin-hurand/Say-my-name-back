package com.saymyname.persistence.entity.concept;

import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.concept.ConceptPortabilityKind;
import com.saymyname.core.model.people.ValueType;

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
@Table(name = "concepts", uniqueConstraints = {
        @UniqueConstraint(name = "uq_concepts_code", columnNames = "code")
})
public class ConceptEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ToString.Include
    @Column(name = "code", nullable = false, length = 64)
    private String code;

    @Column(name = "icon_key", length = 64)
    private String iconKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 50)
    private ValueType valueType;

    @Column(name = "is_derived", nullable = false)
    private boolean derived;

    @Enumerated(EnumType.STRING)
    @Column(name = "portability_kind", nullable = false, length = 20)
    private ConceptPortabilityKind portabilityKind;

    @Column(name = "identity_component_eligible", nullable = false)
    private boolean identityComponentEligible;

    @Column(name = "required_max_values")
    private Integer requiredMaxValues;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_casing_strategy", length = 32)
    private CasingStrategy defaultCasingStrategy;
}
