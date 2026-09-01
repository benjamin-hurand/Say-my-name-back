package com.saymyname.webapp.mapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.saymyname.core.model.people.Attribute;
import com.saymyname.core.model.people.Fact;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.persondirectory.AttributeValueView;
import com.saymyname.core.model.persondirectory.PersonCard;
import com.saymyname.service.attribute.AttributeMetaCache;
import com.saymyname.service.photo.PhotoUrlResolver;
import com.saymyname.webapp.mapper.person.PersonDirectoryDtoMapper;

@ExtendWith(MockitoExtension.class)
class IdentityDisplayNameMapperTest {

    @Mock
    private FactDtoMapper factDtoMapper;
    @Mock
    private PhotoDtoMapper photoDtoMapper;
    @Mock
    private PhotoUrlResolver photoUrlResolver;
    @Mock
    private AttributeMetaCache attributeMetaCache;

    @Test
    void personMappersExposeTheSameMaterializedIdentity() {
        Long identityAttributeId = 13L;
        when(attributeMetaCache.getIdentityAttributeId()).thenReturn(identityAttributeId);

        Attribute identityAttribute = new Attribute.Builder().withId(identityAttributeId).build();
        Fact identityFact = new Fact.Builder()
                .withId(30L)
                .withAttribute(identityAttribute)
                .withValue("Jean DUPONT")
                .withValidFrom(LocalDateTime.now().minusDays(1))
                .build();
        Person person = new Person.Builder()
                .withId(7L)
                .withFacts(List.of(identityFact))
                .build();

        PersonDtoMapper personMapper = new PersonDtoMapper(
                factDtoMapper, photoDtoMapper, photoUrlResolver, attributeMetaCache);
        PersonDirectoryDtoMapper directoryMapper = new PersonDirectoryDtoMapper(photoUrlResolver);
        PersonCard card = new PersonCard.Builder()
                .withIdPerson(7L)
                .withDisplayName("Jean DUPONT")
                .withPrimaryAttributes(List.of(
                        new AttributeValueView.Builder().withValue("Other source values").build()))
                .build();

        assertThat(personMapper.toSummaryDto(person).displayName())
                .isEqualTo(directoryMapper.toDto(card).displayName())
                .isEqualTo("Jean DUPONT");
    }
}
