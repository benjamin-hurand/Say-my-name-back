package com.saymyname.webapp.dto;

public record AttributeDto(
                Long id,
                String name,
                Integer maxValues, // anciennement Boolean unique
                Boolean filter,
                Boolean sort,
                Boolean initializable,
                Boolean required,
                String type, // ex: "TEXT", "DATE"…
                String minValue,
                String maxValue,
                String editPolicy // "FREE" | "RESTRICTED"
) {
}
