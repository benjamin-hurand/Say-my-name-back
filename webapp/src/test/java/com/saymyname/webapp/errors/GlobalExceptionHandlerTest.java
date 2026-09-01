package com.saymyname.webapp.errors;

import java.sql.SQLException;

import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void mapsAttributeTenantConceptConstraintToConflictWithBusinessMessage() {
        var sqlException = new SQLException("Duplicate entry");
        var constraintViolation = new org.hibernate.exception.ConstraintViolationException(
                "could not execute statement",
                sqlException,
                "uq_attributes_tenant_concept");
        var exception = new DataIntegrityViolationException("insert failed", constraintViolation);

        var problem = handler.handleDataIntegrityViolation(exception, request());

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getTitle()).isEqualTo("Conflict");
        assertThat(problem.getDetail())
                .isEqualTo("Ce concept est déjà utilisé par un attribut de ce tenant");
        assertThat(problem.getProperties())
                .containsEntry("path", "/api/admin/attributes");
    }

    @Test
    void doesNotMisclassifyOtherIntegrityViolationsAsConceptConflicts() {
        var exception = new DataIntegrityViolationException(
                "could not execute constraint uq_tenant_attr_name",
                new SQLException("raw SQL detail"));

        var problem = handler.handleDataIntegrityViolation(exception, request());

        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getDetail())
                .isEqualTo("Une erreur interne est survenue. Réessayez plus tard.")
                .doesNotContain("uq_tenant_attr_name", "raw SQL");
    }

    private MockHttpServletRequest request() {
        var request = new MockHttpServletRequest("POST", "/api/admin/attributes");
        request.setRequestURI("/api/admin/attributes");
        return request;
    }
}
