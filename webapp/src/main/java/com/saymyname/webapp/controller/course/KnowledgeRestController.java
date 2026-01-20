// src/main/java/com/saymyname/webapp/controller/course/KnowledgeRestController.java
package com.saymyname.webapp.controller.course;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.security.CustomUserDetails;
import com.saymyname.service.UserService;
import com.saymyname.service.course.KnowledgeService;
import com.saymyname.webapp.dto.course.KnowledgeResultDto;
import com.saymyname.webapp.mapper.course.KnowledgeResultDtoMapper;

@RestController
@RequestMapping("/api/knowledges")
public class KnowledgeRestController {

    private static final Logger logger = LoggerFactory.getLogger(KnowledgeRestController.class);

    private final KnowledgeResultDtoMapper knowledgeResultDtoMapper;
    private final KnowledgeService knowledgeService;
    private final UserService userService;

    public KnowledgeRestController(
            KnowledgeResultDtoMapper knowledgeResultDtoMapper,
            KnowledgeService knowledgeService,
            UserService userService) {
        this.knowledgeResultDtoMapper = knowledgeResultDtoMapper;
        this.knowledgeService = knowledgeService;
        this.userService = userService;
    }

    /**
     * Reçoit un batch de résultats et les enregistre pour l'utilisateur
     * authentifié.
     * Récupère l'utilisateur le plus efficacement possible :
     * - priorité à @AuthenticationPrincipal (CustomUserDetails → pas de requête
     * DB),
     * - fallback sur UserService.getCurrentAuthenticatedUserOrThrow() si besoin.
     */
    @PostMapping("/results")
    public ResponseEntity<Void> submitResults(
            @RequestBody List<KnowledgeResultDto> resultsDto,
            @AuthenticationPrincipal CustomUserDetails current) {

        // 1) Récupération optimisée de l'utilisateur courant
        final User user = (current != null && current.getUser() != null)
                ? current.getUser()
                : userService.getCurrentAuthenticatedUserOrThrow();

        // 2) Mapping DTO -> événements métier
        List<KnowledgeResultEvent> events = resultsDto.stream()
                .map(knowledgeResultDtoMapper::toModel)
                .toList();

        // 3) Enregistrement
        var xpAward = knowledgeService.recordBatchResults(user, events);

        logger.info("Knowledge batch processed | userId={} displayName={} eventsCount={}",
                user.getId(), user.getDisplayName(), events.size());

        return ResponseEntity.ok().build();
    }
}
