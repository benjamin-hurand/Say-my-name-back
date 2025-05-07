package com.saymyname.core.exception;

public class ChallengeAttemptNotFoundException extends ChallengeAttemptException {
    public ChallengeAttemptNotFoundException(Long attemptId) {
        super("Attempt not found: " + attemptId);
    }
}
