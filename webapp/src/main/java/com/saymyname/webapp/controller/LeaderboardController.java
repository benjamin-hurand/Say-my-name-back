// src/main/java/com/saymyname/webapp/controller/LeaderboardController.java
package com.saymyname.webapp.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.leaderboard.LeaderboardEntry;
import com.saymyname.core.model.leaderboard.XpEventHistoryItem;
import com.saymyname.service.UserService;
import com.saymyname.service.leaderboard.LeaderboardService;
import com.saymyname.service.leaderboard.XpHistoryService;
import com.saymyname.webapp.dto.leaderboard.LeaderboardEntryDto;
import com.saymyname.webapp.dto.leaderboard.LeaderboardResponseDto;
import com.saymyname.webapp.dto.leaderboard.XpHistoryResponseDto;
import com.saymyname.webapp.dto.leaderboard.XpEventDto;
import com.saymyname.webapp.mapper.leaderboard.LeaderboardDtoMapper;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final LeaderboardService leaderboardService;
    private final XpHistoryService xpHistoryService;
    private final UserService userService;
    private final LeaderboardDtoMapper dtoMapper;

    public LeaderboardController(
            LeaderboardService leaderboardService,
            XpHistoryService xpHistoryService,
            UserService userService,
            LeaderboardDtoMapper dtoMapper) {
        this.leaderboardService = leaderboardService;
        this.xpHistoryService = xpHistoryService;
        this.userService = userService;
        this.dtoMapper = dtoMapper;
    }

    @GetMapping
    public LeaderboardResponseDto getTop(
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            Principal principal) {

        User me = userService.getCurrentUserOrThrow(principal);

        List<LeaderboardEntry> top = leaderboardService.getTop(limit);
        LeaderboardEntry my = leaderboardService.getMeEntry(me);

        List<LeaderboardEntryDto> entryDtos = top.stream().map(dtoMapper::toDto).toList();

        Long myUserId = me.getId();
        Long myRank = (my != null ? my.getRank() : 0L);
        Long myXp = (my != null ? my.getXp() : 0L);

        return new LeaderboardResponseDto(
                LocalDateTime.now(),
                entryDtos,
                myUserId,
                myRank,
                myXp);
    }

    @GetMapping("/me")
    public LeaderboardEntryDto getMe(Principal principal) {
        User me = userService.getCurrentUserOrThrow(principal);
        LeaderboardEntry entry = leaderboardService.getMeEntry(me);

        if (entry == null) {
            return new LeaderboardEntryDto(
                    me.getId(),
                    me.getDisplayName(),
                    0L,
                    0L,
                    null);
        }

        return dtoMapper.toDto(entry);
    }

    @GetMapping("/me/events")
    public XpHistoryResponseDto getMyXpEvents(
            Principal principal,
            @RequestParam(name = "limit", defaultValue = "50") int limit,
            @RequestParam(name = "before", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime before,
            @RequestParam(name = "beforeId", required = false) Long beforeId) {

        User me = userService.getCurrentUserOrThrow(principal);

        List<XpEventHistoryItem> events = xpHistoryService.getMyHistory(me, before, beforeId, limit);
        List<XpEventDto> eventDtos = events.stream().map(dtoMapper::toDto).toList();

        LocalDateTime nextBefore = null;
        Long nextBeforeId = null;
        if (!events.isEmpty()) {
            XpEventHistoryItem last = events.get(events.size() - 1);
            nextBefore = last.getCreatedAt();
            nextBeforeId = last.getId();
        }

        return new XpHistoryResponseDto(
                me.getId(),
                LocalDateTime.now(),
                eventDtos,
                nextBefore,
                nextBeforeId);
    }
}
