package com.saymyname.persistence.entity.organization.people;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.core.model.enums.EmailKind;
import com.saymyname.core.model.enums.EmailSourceKind;
import com.saymyname.persistence.entity.organization.PersonEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "person_emails", uniqueConstraints = {
        @UniqueConstraint(name = "uq_pe_person_email", columnNames = {"person_id", "email"})
}, indexes = {
        @Index(name = "idx_pe_tenant_person", columnList = "tenant_id,person_id")
})
public class PersonEmailEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false, length = 16)
    private EmailKind kind;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_kind", nullable = false, length = 16)
    private EmailSourceKind sourceKind;

    @Column(name = "source_label", length = 255)
    private String sourceLabel;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "bounced_at")
    private LocalDateTime bouncedAt;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false,
            columnDefinition = "datetime default current_timestamp on update current_timestamp")
    private LocalDateTime updatedAt;

    // Backward-compatible relation-style accessors for legacy DAO/repository code.
    @Transient
    public PersonEntity getPerson() {
        if (personId == null) {
            return null;
        }
        PersonEntity p = PersonEntity.builder().build();
        p.setId(personId);
        p.setOrganizationId(getOrganizationId());
        return p;
    }

    public void setPerson(PersonEntity person) {
        if (person == null) {
            this.personId = null;
            return;
        }
        this.personId = person.getId();
        if (person.getOrganizationId() != null) {
            setOrganizationId(person.getOrganizationId());
        }
    }
}
