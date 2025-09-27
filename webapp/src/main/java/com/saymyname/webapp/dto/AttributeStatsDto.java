// src/main/java/com/saymyname/webapp/dto/AttributeStatsDto.java
package com.saymyname.webapp.dto;

/** Stats optionnelles pour sliders (observées sur les données). */
public record AttributeStatsDto(
        String observedMin,
        String observedMax) {
}
