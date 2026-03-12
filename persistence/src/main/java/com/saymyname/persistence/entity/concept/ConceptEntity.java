package com.saymyname.persistence.entity.concept;

import com.saymyname.core.model.enums.concept.ConceptPortabilityKind;
import com.saymyname.core.model.enums.concept.ConceptValueType;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 50)
    private ConceptValueType valueType;

    @Column(name = "is_derived", nullable = false)
    private boolean derived;

    @Enumerated(EnumType.STRING)
    @Column(name = "portability_kind", nullable = false, length = 20)
    private ConceptPortabilityKind portabilityKind;

    @Column(name = "identity_component_eligible", nullable = false)
    private boolean identityComponentEligible;
}