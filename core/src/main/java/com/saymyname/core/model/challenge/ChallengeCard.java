package com.saymyname.core.model.challenge;

import java.util.Objects;

public class ChallengeCard {
    private Challenge challenge;
    private ChallengeVersion challengeVersion;
    private ChallengeAttempt challengeAttempt; // Peut être null si l'utilisateur n'a pas encore participé

    public ChallengeCard() {}

    public ChallengeCard(Challenge challenge, ChallengeVersion challengeVersion, ChallengeAttempt challengeAttempt) {
        this.challenge = challenge;
        this.challengeVersion = challengeVersion;
        this.challengeAttempt = challengeAttempt;
    }

    public Challenge getChallenge() {
        return challenge;
    }

    public void setChallenge(Challenge challenge) {
        this.challenge = challenge;
    }

    public ChallengeVersion getChallengeVersion() {
        return challengeVersion;
    }

    public void setChallengeVersion(ChallengeVersion challengeVersion) {
        this.challengeVersion = challengeVersion;
    }

    public ChallengeAttempt getChallengeAttempt() {
        return challengeAttempt;
    }

    public void setChallengeAttempt(ChallengeAttempt challengeAttempt) {
        this.challengeAttempt = challengeAttempt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChallengeCard)) return false;
        ChallengeCard that = (ChallengeCard) o;
        return Objects.equals(challenge, that.challenge) &&
               Objects.equals(challengeVersion, that.challengeVersion) &&
               Objects.equals(challengeAttempt, that.challengeAttempt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(challenge, challengeVersion, challengeAttempt);
    }

    @Override
    public String toString() {
        return "ChallengeCard{" +
               "challenge=" + challenge +
               ", challengeVersion=" + challengeVersion +
               ", challengeAttempt=" + challengeAttempt +
               '}';
    }
}
