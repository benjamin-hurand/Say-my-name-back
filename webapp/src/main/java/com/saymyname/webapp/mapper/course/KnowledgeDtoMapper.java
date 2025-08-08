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
        return new Knowledge.Builder()
                .withId(dto.id())
                .withUser(userDtoMapper.toModel(dto.user()))
                .withGameMode(gameModeDtoMapper.toModel(dto.gameMode()))
                .withPerson(reducedPersonDtoMapper.toModel(dto.person()))
                .withStatus(dto.status())
                .withNextReviewDate(dto.nextReviewDate())
                .withLastReviewDate(dto.lastReviewDate())
                .withTotalRepetitionCount(dto.totalRepetitionCount())
                .withFailureCount(dto.failureCount())
                .withSuccessCount(dto.successCount())
                .withSrsStreak(dto.srsStreak())
                .withGlobalStreak(dto.globalStreak())
                .withEaseFactor(dto.easeFactor())
                .withDifficulty(dto.difficulty())
                .withStability(dto.stability())
                .build();
    }

    public Knowledge toModel(Long personId) {
        return new Knowledge.Builder()
                .withPerson(personDtoMapper.toModel(personId))
                .build();
    }

}
