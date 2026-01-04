// src/main/java/com/saymyname/core/model/auth/EmailVerificationConfirmation.java
package com.saymyname.core.model.auth;

public class EmailVerificationConfirmation {

    private UserEmail email;
    private boolean primaryChanged;

    public EmailVerificationConfirmation() {
    }

    public EmailVerificationConfirmation(UserEmail email, boolean primaryChanged) {
        this.email = email;
        this.primaryChanged = primaryChanged;
    }

    public UserEmail getEmail() {
        return email;
    }

    public boolean isPrimaryChanged() {
        return primaryChanged;
    }

    public void setEmail(UserEmail email) {
        this.email = email;
    }

    public void setPrimaryChanged(boolean primaryChanged) {
        this.primaryChanged = primaryChanged;
    }
}
