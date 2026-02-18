package com.saymyname.core.model.auth;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class EmailVerificationConfirmation {
    UserEmail email;
    boolean primaryChanged;
}
