package com.saymyname.persistence.entity;

import com.saymyname.core.model.enums.AuthProvider;
import com.saymyname.core.model.enums.SrsAlgorithm;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Sanity tests for JPA entities to ensure basic construction and field access work.
 * These tests verify that entities can be instantiated and their fields are accessible.
 */
class EntitySanityTest {

    @Test
    void userEntity_canBeInstantiatedAndFieldsSet() {
        UserEntity user = new UserEntity();
        UUID publicId = UUID.randomUUID();
        String displayName = "testuser";

        user.setPublicId(publicId);
        user.setDisplayName(displayName);
        user.setActive(true);
        user.setRoles("ROLE_USER");
        user.setSrsAlgorithm(SrsAlgorithm.SM2);

        assertThat(user.getPublicId()).isEqualTo(publicId);
        assertThat(user.getDisplayName()).isEqualTo(displayName);
        assertThat(user.getActive()).isTrue();
        assertThat(user.getRoles()).isEqualTo("ROLE_USER");
        assertThat(user.getSrsAlgorithm()).isEqualTo(SrsAlgorithm.SM2);
    }

    @Test
    void userIdentityEntity_canBeInstantiatedAndFieldsSet() {
        UserIdentityEntity identity = new UserIdentityEntity();
        String providerSubject = "google-12345";

        identity.setProvider(AuthProvider.GOOGLE);
        identity.setProviderSubject(providerSubject);
        identity.setEnabled(true);

        assertThat(identity.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(identity.getProviderSubject()).isEqualTo(providerSubject);
        assertThat(identity.isEnabled()).isTrue();
    }

    @Test
    void userIdentityEntity_isLocalMethod() {
        UserIdentityEntity localIdentity = new UserIdentityEntity();
        localIdentity.setProvider(AuthProvider.LOCAL);

        UserIdentityEntity googleIdentity = new UserIdentityEntity();
        googleIdentity.setProvider(AuthProvider.GOOGLE);

        assertThat(localIdentity.isLocal()).isTrue();
        assertThat(googleIdentity.isLocal()).isFalse();
    }

    @Test
    void passwordResetTokenEntity_canBeInstantiatedAndFieldsSet() {
        PasswordResetTokenEntity token = new PasswordResetTokenEntity();
        Long userId = 789L;
        String tokenHash = "hashed-token-value";
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(1);

        token.setUserId(userId);
        token.setTokenHash(tokenHash);
        token.setExpiresAt(expiresAt);

        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getTokenHash()).isEqualTo(tokenHash);
        assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(token.getUsedAt()).isNull();
    }

    @Test
    void passwordResetTokenEntity_constructorWorks() {
        Long userId = 123L;
        String tokenHash = "hash123";
        OffsetDateTime expiresAt = OffsetDateTime.now().plusHours(2);
        OffsetDateTime usedAt = OffsetDateTime.now();

        PasswordResetTokenEntity token = new PasswordResetTokenEntity(
                null, userId, tokenHash, expiresAt, usedAt, "127.0.0.1", "Mozilla");

        assertThat(token.getUserId()).isEqualTo(userId);
        assertThat(token.getTokenHash()).isEqualTo(tokenHash);
        assertThat(token.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(token.getUsedAt()).isEqualTo(usedAt);
        assertThat(token.getCreatedIp()).isEqualTo("127.0.0.1");
        assertThat(token.getUserAgent()).isEqualTo("Mozilla");
    }
}
