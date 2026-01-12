package com.saymyname.service.config;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Base class for unit tests in the service layer.
 * Uses Mockito for mocking dependencies.
 *
 * Usage: Extend this class for service unit tests that don't require Spring context.
 */
@ExtendWith(MockitoExtension.class)
public abstract class BaseServiceTest {

    // Add common test setup methods here if needed

}
