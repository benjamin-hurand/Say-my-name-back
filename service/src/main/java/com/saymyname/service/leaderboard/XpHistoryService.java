// src/main/java/com/saymyname/service/leaderboard/XpHistoryService.java
package com.saymyname.service.leaderboard;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.leaderboard.XpEventHistoryItem;
import com.saymyname.persistence.dao.leaderboard.XpEventDao;

@Service
public class XpHistoryService {

    private final XpEventDao xpEventDao;

    public XpHistoryService(XpEventDao xpEventDao) {
        this.xpEventDao = xpEventDao;
    }

    @Transactional(readOnly = true)
    public List<XpEventHistoryItem> getMyHistory(User me, LocalDateTime beforeCreatedAt, Long beforeId, int limit) {
        if (me == null || me.getId() == null)
            return List.of();
        return xpEventDao.findHistoryForUser(me.getId(), beforeCreatedAt, beforeId, limit);
    }
}
