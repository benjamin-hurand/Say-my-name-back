// (rappel) src/main/java/com/saymyname/webapp/dto/SubmitChangeRequestRequest.java
package com.saymyname.webapp.dto.changerequest;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubmitChangeRequestDto(
                @NotNull Long personId,
                @NotNull Long attributeId,
                @NotBlank String requestReason,
                @Size(min = 1) List<SubmitChangeRequestItemDto> items) {
}
