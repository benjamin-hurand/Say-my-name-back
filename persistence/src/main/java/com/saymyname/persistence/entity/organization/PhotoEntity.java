package com.saymyname.persistence.entity.organization;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.core.model.enums.PhotoStatus;
import com.saymyname.persistence.entity.UserEntity;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
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
@Table(name = "photos", uniqueConstraints = {
        @UniqueConstraint(name = "uq_photos_tenant_id", columnNames = {"tenant_id", "id"})
}, indexes = {
        @Index(name = "idx_photos_person", columnList = "person_id"),
        @Index(name = "idx_photos_status", columnList = "status"),
        @Index(name = "idx_photos_approved_by", columnList = "approved_by"),
        @Index(name = "idx_photos_tenant_person", columnList = "tenant_id,person_id")
})
public class PhotoEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "storage_key", nullable = false, length = 255)
    private String storageKey;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16, columnDefinition = "varchar(16) default 'PENDING'")
    private PhotoStatus status;

    @Column(name = "submitted_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", foreignKey = @ForeignKey(name = "fk_photos_submitted_by"))
    private UserEntity submittedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by", foreignKey = @ForeignKey(name = "fk_photos_approved_by"))
    private UserEntity approvedBy;
}
