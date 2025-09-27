// src/main/java/com/saymyname/service/profile/PhotoBackfillService.java
package com.saymyname.service.profile;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.saymyname.persistence.repository.PhotoRepository;

@Service
public class PhotoBackfillService {

    private static final Logger log = LoggerFactory.getLogger(PhotoBackfillService.class);

    private final PhotoRepository photoRepository;
    private final PhotoService photoService;

    public PhotoBackfillService(PhotoRepository photoRepository, PhotoService photoService) {
        this.photoRepository = photoRepository;
        this.photoService = photoService;
    }

    /**
     * Backfill sur toutes les photos APPROVED : (ré)génère la miniature si
     * manquante.
     *
     * @return stats formatées.
     */
    public String backfillApprovedThumbnails() {
        List<String> keys = photoRepository.findAllApprovedKeys();
        return doBackfill(keys, "APPROVED");
    }

    /**
     * Backfill sur toutes les photos (quel que soit le statut).
     */
    public String backfillAllThumbnails() {
        List<String> keys = photoRepository.findAllKeys();
        return doBackfill(keys, "ALL");
    }

    private String doBackfill(List<String> keys, String label) {
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger fail = new AtomicInteger();

        log.info("Backfill thumbnails [{}] - {} keys à traiter", label, keys.size());

        // simple boucle (en dev, c’est suffisant). Tu peux chunker si tu veux.
        for (String key : keys) {
            try {
                boolean ensured = photoService.ensureSmallExists(key);
                if (ensured) {
                    ok.incrementAndGet();
                } else {
                    fail.incrementAndGet();
                    log.warn("Miniature non assurée pour key={}", key);
                }
            } catch (Exception ex) {
                fail.incrementAndGet();
                log.warn("Erreur backfill key={}: {}", key, ex.getMessage());
            }
        }

        String stats = String.format("Backfill [%s] terminé: total=%d, OK=%d, KO=%d",
                label, keys.size(), ok.get(), fail.get());
        log.info(stats);
        return stats;
    }
}
