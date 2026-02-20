package com.saymyname.persistence.entity.workspace;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "import_mappings")
public class ImportMappingEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "batch_id", nullable = false, foreignKey = @ForeignKey(name = "fk_import_mapping_batch"))
    private ImportBatchEntity batch;

    @Column(name = "source_key", nullable = false, length = 255)
    private String sourceKey;

    @Column(name = "target_attr_id")
    private Long targetAttributeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transform_kind", nullable = false, length = 16,
            columnDefinition = "enum('NONE','SPLIT_NAME','REGEX','DATE_PARSE','LOWER','UPPER','TRIM','ENUM_MAP') default 'NONE'")
    private TransformKind transformKind;

    @Column(name = "transform_payload", columnDefinition = "json")
    private String transformPayload;

    public enum TransformKind {
        NONE,
        SPLIT_NAME,
        REGEX,
        DATE_PARSE,
        LOWER,
        UPPER,
        TRIM,
        ENUM_MAP
    }
}
