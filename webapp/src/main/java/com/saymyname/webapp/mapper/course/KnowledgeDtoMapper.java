package com.saymyname.webapp.mapper.course;

import org.springframework.stereotype.Component;

import com.saymyname.core.model.course.Knowledge;
import com.saymyname.webapp.dto.course.KnowledgeDto;
import com.saymyname.webapp.mapper.GameModeDtoMapper;
import com.saymyname.webapp.mapper.PersonDtoMapper;
import com.saymyname.webapp.mapper.ReducedPersonDtoMapper;
import com.saymyname.webapp.mapper.UserDtoMapper;

@Component
public class KnowledgeDtoMapper {

    private final GameModeDtoMapper gameModeDtoMapper;
    private final ReducedPersonDtoMapper reducedPersonDtoMapper;
    private final PersonDtoMapper personDtoMapper;
    private final UserDtoMapper userDtoMapper;

    public KnowledgeDtoMapper(GameModeDtoMapper gameModeDtoMapper, ReducedPersonDtoMapper reducedPersonDtoMapper,
            PersonDtoMapper personDtoMapper, UserDtoMapper userDtoMapper) {
        this.gameModeDtoMapper = gameModeDtoMapper;
        this.reducedPersonDtoMapper = reducedPersonDtoMapper;
        this.userDtoMapper = userDtoMapper;
        this.personDtoMapper = personDtoMapper; // Not needed if unused
    }

    public KnowledgeDto toDto(Knowledge knowledge) {
        return new KnowledgeDto(
                knowledge.getId(),
                userDtoMapper.toReducedDto(knowledge.getUser()),
                gameModeDtoMapper.toDto(knowledge.getGameMode()),
                reducedPersonDtoMapper.toDto(knowledge.getPerson()),
                knowledge.getStatus(),
                knowledge.getNextReviewDate(),
                knowledge.getLastReviewDate(),
                knowledge.getTotalRepetitionCount(),
                knowledge.getFailureCount(),
                knowledge.getSuccessCount(),
                knowledge.getSrsStreak(),
                knowledge.getGlobalStreak(),
                knowledge.getEaseFactor(),
                knowledge.getDifficulty(),
                knowledge.getStability());
    }

    public Knowledge toModel(KnowledgeDto dto) {
        return Knowledge.builder()
                .id(dto.id())
                .user(userDtoMapper.toModel(dto.user()))
                .gameMode(gameModeDtoMapper.toModel(dto.gameMode()))
                .person(reducedPersonDtoMapper.toModel(dto.person()))
                .status(dto.status())
                .nextReviewDate(dto.nextReviewDate())
                .lastReviewDate(dto.lastReviewDate())
                .totalRepetitionCount(dto.totalRepetitionCount())
                .failureCount(dto.failureCount())
                .successCount(dto.successCount())
                .srsStreak(dto.srsStreak())
                .globalStreak(dto.globalStreak())
                .easeFactor(dto.easeFactor())
                .difficulty(dto.difficulty())
                .stability(dto.stability())
                .build();
    }

    public Knowledge toModel(Long personId) {
        return Knowledge.builder()
                .person(personDtoMapper.toModel(personId))
                .build();
    }

}
