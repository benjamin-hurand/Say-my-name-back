package com.saymyname.webapp.controller.challenge;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.enums.ChallengeSortCriterionType;
import com.saymyname.webapp.dto.challenge.ChallengeSortCriterionTypeDto;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/challenges/sort-criterion-type")
public class ChallengeSortCriterionTypeController {

    @GetMapping
    public List<ChallengeSortCriterionTypeDto> getSortCriterionType() {
        return Arrays.stream(ChallengeSortCriterionType.values())
                .map(perf -> new ChallengeSortCriterionTypeDto(perf.name(), perf.getLabel()))
                .toList();
    }
}
