package com.oxyl.core.model;

public enum GameType {
    RANKED("ranked"),
    CASUAL("casual"),
    TRAINING("training");

    private final String name;

    // Constructor
    GameType(String name) {
        this.name = name;
    }

    // Getter method
    public String getName() {
        return name;
    }
}

