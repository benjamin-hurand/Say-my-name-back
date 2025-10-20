package com.saymyname.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.saymyname.persistence.dao.ChangeRequestItemDao;

@Service
public class ChangeRequestItemService {

    private final ChangeRequestItemDao changeRequestItemDao;

    public ChangeRequestItemService(ChangeRequestItemDao changeRequestItemDao) {
        this.changeRequestItemDao = changeRequestItemDao;
    }

    /**
     * Détache en masse les FK CR→PA pour les PA tombstones expirées, org courante.
     */
    @Transactional
    public int detachExpiredTombstoneLinksForResolved(LocalDateTime cutoffExclusive) {
        if (cutoffExclusive == null) {
            throw new IllegalArgumentException("cutoffExclusive ne peut pas être null");
        }
        return changeRequestItemDao.detachExpiredTombstoneLinksForResolved(cutoffExclusive);
    }

}
