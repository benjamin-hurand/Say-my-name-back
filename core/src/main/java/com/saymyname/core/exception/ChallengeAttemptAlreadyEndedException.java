// src/main/java/com/saymyname/core/exception/ChallengeAttemptAlreadyEndedException.java
package com.saymyname.core.exception;

public class ChallengeAttemptAlreadyEndedException extends ChallengeAttemptException {
    public ChallengeAttemptAlreadyEndedException(Long attemptId) {
        super("Attempt already ended: " + attemptId);
    }
}
