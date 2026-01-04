// src/main/java/com/saymyname/core/model/auth/RegisterClassicResult.java
package com.saymyname.core.model.auth;

public class RegisterClassicResult {

    private User user;
    private EmailVerificationChallenge challenge;

    public RegisterClassicResult() {
    }

    public RegisterClassicResult(User user, EmailVerificationChallenge challenge) {
        this.user = user;
        this.challenge = challenge;
    }

    public User getUser() {
        return user;
    }

    public EmailVerificationChallenge getChallenge() {
        return challenge;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setChallenge(EmailVerificationChallenge challenge) {
        this.challenge = challenge;
    }
}
