package com.saymyname.core.util;

import com.saymyname.core.model.people.Fact;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class InitialCrafter {

    public String computeInitials(List<Fact> attributes, List<Long> attributeIds, String operator) {
        if (attributes == null || attributes.isEmpty() || attributeIds == null || attributeIds.isEmpty()) {
            return "";
        }

        List<Fact> filteredAttributes = attributes.stream()
                .filter(attr -> attr.getAttributeId() != null && attributeIds.contains(attr.getAttributeId()))
                .toList();

        String outerDelimiter;
        if ("AND".equalsIgnoreCase(operator)) {
            outerDelimiter = ".";
        } else if ("OR".equalsIgnoreCase(operator)) {
            outerDelimiter = " / ";
        } else {
            outerDelimiter = "";
        }

        java.util.function.Function<String, String> extractLocalInitials = value -> {
            if (value == null || value.isEmpty()) {
                return "";
            }
            String[] words = value.trim().split("\\s+");
            List<String> wordInitials = new ArrayList<>();
            for (String word : words) {
                if (word.contains("-")) {
                    String[] subWords = word.split("-");
                    String subInitials = Arrays.stream(subWords)
                            .filter(sw -> !sw.isEmpty())
                            .map(sw -> sw.substring(0, 1).toUpperCase())
                            .collect(Collectors.joining("-"));
                    wordInitials.add(subInitials);
                } else {
                    wordInitials.add(word.substring(0, 1).toUpperCase());
                }
            }
            String localInitials = String.join(".", wordInitials);
            if ("OR".equalsIgnoreCase(operator)) {
                localInitials += ".";
            }
            return localInitials;
        };

        List<String> initialsList = filteredAttributes.stream()
                .map(Fact::getValue)
                .map(extractLocalInitials)
                .filter(initial -> !initial.isEmpty())
                .toList();

        if ("AND".equalsIgnoreCase(operator)) {
            return String.join(outerDelimiter, initialsList) + ".";
        }
        if ("OR".equalsIgnoreCase(operator)) {
            return String.join(outerDelimiter, initialsList);
        }
        return String.join("", initialsList);
    }
}
