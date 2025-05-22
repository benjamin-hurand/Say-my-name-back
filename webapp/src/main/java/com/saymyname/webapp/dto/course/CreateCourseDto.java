package com.saymyname.webapp.dto.course;

import java.util.List;

public record CreateCourseDto(
                Long userId,
                Long gameModeId,
                Long sortingAttributeId,
                String sortingOrder,
                List<Long> populationIds) {

}
