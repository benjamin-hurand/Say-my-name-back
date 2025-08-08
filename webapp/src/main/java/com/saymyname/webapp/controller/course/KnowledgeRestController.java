package com.saymyname.webapp.controller.course;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.saymyname.core.model.common.User;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.service.UserService;
import com.saymyname.service.course.KnowledgeService;
import com.saymyname.webapp.dto.course.KnowledgeResultDto;
import com.saymyname.webapp.mapper.course.KnowledgeResultDtoMapper;

@RestController
@RequestMapping("/api/knowledges")
public class KnowledgeRestController {

    private final Logger logger = LoggerFactory.getLogger(KnowledgeRestController.class);

    private final KnowledgeResultDtoMapper knowledgeResultDtoMapper;
    private final KnowledgeService knowledgeService;
    private final UserService userService;

    public KnowledgeRestController(KnowledgeResultDtoMapper knowledgeResultDtoMapper,
            KnowledgeService knowledgeService, UserService userService) {
        this.knowledgeResultDtoMapper = knowledgeResultDtoMapper;
        this.knowledgeService = knowledgeService;
        this.userService = userService;
    }

    @PostMapping("/results")
    public ResponseEntity<?> submitResults(
            @RequestBody List<KnowledgeResultDto> resultsDto,
            Principal principal) {

        // 1) le nom du principal (login ou email)
        String name = principal.getName();
        if (name == null || name.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // 2) recharger votre User métier
        User user;
        try {
            user = userService.findByEmailOrUsername(name);
        } catch (UsernameNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        logger.info("CONNECTE AVEC : " + user.getUsername());

        // 1) convertir chaque DTO en event métier
        List<KnowledgeResultEvent> events = resultsDto.stream()
                .map(knowledgeResultDtoMapper::toModel)
                .toList();

        // 3) déléguer au service
        int nbDeKnowledges = knowledgeService.recordBatchResults(user, events);

        logger.info("NOMBRE KNOWLEDGE INSERTED: " + nbDeKnowledges);

        return ResponseEntity.ok().build();
    }
}
