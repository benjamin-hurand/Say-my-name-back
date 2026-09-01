package com.saymyname.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.enums.EditPolicy;
import com.saymyname.core.model.people.Attribute;
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
}
