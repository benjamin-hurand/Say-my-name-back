package com.oxyl.webapp.dto;

public enum SelectBy {
    AUTO("auto"),
    USER("user"),
    USER_1TAP("user_1tap"),
    USER_2TAP("user_2tap"),
    BTN("btn"),
    BTN_CONFIRM("btn_confirm"),
    BTN_ADD_SESSION("btn_add_session"),
    BTN_CONFIRM_ADD_SESSION("btn_confirm_add_session");

    private final String value;

    SelectBy(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}