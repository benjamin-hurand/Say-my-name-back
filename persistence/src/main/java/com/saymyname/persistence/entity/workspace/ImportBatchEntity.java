package com.saymyname.persistence.entity.workspace;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import com.saymyname.persistence.entity.UserEntity;
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
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "import_batches", indexes = {
        @Index(name = "idx_imp_ws_status", columnList = "workspace_id,status,created_at")
})
public class ImportBatchEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_kind", nullable = false, length = 16, columnDefinition = "enum('CSV','XLSX','HTML','API','MANUAL')")
    private SourceKind sourceKind;

    @Column(name = "source_label", length = 255)
    private String sourceLabel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, foreignKey = @ForeignKey(name = "fk_import_batch_created_by"))
    private UserEntity createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16,
            columnDefinition = "enum('PENDING','MAPPING','REVIEW','APPLIED','FAILED') default 'PENDING'")
    private ImportBatchStatus status;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;

    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "workspace_id", nullable = false, foreignKey = @ForeignKey(name = "fk_import_batch_workspace"))
    private WorkspaceEntity workspace;

    public enum SourceKind {
        CSV,
        XLSX,
        HTML,
        API,
        MANUAL
    }

    public enum ImportBatchStatus {
        PENDING,
        MAPPING,
        REVIEW,
        APPLIED,
        FAILED
    }
}
