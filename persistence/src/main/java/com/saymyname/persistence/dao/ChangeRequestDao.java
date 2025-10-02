// src/main/java/com/saymyname/persistence/dao/ChangeRequestDao.java
package com.saymyname.persistence.dao;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.ChangeStatus;
import com.saymyname.core.model.people.ChangeRequest;
import com.saymyname.core.model.people.ChangeRequestItem;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.entity.organization.ChangeRequestEntity;
import com.saymyname.persistence.entity.organization.ChangeRequestItemEntity;
import com.saymyname.persistence.mapper.ChangeRequestEntityMapper;
import com.saymyname.persistence.mapper.ChangeRequestItemEntityMapper;
import com.saymyname.persistence.repository.ChangeRequestRepository;
import com.saymyname.persistence.repository.UserRepository;

@Repository
public class ChangeRequestDao {

    private final ChangeRequestRepository crRepo;
    private final ChangeRequestEntityMapper crMapper;
    private final ChangeRequestItemEntityMapper itemMapper;
    private final UserRepository userRepository;

    public ChangeRequestDao(ChangeRequestRepository crRepo,
            ChangeRequestEntityMapper crMapper,
            ChangeRequestItemEntityMapper itemMapper,
            UserRepository userRepository) {
        this.crRepo = crRepo;
        this.crMapper = crMapper;
        this.itemMapper = itemMapper;
        this.userRepository = userRepository;
    }

    /* ---------- Queries ---------- */

    @Transactional(readOnly = true)
    public ChangeRequest findOpenByTriplet(Long personId, Long requesterId, Long attributeId,
            Set<ChangeStatus> openStatuses) {
        return crRepo.findFirstOpenByTriplet(personId, requesterId, attributeId, openStatuses)
                .map(crMapper::toModel)
                .orElse(null);
    }

    @Transactional(readOnly = true)
    public ChangeRequest getEnvelopeOrThrow(Long id) {
        ChangeRequestEntity e = crRepo.findByIdDeep(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ChangeRequest introuvable"));
        return crMapper.toModel(e);
    }

    /* ---------- Create ---------- */

    @Transactional
    public ChangeRequest createEnvelope(ChangeRequest envelope) {
        // Model -> Entity
        ChangeRequestEntity e = crMapper.toEntity(envelope);

        // cohérence parent/enfants
        if (e.getItems() != null) {
            for (ChangeRequestItemEntity it : e.getItems()) {
                it.setId(null);
                it.setChangeRequest(e);
            }
        }

        LocalDateTime now = LocalDateTime.now();
        if (e.getCreatedAt() == null)
            e.setCreatedAt(now);
        e.setUpdatedAt(now);
        if (e.getStatus() == null)
            e.setStatus(ChangeStatus.PENDING);

        ChangeRequestEntity saved = crRepo.save(e);

        // reload DEEP pour renvoyer un modèle hydraté
        ChangeRequestEntity deep = crRepo.findByIdDeep(saved.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ChangeRequest introuvable après création"));
        return crMapper.toModel(deep);
    }

    /* ---------- Replace (par id trouvé via triplet en service) ---------- */
    @Transactional
    public ChangeRequest replaceEnvelopeItems(Long envelopeId, Long requesterId, ChangeRequest source) {
        // Charger DEEP pour manipuler la collection managée (orphanRemoval-friendly)
        ChangeRequestEntity env = crRepo.findByIdDeep(envelopeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ChangeRequest introuvable"));

        Long authorId = (env.getRequester() != null ? env.getRequester().getId() : null);
        if (!Objects.equals(authorId, requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas l'auteur de cette demande.");
        }
        if (env.getStatus() != ChangeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seules les enveloppes PENDING peuvent être modifiées (actuel: " + env.getStatus() + ")");
        }

        // Mettre à jour le motif (si fourni, déjà normalisé par le service)
        if (source.getRequestReason() != null && !source.getRequestReason().trim().isEmpty()) {
            env.setRequestReason(source.getRequestReason().trim());
        }

        // Rebuild de la collection
        env.getItems().clear();
        if (source.getItems() != null) {
            for (ChangeRequestItem it : source.getItems()) {
                ChangeRequestItemEntity child = itemMapper.toEntity(it);
                child.setId(null);
                child.setChangeRequest(env);
                env.getItems().add(child);
            }
        }

        env.setUpdatedAt(LocalDateTime.now());
        crRepo.save(env);

        ChangeRequestEntity deep = crRepo.findByIdDeep(env.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "ChangeRequest introuvable après update"));
        return crMapper.toModel(deep);
    }

    @Transactional
    public void cancelEnvelope(Long changeRequestId, User requester) {
        ChangeRequestEntity e = crRepo.findById(changeRequestId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ChangeRequest introuvable"));

        Long requesterId = requester.getId();
        Long authorId = (e.getRequester() != null ? e.getRequester().getId() : null);
        if (!Objects.equals(authorId, requesterId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Vous n'êtes pas l'auteur de cette demande.");
        }
        if (e.getStatus() == ChangeStatus.CANCELED)
            return;
        if (e.getStatus() != ChangeStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Seules les enveloppes PENDING peuvent être annulées (actuel: " + e.getStatus() + ")");
        }

        LocalDateTime now = LocalDateTime.now();
        e.setStatus(ChangeStatus.CANCELED);
        e.setUpdatedAt(now);
        e.setResolvedAt(now);
        UserEntity rb = userRepository.getReferenceById(requesterId);
        e.setResolvedBy(rb);
        e.setResolutionComment("Envelope cancelled by requester #" + requesterId);

        crRepo.save(e);
    }

    @Transactional(readOnly = true)
    public List<ChangeRequest> findByUserIdAndStatuses(Long userId, Collection<ChangeStatus> statuses) {
        return crRepo.findByUserIdAndStatusesDeep(userId, statuses).stream()
                .map(crMapper::toModel)
                .toList();
    }
}
