package com.saymyname.security.util;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class PasswordGeneratorTest {

    @Test
    void generatePassword_returnsNonNullNonEmpty() {
        String password = PasswordGenerator.generatePassword();

        assertThat(password).isNotNull();
        assertThat(password).isNotEmpty();
    }

    @Test
    void generatePassword_hasExpectedLength() {
        String password = PasswordGenerator.generatePassword();

        // 24 bytes encoded in Base64 URL-safe = 32 characters
        assertThat(password).hasSizeGreaterThanOrEqualTo(30);
    }

    @Test
    void generatePassword_isUrlSafe() {
        String password = PasswordGenerator.generatePassword();

        // URL-safe Base64 should not contain + or /
        assertThat(password).doesNotContain("+", "/");
    }

    @Test
    void generatePassword_generatesDifferentPasswords() {
        Set<String> passwords = new HashSet<>();

        // Generate 100 passwords and verify they're all unique
        for (int i = 0; i < 100; i++) {
            passwords.add(PasswordGenerator.generatePassword());
        }

        assertThat(passwords).hasSize(100);
    }

    @Test
    void generatePassword_containsValidBase64Characters() {
        String password = PasswordGenerator.generatePassword();

        // Base64 URL-safe alphabet: A-Z, a-z, 0-9, -, _
        assertThat(password).matches("[A-Za-z0-9\\-_=]*");
    }
}
