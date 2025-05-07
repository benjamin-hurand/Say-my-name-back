package com.saymyname.core.exception;

public class ChallengeAttemptAlreadyStartedException extends ChallengeAttemptException {
    public ChallengeAttemptAlreadyStartedException(Long attemptId) {
        super("Attempt already started: " + attemptId);
    }
}
