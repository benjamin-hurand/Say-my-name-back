// src/main/java/com/saymyname/webapp/controller/subscription/UserSubscriptionRestController.java
package com.saymyname.webapp.controller.subscription;

import java.security.Principal;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.people.UserSubscription;
import com.saymyname.core.model.people.SubscriptionBulkResult; // NEW (model service)
import com.saymyname.core.model.persondirectory.PersonSearchCriteria;
import com.saymyname.service.UserSubscriptionService;
import com.saymyname.service.UserService; // contient getCurrentUserOrThrow(Principal)
import com.saymyname.webapp.dto.person.PersonSearchRequestDto;
import com.saymyname.webapp.dto.subscription.*;
import com.saymyname.webapp.mapper.person.PersonDirectoryDtoMapper;
import com.saymyname.webapp.mapper.subscription.UserSubscriptionDtoMapper;

@RestController
@RequestMapping("/api/subscriptions")
public class UserSubscriptionRestController {

    private final UserSubscriptionService subscriptionService;
    private final UserSubscriptionDtoMapper mapper;
    private final UserService userService;
    private final PersonDirectoryDtoMapper personDirectoryDtoMapper; // NEW

    public UserSubscriptionRestController(UserSubscriptionService subscriptionService,
            UserSubscriptionDtoMapper mapper,
            UserService userService,
            PersonDirectoryDtoMapper personDirectoryDtoMapper) { // NEW
        this.subscriptionService = subscriptionService;
        this.mapper = mapper;
        this.userService = userService;
        this.personDirectoryDtoMapper = personDirectoryDtoMapper; // NEW
    }

    /** Récupère l'ID utilisateur en s'appuyant sur ta méthode standardisée. */
    private Long currentUserId(Principal principal) {
        return userService.getCurrentUserOrThrow(principal).getId();
    }

    /** Liste paginée des abonnements (objets complets). */
    @GetMapping
    public Page<UserSubscriptionDto> list(Pageable pageable, Principal principal) {
        Long uid = currentUserId(principal);
        return subscriptionService.listSubscriptions(uid, pageable).map(mapper::toDto);
    }

    /** Liste paginée des IDs de personnes suivies (optimisé pour le Quiz). */
    @GetMapping("/person-ids")
    public Page<Long> listIds(Pageable pageable, Principal principal) {
        Long uid = currentUserId(principal);
        return subscriptionService.listFollowedPersonIds(uid, pageable);
    }

    /** Compteur des suivis. */
    @GetMapping("/count")
    public CountDto count(Principal principal) {
        Long uid = currentUserId(principal);
        return new CountDto(subscriptionService.countFollowed(uid));
    }

    /** S'abonner à une personne. */
    @PostMapping("/{personId}")
    public ResponseEntity<Void> subscribeOne(@PathVariable("personId") Long personId, Principal principal) {
        Long uid = currentUserId(principal);
        boolean inserted = subscriptionService.subscribe(
                UserSubscription.builder().userId(uid).personId(personId).build());
        return inserted ? ResponseEntity.status(HttpStatus.CREATED).build()
                : ResponseEntity.noContent().build();
    }

    /** Se désabonner d'une personne. */
    @DeleteMapping("/{personId}")
    public ResponseEntity<Void> unsubscribeOne(@PathVariable("personId") Long personId, Principal principal) {
        Long uid = currentUserId(principal);
        subscriptionService.unsubscribe(uid, personId);
        return ResponseEntity.noContent().build();
    }

    /** S'abonner en masse à partir d'une liste d'IDs. */
    @PostMapping("/bulk/follow")
    public ResponseEntity<BulkSubscribeResponseDto> bulkFollow(@RequestBody SubscribeBulkRequestDto body,
            Principal principal) {
        Long uid = currentUserId(principal);
        List<Long> ids = body.personIds();
        int requested = (ids != null) ? ids.size() : 0;

        int inserted = subscriptionService.bulkSubscribe(uid, ids);
        int already = Math.max(0, requested - inserted);

        return ResponseEntity.ok(new BulkSubscribeResponseDto(requested, inserted, already));
    }

    @PostMapping("/bulk/unfollow")
    public ResponseEntity<BulkUnsubscribeResponseDto> bulkUnfollow(
            @RequestBody UnsubscribeBulkRequestDto body,
            Principal principal) {

        Long uid = currentUserId(principal);
        var ids = body.personIds();
        int requested = (ids != null) ? ids.size() : 0;

        int removed = subscriptionService.bulkUnsubscribe(uid, ids);
        int notFoundOrAlready = Math.max(0, requested - removed);

        return ResponseEntity.ok(new BulkUnsubscribeResponseDto(requested, removed, notFoundOrAlready));
    }

    /*
     * =========================
     * == NOUVEAUX ENDPOINTS ==
     * =========================
     */

    /** NEW: Suivre tous les résultats d'une recherche (idempotent). */
    @PostMapping("/bulk/search/follow")
    public ResponseEntity<BulkBySearchResultDto> bulkFollowBySearch(
            @RequestBody PersonSearchRequestDto request,
            Principal principal) {

        Long uid = currentUserId(principal);

        // DTO -> Model
        PersonSearchCriteria criteria = personDirectoryDtoMapper.toModel(request);

        // Service (ignorera tri/pagination et followedOnly)
        SubscriptionBulkResult res = subscriptionService.followAllMatching(criteria, uid);

        return ResponseEntity.ok(new BulkBySearchResultDto(
                res.getMatched(),
                res.getActed(),
                res.getSkipped(),
                res.getSeconds()));
    }

    /** NEW: Ne plus suivre tous les résultats d'une recherche (idempotent). */
    @PostMapping("/bulk/search/unfollow")
    public ResponseEntity<BulkBySearchResultDto> bulkUnfollowBySearch(
            @RequestBody PersonSearchRequestDto request,
            Principal principal) {

        Long uid = currentUserId(principal);

        // DTO -> Model
        PersonSearchCriteria criteria = personDirectoryDtoMapper.toModel(request);

        SubscriptionBulkResult res = subscriptionService.unfollowAllMatching(criteria, uid);

        return ResponseEntity.ok(new BulkBySearchResultDto(
                res.getMatched(),
                res.getActed(),
                res.getSkipped(),
                res.getSeconds()));
    }
}
