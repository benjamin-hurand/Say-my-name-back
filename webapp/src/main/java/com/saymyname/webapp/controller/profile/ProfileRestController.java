// src/main/java/com/saymyname/webapp/controller/profile/ProfileRestController.java
package com.saymyname.webapp.controller.profile;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.people.Person;
import com.saymyname.service.ChangeRequestService;
import com.saymyname.service.UserOrganizationService;
import com.saymyname.service.UserService;
import com.saymyname.service.leaderboard.LeaderboardService; // ✅
import com.saymyname.core.model.leaderboard.LeaderboardEntry; // ✅
import com.saymyname.service.person.PersonService;
import com.saymyname.webapp.dto.PersonAttributeDto;
import com.saymyname.webapp.dto.PersonDto;
import com.saymyname.webapp.dto.UserDto;
import com.saymyname.webapp.dto.changerequest.ChangeRequestSummaryDto;
import com.saymyname.webapp.dto.profile.AttributeValuesResponseDto;
import com.saymyname.webapp.dto.profile.BulkPersonAttributeRequest;
import com.saymyname.webapp.dto.profile.ProfileOnboardingDto;
import com.saymyname.webapp.dto.profile.ProfileResponseDto;
import com.saymyname.webapp.dto.profile.ProfileXpSummaryDto; // ✅
import com.saymyname.webapp.dto.profile.UpdateDisplayNameRequestDto;
import com.saymyname.webapp.dto.profile.UpdateDisplayNameResponseDto;
import com.saymyname.webapp.mapper.BulkPersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.ChangeRequestDtoMapper;
import com.saymyname.webapp.mapper.PersonAttributeDtoMapper;
import com.saymyname.webapp.mapper.PersonDtoMapper;
import com.saymyname.webapp.mapper.ProfileOnboardingDtoMapper;
import com.saymyname.webapp.mapper.UserDtoMapper;

@RestController
@RequestMapping("/api/profile")
public class ProfileRestController {

        private final PersonService personService;
        private final ChangeRequestService changeRequestService;
        private final ChangeRequestDtoMapper changeRequestDtoMapper;
        private final PersonDtoMapper personDtoMapper;
        private final BulkPersonAttributeDtoMapper bulkPersonAttributeDtoMapper;
        private final PersonAttributeDtoMapper personAttributeDtoMapper;

        private final UserService userService;
        private final UserOrganizationService userOrganizationService;
        private final UserDtoMapper userDtoMapper;
        private final ProfileOnboardingDtoMapper profileOnboardingDtoMapper;

        private final LeaderboardService leaderboardService; // ✅

        private static final Logger logger = LoggerFactory.getLogger(ProfileRestController.class);

        public ProfileRestController(
                        PersonService personService,
                        ChangeRequestService changeRequestService,
                        ChangeRequestDtoMapper changeRequestDtoMapper,
                        PersonDtoMapper personDtoMapper,
                        BulkPersonAttributeDtoMapper bulkPersonAttributeDtoMapper,
                        PersonAttributeDtoMapper personAttributeDtoMapper,
                        UserService userService,
                        UserOrganizationService userOrganizationService,
                        UserDtoMapper userDtoMapper,
                        ProfileOnboardingDtoMapper profileOnboardingDtoMapper,
                        LeaderboardService leaderboardService // ✅
        ) {
                this.personService = personService;
                this.changeRequestService = changeRequestService;
                this.changeRequestDtoMapper = changeRequestDtoMapper;
                this.personDtoMapper = personDtoMapper;
                this.bulkPersonAttributeDtoMapper = bulkPersonAttributeDtoMapper;
                this.personAttributeDtoMapper = personAttributeDtoMapper;
                this.userService = userService;
                this.userOrganizationService = userOrganizationService;
                this.userDtoMapper = userDtoMapper;
                this.profileOnboardingDtoMapper = profileOnboardingDtoMapper;
                this.leaderboardService = leaderboardService; // ✅
        }

        /**
         * GET /api/profile
         * Récupère le profil (user + person + changeRequests + onboarding + xpSummary).
         */
        @GetMapping
        public ResponseEntity<ProfileResponseDto> getProfile(Principal principal) {
                User me = userService.getCurrentUserOrThrow(principal);
                User user = userService.findByIdWithEmails(me.getId()).orElse(me);

                OrgRole orgRole = userOrganizationService.findRoleForCurrentOrg(user.getId()).orElse(null);
                UserDto userDto = userDtoMapper.toDto(user, orgRole);

                Optional<Person> optPerson = personService.getPersonByUserWithAllAttributes(user);
                PersonDto personDto = optPerson.map(personDtoMapper::toDto).orElse(null);

                List<ChangeRequestSummaryDto> crDtos = optPerson.isPresent()
                                ? changeRequestService.findOpenForUser(user.getId()).stream()
                                                .map(changeRequestDtoMapper::toSummaryDto)
                                                .toList()
                                : List.of();

                ProfileOnboardingDto onboarding = null;
                if (optPerson.isEmpty()) {
                        onboarding = userOrganizationService.findMembershipForCurrentOrg(user.getId())
                                        .map(profileOnboardingDtoMapper::toDto)
                                        .orElse(null);
                }

                // ✅ XP summary (cheap read)
                ProfileXpSummaryDto xpSummary = null;
                LeaderboardEntry my = leaderboardService.getMeEntry(user);
                if (my != null) {
                        xpSummary = new ProfileXpSummaryDto(
                                        my.getXp(),
                                        my.getRank(),
                                        my.getLastEventAt());
                } else {
                        xpSummary = new ProfileXpSummaryDto(0, 0, null);
                }

                ProfileResponseDto response = new ProfileResponseDto(
                                userDto,
                                personDto,
                                crDtos,
                                onboarding,
                                xpSummary);

                return ResponseEntity.ok(response);
        }

        /**
         * PATCH /api/profile/display-name
         */
        @PatchMapping("/display-name")
        public ResponseEntity<UpdateDisplayNameResponseDto> updateMyDisplayName(
                        @RequestBody UpdateDisplayNameRequestDto body,
                        Principal principal) {

                User me = userService.getCurrentUserOrThrow(principal);

                String newDisplayName = body != null ? body.displayName() : null;
                User updated = userService.updateDisplayName(me, newDisplayName);

                return ResponseEntity.ok(new UpdateDisplayNameResponseDto(updated.getDisplayName()));
        }

        /**
         * POST /api/profile/attributes/{attributeId}/bulk
         */
        @PostMapping("/attributes/{attributeId}/bulk")
        public ResponseEntity<AttributeValuesResponseDto> applyAttributeChanges(
                        @PathVariable("attributeId") Long attributeId,
                        @RequestBody BulkPersonAttributeRequest body,
                        Principal principal) {

                logger.info("Applying bulk attribute changes for attributeId: {}", attributeId);

                User me = userService.getCurrentUserOrThrow(principal);
                User user = userService.findByIdWithEmails(me.getId()).orElse(me);

                var toCreate = bulkPersonAttributeDtoMapper.toCreateModels(body.create());
                var toUpdate = bulkPersonAttributeDtoMapper.toUpdateModels(body.update());
                var toDelete = bulkPersonAttributeDtoMapper.toDeleteModels(body.delete());

                var updatedAttributes = personService.applyAttributeChangesForUser(
                                user,
                                attributeId,
                                toCreate,
                                toUpdate,
                                toDelete);

                List<PersonAttributeDto> updatedDtos = updatedAttributes.stream()
                                .map(personAttributeDtoMapper::toDto)
                                .toList();

                var response = new AttributeValuesResponseDto(attributeId, updatedDtos);
                return ResponseEntity.ok(response);
        }
}
