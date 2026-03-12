package com.saymyname.webapp.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.saymyname.core.model.enums.CasingStrategy;
import com.saymyname.core.model.enums.ConstraintKind;
import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.enums.concept.ConceptPortabilityKind;
import com.saymyname.core.model.enums.concept.ConceptValueType;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.AttributeType;
import com.saymyname.core.model.people.ObservedMinMax;
import com.saymyname.core.model.rules.EnumRules;
import com.saymyname.core.model.rules.RangeRules;
import com.saymyname.core.model.rules.RegexRules;
import com.saymyname.core.model.rules.SetRules;
import com.saymyname.webapp.dto.AttributeDto;
import com.saymyname.webapp.dto.AttributeEnumOptionDto;
import com.saymyname.webapp.dto.AttributeStatsDto;
import com.saymyname.webapp.dto.ReducedAttributeDto;
import com.saymyname.webapp.dto.constraint.ConstraintDto;
import com.saymyname.webapp.dto.constraint.EnumConstraintDto;
import com.saymyname.webapp.dto.constraint.RangeConstraintDto;
import com.saymyname.webapp.dto.constraint.RegexConstraintDto;
import com.saymyname.webapp.dto.constraint.SetConstraintDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class AttributeDtoMapper {

    private static final Logger log = LoggerFactory.getLogger(AttributeDtoMapper.class);
    private static final ObjectMapper OM = new ObjectMapper();

    public AttributeDto toDto(final Attribute m) {
        return toDto(m, null, null);
    }

    public AttributeDto toDto(final Attribute m,
            final AttributeStatsDto stats,
            final List<AttributeEnumOptionDto> options) {
        ConstraintDto constraint = buildConstraintDto(m.getConstraintKind(), m.getConstraintPayload());
        return new AttributeDto(
                m.getId(),

                m.getConceptId(),
                m.getConceptCode(),
                m.getConceptValueType() != null ? m.getConceptValueType().name() : null,
                m.getConceptDerived(),
                m.getConceptPortabilityKind() != null ? m.getConceptPortabilityKind().name() : null,
                m.getIdentityComponentEligible(),

                m.getName(),

                m.getDisplayOrder(),
                m.isIdentitySource(),
                m.isCategory(),

                m.getMaxValues(),
                m.isFilter(),
                m.isSort(),
                m.isInitializable(),
                m.isRequired(),

                m.getType() != null ? m.getType().name() : null,
                m.getEditPolicy() != null ? m.getEditPolicy().name() : "FREE",
                m.getCasingStrategy() != null ? m.getCasingStrategy().name() : null,

                m.getConstraintKind() != null ? m.getConstraintKind().name() : "NONE",
                m.getConstraintPayload(),
                constraint,

                stats,
                options);
    }

    private ConstraintDto buildConstraintDto(ConstraintKind kind, Map<String, Object> payload) {
        if (kind == null || kind == ConstraintKind.NONE) {
            return new ConstraintDto("NONE", null, null, null, null);
        }
        try {
            return switch (kind) {
                case RANGE -> {
                    RangeRules r = OM.convertValue(payload, RangeRules.class);
                    yield new ConstraintDto(
                            "RANGE",
                            new RangeConstraintDto(r.min, r.max, r.inclusive, r.step),
                            null, null, null);
                }
                case REGEX -> {
                    RegexRules r = OM.convertValue(payload, RegexRules.class);
                    yield new ConstraintDto(
                            "REGEX",
                            null,
                            new RegexConstraintDto(r.pattern, r.minLength, r.maxLength, r.caseInsensitive),
                            null, null);
                }
                case SET -> {
                    SetRules r = OM.convertValue(payload, SetRules.class);
                    yield new ConstraintDto(
                            "SET",
                            null, null,
                            new SetConstraintDto(r.values, r.strict),
                            null);
                }
                case ENUM -> {
                    EnumRules r = OM.convertValue(payload, EnumRules.class);
                    yield new ConstraintDto(
                            "ENUM",
                            null, null, null,
                            new EnumConstraintDto(r.allowInactive, r.storeCode));
                }
                default -> new ConstraintDto("NONE", null, null, null, null);
            };
        } catch (IllegalArgumentException ex) {
            log.warn("Invalid constraint payload for kind {}. Returning minimal ConstraintDto. Details: {}", kind,
                    ex.getMessage(), ex);
            return new ConstraintDto(kind.name(), null, null, null, null);
        }
    }

    public Attribute toModel(final AttributeDto dto) {
        final AttributeType at = dto.type() != null ? AttributeType.valueOf(dto.type()) : AttributeType.TEXT;
        final EditPolicy policy = dto.editPolicy() != null ? EditPolicy.valueOf(dto.editPolicy()) : EditPolicy.FREE;
        final ConstraintKind ck = dto.constraintKind() != null
                ? ConstraintKind.valueOf(dto.constraintKind())
                : ConstraintKind.NONE;

        final CasingStrategy cs = dto.casingStrategy() != null
                ? CasingStrategy.valueOf(dto.casingStrategy())
                : (at == AttributeType.TEXT ? CasingStrategy.TITLE_CASE : CasingStrategy.NONE);

        return new Attribute.Builder()
                .withId(dto.id())
                .withConceptId(dto.conceptId())
                .withConceptCode(dto.conceptCode())
                .withConceptValueType(
                        dto.conceptValueType() != null ? ConceptValueType.valueOf(dto.conceptValueType()) : null)
                .withConceptDerived(dto.conceptDerived())
                .withConceptPortabilityKind(dto.conceptPortabilityKind() != null
                        ? ConceptPortabilityKind.valueOf(dto.conceptPortabilityKind())
                        : null)
                .withIdentityComponentEligible(dto.identityComponentEligible())

                .withName(dto.name())
                .withDisplayOrder(dto.displayOrder() != null ? dto.displayOrder() : 100)
                .withIdentitySource(Boolean.TRUE.equals(dto.identitySource()))
                .withCategory(Boolean.TRUE.equals(dto.category()))
                .withMaxValues(dto.maxValues() != null ? dto.maxValues() : 1)
                .withFilter(Boolean.TRUE.equals(dto.filter()))
                .withSort(Boolean.TRUE.equals(dto.sort()))
                .withInitializable(Boolean.TRUE.equals(dto.initializable()))
                .withRequired(Boolean.TRUE.equals(dto.required()))
                .withType(at)
                .withEditPolicy(policy)
                .withCasingStrategy(cs)
                .withConstraintKind(ck)
                .withConstraintPayload(dto.constraintPayload())
                .build();
    }

    public ReducedAttributeDto toReducedDto(final Attribute model) {
        return new ReducedAttributeDto(model.getId(), model.getName());
    }

    public AttributeStatsDto toStats(ObservedMinMax mm) {
        return (mm == null) ? null : new AttributeStatsDto(mm.min(), mm.max());
    }
}