package com.saymyname.core.model.invitation;

import com.saymyname.core.model.auth.User;
import com.saymyname.core.model.enums.InvitationType;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.people.Person;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Modèle "plat" côté core.
 * - Listes toujours non null (vide par défaut).
 * - equals/hashCode basés uniquement sur id.
 * - Helpers métier (actif/expiré/quotas/…).
 */
public class Invitation {

    private Long id;

    private InvitationType type;
    private String label;
    private String note;
    private String constraintsJson;

    private OrgRole role; // rôle/permission cible
    private String email; // cible si type=EMAIL

    private Person person; // nominative si renseignée

    private byte[] tokenHash; // 32 octets (HMAC-SHA-256)
    private String pinHashPhc; // PHC string (Argon2id conseillé)

    private Integer maxUses; // null = illimité
    private int usesCount;

    private LocalDateTime expiresAt;
    private LocalDateTime revokedAt;

    private User createdBy;
    private LocalDateTime createdAt;

    private User acceptedBy;
    private LocalDateTime acceptedAt;

    private LocalDateTime lastUsedAt;

    // Journal des usages (append-only)
    private List<InvitationUsage> usages = Collections.emptyList();

    public Invitation() {
    }

    private Invitation(Builder b) {
        this.id = b.id;
        this.type = b.type;
        this.label = b.label;
        this.note = b.note;
        this.constraintsJson = b.constraintsJson;
        this.role = b.role;
        this.email = b.email;
        this.person = b.person;
        this.tokenHash = b.tokenHash;
        this.pinHashPhc = b.pinHashPhc;
        this.maxUses = b.maxUses;
        this.usesCount = b.usesCount;
        this.expiresAt = b.expiresAt;
        this.revokedAt = b.revokedAt;
        this.createdBy = b.createdBy;
        this.createdAt = b.createdAt;
        this.acceptedBy = b.acceptedBy;
        this.acceptedAt = b.acceptedAt;
        this.lastUsedAt = b.lastUsedAt;
        setUsages(b.usages);
    }

    // --- Getters
    public Long getId() {
        return id;
    }

    public InvitationType getType() {
        return type;
    }

    public String getLabel() {
        return label;
    }

    public String getNote() {
        return note;
    }

    public String getConstraintsJson() {
        return constraintsJson;
    }

    public OrgRole getRole() {
        return role;
    }

    public String getEmail() {
        return email;
    }

    public Person getPerson() {
        return person;
    }

    public byte[] getTokenHash() {
        return tokenHash;
    }

    public String getPinHashPhc() {
        return pinHashPhc;
    }

    public Integer getMaxUses() {
        return maxUses;
    }

    public int getUsesCount() {
        return usesCount;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public User getAcceptedBy() {
        return acceptedBy;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public LocalDateTime getLastUsedAt() {
        return lastUsedAt;
    }

    public List<InvitationUsage> getUsages() {
        return usages;
    }

    // --- Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setType(InvitationType type) {
        this.type = type;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public void setConstraintsJson(String constraintsJson) {
        this.constraintsJson = constraintsJson;
    }

    public void setRole(OrgRole role) {
        this.role = role;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPerson(Person person) {
        this.person = person;
    }

    public void setTokenHash(byte[] tokenHash) {
        this.tokenHash = tokenHash;
    }

    public void setPinHashPhc(String pinHashPhc) {
        this.pinHashPhc = pinHashPhc;
    }

    public void setMaxUses(Integer maxUses) {
        this.maxUses = maxUses;
    }

    public void setUsesCount(int usesCount) {
        this.usesCount = usesCount;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setAcceptedBy(User acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public void setLastUsedAt(LocalDateTime lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }

    public void setUsages(List<InvitationUsage> usages) {
        this.usages = (usages != null ? new ArrayList<>(usages) : Collections.emptyList());
    }

    // --- Helpers métier
    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && now != null && expiresAt.isBefore(now);
    }

    public boolean isExpiredNow() {
        return isExpired(LocalDateTime.now());
    }

    public boolean isSingleUse() {
        return maxUses != null && maxUses == 1;
    }

    public Optional<Integer> remainingUses() {
        if (maxUses == null)
            return Optional.empty();
        return Optional.of(Math.max(0, maxUses - usesCount));
    }

    public boolean hasRemainingUses() {
        return remainingUses().map(v -> v > 0).orElse(true);
    }

    public boolean isActive(LocalDateTime now) {
        return !isRevoked() && !isExpired(now) && hasRemainingUses();
    }

    public List<InvitationUsage> getUsagesMostRecentFirst() {
        if (usages == null)
            return Collections.emptyList();
        return usages.stream()
                .sorted(Comparator.comparing(InvitationUsage::getUsedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())).reversed())
                .collect(Collectors.toList());
    }

    // --- Builder
    public static class Builder {
        private Long id;
        private InvitationType type;
        private String label;
        private String note;
        private String constraintsJson;
        private OrgRole role;
        private String email;
        private Person person;
        private byte[] tokenHash;
        private String pinHashPhc;
        private Integer maxUses;
        private int usesCount;
        private LocalDateTime expiresAt;
        private LocalDateTime revokedAt;
        private User createdBy;
        private LocalDateTime createdAt;
        private User acceptedBy;
        private LocalDateTime acceptedAt;
        private LocalDateTime lastUsedAt;
        private List<InvitationUsage> usages;

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withType(InvitationType type) {
            this.type = type;
            return this;
        }

        public Builder withLabel(String label) {
            this.label = label;
            return this;
        }

        public Builder withNote(String note) {
            this.note = note;
            return this;
        }

        public Builder withConstraintsJson(String constraintsJson) {
            this.constraintsJson = constraintsJson;
            return this;
        }

        public Builder withRole(OrgRole role) {
            this.role = role;
            return this;
        }

        public Builder withEmail(String email) {
            this.email = email;
            return this;
        }

        public Builder withPerson(Person person) {
            this.person = person;
            return this;
        }

        public Builder withTokenHash(byte[] tokenHash) {
            this.tokenHash = tokenHash;
            return this;
        }

        public Builder withPinHashPhc(String pinHashPhc) {
            this.pinHashPhc = pinHashPhc;
            return this;
        }

        public Builder withMaxUses(Integer maxUses) {
            this.maxUses = maxUses;
            return this;
        }

        public Builder withUsesCount(int usesCount) {
            this.usesCount = usesCount;
            return this;
        }

        public Builder withExpiresAt(LocalDateTime expiresAt) {
            this.expiresAt = expiresAt;
            return this;
        }

        public Builder withRevokedAt(LocalDateTime revokedAt) {
            this.revokedAt = revokedAt;
            return this;
        }

        public Builder withCreatedBy(User createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withAcceptedBy(User acceptedBy) {
            this.acceptedBy = acceptedBy;
            return this;
        }

        public Builder withAcceptedAt(LocalDateTime acceptedAt) {
            this.acceptedAt = acceptedAt;
            return this;
        }

        public Builder withLastUsedAt(LocalDateTime lastUsedAt) {
            this.lastUsedAt = lastUsedAt;
            return this;
        }

        public Builder withUsages(List<InvitationUsage> usages) {
            this.usages = usages;
            return this;
        }

        public Invitation build() {
            return new Invitation(this);
        }
    }

    // --- equals/hashCode sur id uniquement
    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Invitation))
            return false;
        Invitation that = (Invitation) o;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    @Override
    public String toString() {
        return "Invitation{" +
                "id=" + id +
                ", type=" + type +
                ", role='" + role + '\'' +
                ", email='" + email + '\'' +
                ", maxUses=" + maxUses +
                ", usesCount=" + usesCount +
                ", expiresAt=" + expiresAt +
                ", revokedAt=" + revokedAt +
                ", createdBy=" + (createdBy != null ? createdBy.getId() : null) +
                ", acceptedBy=" + (acceptedBy != null ? acceptedBy.getId() : null) +
                ", usages=" + (usages != null ? usages.size() : 0) + " items" +
                '}';
    }
}
