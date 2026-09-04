package com.saymyname.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.saymyname.core.model.people.AttributeEnumOption;
import com.saymyname.core.model.people.GenderOptions;
import com.saymyname.persistence.dao.AttributeEnumOptionDao;

@ExtendWith(MockitoExtension.class)
class AttributeEnumOptionServiceTest {

    @Mock
    private AttributeEnumOptionDao dao;

    @InjectMocks
    private AttributeEnumOptionService service;

    @Test
    void createsSystemOptionsWhenNoneExistYet() {
        when(dao.findAllOptionsByAttributeId(7L)).thenReturn(List.of());

        service.synchronizeSystemOptions(7L, GenderOptions.SYSTEM_OPTIONS);

        ArgumentCaptor<List<AttributeEnumOption>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(dao).saveAll(captor.capture());
        List<AttributeEnumOption> saved = captor.getValue();
        assertThat(saved).hasSize(3);
        assertThat(saved).extracting(AttributeEnumOption::getCode)
                .containsExactly(GenderOptions.MALE, GenderOptions.FEMALE, GenderOptions.OTHER);
        assertThat(saved).allMatch(AttributeEnumOption::isActive);
    }

    @Test
    void reconcilesLegacyCodeEqualsLabelOptionsToStableCodes() {
        // Mirrors the pre-refactor dev-tenant state: code == label, verbatim from the admin.
        AttributeEnumOption legacyHomme = new AttributeEnumOption(3L, 7L, "Homme", "Homme", 0, true);
        AttributeEnumOption legacyFemme = new AttributeEnumOption(4L, 7L, "Femme", "Femme", 1, true);
        AttributeEnumOption legacyAutre = new AttributeEnumOption(5L, 7L, "Autre", "Autre", 2, true);
        when(dao.findAllOptionsByAttributeId(7L)).thenReturn(
                new java.util.ArrayList<>(List.of(legacyHomme, legacyFemme, legacyAutre)));

        service.synchronizeSystemOptions(7L, GenderOptions.SYSTEM_OPTIONS);

        // The legacy rows have no code matching MALE/FEMALE/OTHER, so they are all
        // deactivated and three brand-new system rows are created instead.
        assertThat(legacyHomme.isActive()).isFalse();
        assertThat(legacyFemme.isActive()).isFalse();
        assertThat(legacyAutre.isActive()).isFalse();

        ArgumentCaptor<List<AttributeEnumOption>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(dao).saveAll(captor.capture());
        List<AttributeEnumOption> saved = captor.getValue();
        assertThat(saved).filteredOn(AttributeEnumOption::isActive)
                .extracting(AttributeEnumOption::getCode)
                .containsExactlyInAnyOrder(GenderOptions.MALE, GenderOptions.FEMALE, GenderOptions.OTHER);
    }

    @Test
    void reactivatesExistingSystemOptionsByCodeWithoutDuplicating() {
        AttributeEnumOption existingMale = new AttributeEnumOption(1L, 7L, GenderOptions.MALE, "Homme (old label)", 0,
                false);
        when(dao.findAllOptionsByAttributeId(7L)).thenReturn(new java.util.ArrayList<>(List.of(existingMale)));

        service.synchronizeSystemOptions(7L, GenderOptions.SYSTEM_OPTIONS);

        assertThat(existingMale.isActive()).isTrue();
        assertThat(existingMale.getLabel()).isEqualTo("Homme");

        ArgumentCaptor<List<AttributeEnumOption>> captor = ArgumentCaptor.forClass(List.class);
        org.mockito.Mockito.verify(dao).saveAll(captor.capture());
        // Existing MALE row reused (not duplicated) + FEMALE/OTHER created.
        assertThat(captor.getValue()).hasSize(3);
    }
}
