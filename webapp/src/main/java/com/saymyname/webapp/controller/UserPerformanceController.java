package com.saymyname.webapp.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.enums.UserPerformance;
import com.saymyname.webapp.dto.UserPerformanceDto;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/user-performances")
public class UserPerformanceController {

    @GetMapping
    public List<UserPerformanceDto> getUserPerformances() {
        return Arrays.stream(UserPerformance.values())
                .map(perf -> new UserPerformanceDto(perf.name(), perf.getLabel()))
                .toList();
    }
}
