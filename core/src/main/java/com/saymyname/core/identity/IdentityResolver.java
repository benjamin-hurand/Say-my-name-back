package com.saymyname.core.identity;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;

@Component
public class IdentityResolver {

    public String compose(List<String> sourceValues) {
        if (sourceValues == null || sourceValues.isEmpty()) {
            return "";
        }

        return sourceValues.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .reduce((left, right) -> left + " " + right)
                .orElse("");
    }
}
