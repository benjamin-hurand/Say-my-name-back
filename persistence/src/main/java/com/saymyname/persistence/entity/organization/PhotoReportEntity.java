package com.saymyname.persistence.entity.organization;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.core.model.enums.PhotoReportReason;
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
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "photo_reports", indexes = {
        @Index(name = "idx_pr_person_created", columnList = "person_id,created_at"),
        @Index(name = "idx_pr_reported_by_created", columnList = "reported_by,created_at"),
        @Index(name = "idx_pr_reason_created", columnList = "reason_type,created_at"),
        @Index(name = "idx_pr_tenant", columnList = "tenant_id"),
        @Index(name = "idx_pr_tenant_person", columnList = "tenant_id,person_id")
})
public class PhotoReportEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "person_id", nullable = false)
    private Long personId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reported_by", nullable = false, foreignKey = @ForeignKey(name = "fk_pr_reported_by"))
    private UserEntity reportedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "reason_type", nullable = false, length = 50)
    private PhotoReportReason reasonType;

    @Column(name = "reason_text", length = 255)
    private String reasonText;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;
}
