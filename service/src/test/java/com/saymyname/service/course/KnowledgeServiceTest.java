package com.saymyname.service.course;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.course.Knowledge;
import com.saymyname.core.model.course.KnowledgeResultEvent;
import com.saymyname.core.model.enums.KnowledgeStatus;
import com.saymyname.core.model.enums.SrsAlgorithm;
import com.saymyname.core.model.people.Person;
import com.saymyname.core.model.quiz.options.GameMode;
import com.saymyname.persistence.dao.FactDao;
import com.saymyname.persistence.dao.course.KnowledgeDao;
import com.saymyname.service.course.scheduler.SchedulerStrategy;
import com.saymyname.service.leaderboard.LeaderboardService;

class KnowledgeServiceTest {

        @Test
        void recordBatchResults_knowledgeIdFastPath_doesNotRequireFallback() {
                KnowledgeDao knowledgeDao = mock(KnowledgeDao.class);
                LeaderboardService leaderboardService = mock(LeaderboardService.class);
                KnowledgeService service = newService(knowledgeDao, leaderboardService);

                User user = new User.Builder().withId(1L).withSrsAlgorithm(SrsAlgorithm.SM2).build();
                Knowledge knowledge = buildKnowledge(101L, user, 7L, 9L);

                when(knowledgeDao.findAllByIdsForUser(eq(1L), eq(List.of(101L))))
                                .thenReturn(List.of(knowledge));

                KnowledgeResultEvent event = new KnowledgeResultEvent.Builder()
                                .withKnowledgeId(101L)
                                .withCorrect(true)
                                .withHelpUsed(false)
                                .build();

                service.recordBatchResults(user, List.of(event));

                verify(knowledgeDao, never()).findByUserGameModeAndPerson(anyLong(), anyLong(), anyLong());
                verify(knowledgeDao).upsertKnowledge(any(Knowledge.class));
        }

        @Test
        void recordBatchResults_modePersonFallback_ok() {
                KnowledgeDao knowledgeDao = mock(KnowledgeDao.class);
                LeaderboardService leaderboardService = mock(LeaderboardService.class);
                KnowledgeService service = newService(knowledgeDao, leaderboardService);

                User user = new User.Builder().withId(2L).withSrsAlgorithm(SrsAlgorithm.SM2).build();
                Long gameModeId = 3L;
                Long personId = 5L;

                when(knowledgeDao.findByUserGameModeAndPerson(eq(2L), eq(gameModeId), eq(personId)))
                                .thenReturn(Optional.empty());

                KnowledgeResultEvent event = new KnowledgeResultEvent.Builder()
                                .withGameModeId(gameModeId)
                                .withPersonId(personId)
                                .withCorrect(true)
                                .withHelpUsed(false)
                                .build();

                service.recordBatchResults(user, List.of(event));

                ArgumentCaptor<Knowledge> captor = ArgumentCaptor.forClass(Knowledge.class);
                verify(knowledgeDao).upsertKnowledge(captor.capture());
                Knowledge saved = captor.getValue();
                assertEquals(gameModeId, saved.getGameMode().getId());
                assertEquals(personId, saved.getPerson().getId());
                assertEquals(user.getId(), saved.getUser().getId());
        }

        @Test
        void recordBatchResults_knowledgeIdNotOwned_throws() {
                KnowledgeDao knowledgeDao = mock(KnowledgeDao.class);
                LeaderboardService leaderboardService = mock(LeaderboardService.class);
                KnowledgeService service = newService(knowledgeDao, leaderboardService);

                User user = new User.Builder().withId(3L).withSrsAlgorithm(SrsAlgorithm.SM2).build();
                Long knowledgeId = 999L;

                when(knowledgeDao.findAllByIdsForUser(eq(3L), eq(List.of(knowledgeId))))
                                .thenReturn(List.of());
                when(knowledgeDao.findByIdForUser(eq(3L), eq(knowledgeId)))
                                .thenReturn(Optional.empty());

                KnowledgeResultEvent event = new KnowledgeResultEvent.Builder()
                                .withKnowledgeId(knowledgeId)
                                .withCorrect(true)
                                .withHelpUsed(false)
                                .build();

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> service.recordBatchResults(user, List.of(event)));

                assertTrue(ex.getMessage().contains("knowledgeId=" + knowledgeId));
                verify(knowledgeDao, never()).upsertKnowledge(any(Knowledge.class));
        }

        private static KnowledgeService newService(
                        KnowledgeDao knowledgeDao,
                        LeaderboardService leaderboardService) {
                SchedulerStrategy scheduler = new SchedulerStrategy() {
                        @Override
                        public int mapGrade(boolean correct) {
                                return correct ? 5 : 0;
                        }

                        @Override
                        public void schedule(Knowledge k, int grade) {
                                // no-op for unit tests
                        }
                };

                return new KnowledgeService(
                                knowledgeDao,
                                Map.of(SrsAlgorithm.SM2, scheduler),
                                SrsAlgorithm.SM2,
                                mock(FactDao.class),
                                leaderboardService);
        }

        private static Knowledge buildKnowledge(Long id, User user, Long gameModeId, Long personId) {
                return new Knowledge.Builder()
                                .withId(id)
                                .withUser(user)
                                .withGameMode(new GameMode.Builder().withId(gameModeId).build())
                                .withPerson(new Person.Builder().withId(personId).build())
                                .withStatus(KnowledgeStatus.DISCOVERED)
                                .withNextReviewDate(LocalDateTime.now())
                                .withLastReviewDate(LocalDateTime.now())
                                .withTotalRepetitionCount(0)
                                .withFailureCount(0)
                                .withSuccessCount(0)
                                .withSrsStreak(0)
                                .withGlobalStreak(0)
                                .withEaseFactor(java.math.BigDecimal.valueOf(2.5))
                                .withDifficulty(1.0)
                                .withStability(1.0)
                                .build();
        }
}
