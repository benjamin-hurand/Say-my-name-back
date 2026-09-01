package com.saymyname.core.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

class IdentityResolverTest {

    private final IdentityResolver resolver = new IdentityResolver();

    @Test
    void composesOrderedValues() {
        assertThat(resolver.compose(List.of("Jean", "DUPONT"))).isEqualTo("Jean DUPONT");
    }

    @Test
    void ignoresNullValues() {
        assertThat(resolver.compose(Arrays.asList("Jean", null, "DUPONT"))).isEqualTo("Jean DUPONT");
    }

    @Test
    void trimsAndIgnoresBlankValues() {
        assertThat(resolver.compose(List.of(" ", "DUPONT"))).isEqualTo("DUPONT");
    }

    @Test
    void returnsEmptyIdentityForNoValues() {
        assertThat(resolver.compose(List.of())).isEmpty();
    }
}
