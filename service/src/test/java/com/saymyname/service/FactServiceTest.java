package com.saymyname.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.people.GenderOptions;
import com.saymyname.core.model.people.ValueType;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.dao.FactDao;
import com.saymyname.service.identity.IdentityService;

@ExtendWith(MockitoExtension.class)
class FactServiceTest {

    @Mock
    private FactDao factDao;
    @Mock
    private AttributeDao attributeDao;
    @Mock
    private IdentityService identityService;
    @Mock
    private AttributeEnumOptionService attributeEnumOptionService;
    @InjectMocks
    private FactService service;

    @Test
    void rejectsExternalWriteOnDerivedAttributeEvenWithRestrictedBypass() {
        Attribute identity = new Attribute.Builder()
                .withId(13L)
                .withType(ValueType.TEXT)
                .withMaxValues(1)
                .withEditPolicy(EditPolicy.DERIVED)
                .build();
        when(attributeDao.findById(13L)).thenReturn(Optional.of(identity));

        assertThatThrownBy(() -> service.applyChangesForPerson(
                7L, 13L, List.of(), List.of(), List.of(), true))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> org.assertj.core.api.Assertions.assertThat(
                        ((ResponseStatusException) error).getStatusCode().value()).isEqualTo(403));

        InOrder calls = inOrder(factDao, attributeDao);
        calls.verify(factDao).lockPersonForUpdate(7L);
        calls.verify(attributeDao).findById(13L);
    }

    @Test
    void acceptsEachActiveGenderCodeOnCreate() {
        Attribute gender = genderAttribute(7L);
        when(attributeDao.findById(7L)).thenReturn(Optional.of(gender));
        when(attributeEnumOptionService.getActiveCodesByAttributeId(7L))
                .thenReturn(Set.of(GenderOptions.MALE, GenderOptions.FEMALE, GenderOptions.OTHER));
        when(factDao.findActiveAtByPersonAndAttribute(org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(7L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(List.of());

        for (String code : List.of(GenderOptions.MALE, GenderOptions.FEMALE, GenderOptions.OTHER)) {
            Fact toCreate = new Fact.Builder().withValue(code).build();
            assertThatCode(() -> service.applyChangesForPerson(
                    1L, 7L, List.of(toCreate), List.of(), List.of(), true))
                    .doesNotThrowAnyException();
        }
    }

    @Test
    void rejectsArbitraryValueForGenderFact() {
        Attribute gender = genderAttribute(7L);
        when(attributeDao.findById(7L)).thenReturn(Optional.of(gender));
        when(attributeEnumOptionService.getActiveCodesByAttributeId(7L))
                .thenReturn(Set.of(GenderOptions.MALE, GenderOptions.FEMALE, GenderOptions.OTHER));

        Fact toCreate = new Fact.Builder().withValue("Homme").build();

        assertThatThrownBy(() -> service.applyChangesForPerson(
                1L, 7L, List.of(toCreate), List.of(), List.of(), true))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(error -> assertThat(((ResponseStatusException) error).getStatusCode().value())
                        .isEqualTo(400));
    }

    private static Attribute genderAttribute(Long id) {
        return new Attribute.Builder()
                .withId(id)
                .withType(ValueType.ENUM)
                .withMaxValues(1)
                .withEditPolicy(EditPolicy.FREE)
                .build();
    }
}
