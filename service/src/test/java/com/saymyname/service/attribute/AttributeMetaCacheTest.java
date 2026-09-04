package com.saymyname.service.attribute;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.saymyname.core.multitenancy.TenantContext;
import com.saymyname.persistence.dao.AttributeDao;
import com.saymyname.persistence.repository.AttributeRepository.AttributeMetaRow;

@ExtendWith(MockitoExtension.class)
class AttributeMetaCacheTest {

    @Mock
    private AttributeDao attributeDao;

    private AttributeMetaCache cache;

    private static final Long TENANT_ID = 10L;

    @AfterEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    @Test
    void composesFirstNameBeforeLastNameEvenWhenDisplayOrderIsReversed() {
        // MVP: composition order is semantic, never driven by displayOrder.
        TenantContext.set(TENANT_ID);
        cache = new AttributeMetaCache(attributeDao);
        when(attributeDao.findMetaByTenantId(TENANT_ID)).thenReturn(List.of(
                row(1L, true, /* displayOrder */ 20, "LAST_NAME"),
                row(2L, true, /* displayOrder */ 10, "FIRST_NAME")));

        assertThat(cache.getIdentitySourceAttributeIds()).containsExactly(2L, 1L);
    }

    @Test
    void includesFirstNameEvenWhenLegacyIdentitySourceFlagIsFalse() {
        // MVP: identitySource is no longer an admin decision — a stale/legacy
        // false value on a FIRST_NAME attribute must not exclude it.
        TenantContext.set(TENANT_ID);
        cache = new AttributeMetaCache(attributeDao);
        when(attributeDao.findMetaByTenantId(TENANT_ID)).thenReturn(List.of(
                row(1L, false, 10, "FIRST_NAME")));

        assertThat(cache.getIdentitySourceAttributeIds()).containsExactly(1L);
    }

    @Test
    void excludesNonEligibleConceptsEvenWhenLegacyIdentitySourceFlagIsTrue() {
        // MVP: custom attributes and GENDER can never compose IDENTITY, regardless
        // of a stale identitySource=true left over from the old admin-driven rule.
        TenantContext.set(TENANT_ID);
        cache = new AttributeMetaCache(attributeDao);
        when(attributeDao.findMetaByTenantId(TENANT_ID)).thenReturn(List.of(
                row(1L, true, 10, "FIRST_NAME"),
                row(2L, true, 20, "GENDER"),
                row(3L, true, 30, null)));

        assertThat(cache.getIdentitySourceAttributeIds()).containsExactly(1L);
    }

    @Test
    void resolvesGenderAttributeIdWhenConfiguredForTenant() {
        TenantContext.set(TENANT_ID);
        cache = new AttributeMetaCache(attributeDao);
        when(attributeDao.findMetaByTenantId(TENANT_ID)).thenReturn(List.of(
                row(1L, true, 10, "FIRST_NAME"),
                row(2L, false, 20, "GENDER")));

        assertThat(cache.getGenderAttributeId()).isEqualTo(2L);
    }

    @Test
    void returnsNullGenderAttributeIdWhenTenantHasNoGenderConcept() {
        TenantContext.set(TENANT_ID);
        cache = new AttributeMetaCache(attributeDao);
        when(attributeDao.findMetaByTenantId(TENANT_ID)).thenReturn(List.of(
                row(1L, true, 10, "FIRST_NAME"),
                row(3L, false, 30, null)));

        assertThat(cache.getGenderAttributeId()).isNull();
    }

    private static AttributeMetaRow row(Long id, boolean identitySource, int displayOrder, String conceptCode) {
        return new AttributeMetaRow() {
            @Override
            public Long getId() {
                return id;
            }

            @Override
            public boolean getIdentitySource() {
                return identitySource;
            }

            @Override
            public int getDisplayOrder() {
                return displayOrder;
            }

            @Override
            public String getConceptCode() {
                return conceptCode;
            }
        };
    }
}
