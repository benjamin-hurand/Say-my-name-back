package com.saymyname.persistence.dao.leaderboard;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.leaderboard.XpEventHistoryItem;
import com.saymyname.persistence.repository.leaderboard.XpEventRepository;

@Repository
@Transactional
public class XpEventDao {

        private final XpEventRepository xpEventRepository;

        public XpEventDao(XpEventRepository xpEventRepository) {
                this.xpEventRepository = xpEventRepository;
        }

        @Transactional(readOnly = true)
        public List<XpEventHistoryItem> findHistoryForUser(
                        Long userId,
                        java.time.LocalDateTime beforeCreatedAt,
                        Long beforeId,
                        int limit) {
                int safeLimit = Math.max(1, Math.min(limit, 200));

                // ✅ IMPORTANT : pas de `var` ici
                List<XpEventRepository.XpEventRow> rows = xpEventRepository.findHistoryForUser(
                                userId,
                                beforeCreatedAt,
                                beforeId,
                                PageRequest.of(0, safeLimit));

                return rows.stream()
                                // ✅ IMPORTANT : typer le lambda
                                .map((XpEventRepository.XpEventRow r) -> XpEventHistoryItem.builder()
                                                .id(r.getId())
                                                .eventId(r.getEventId())
                                                .eventKey(r.getEventKey())
                                                .sourceType(r.getSourceType())
                                                .sourceId(r.getSourceId())
                                                .deltaXp(r.getDeltaXp())
                                                .createdAt(r.getCreatedAt())
                                                .build())
                                // si ton projet n’est pas en Java 16+, utilise plutôt collect:
                                .collect(Collectors.toList());
        }
}
