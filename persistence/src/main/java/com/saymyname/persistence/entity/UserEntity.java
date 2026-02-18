package com.saymyname.persistence.entity;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder;
import com.saymyname.core.model.enums.SrsAlgorithm;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(name = "uk_users_public_id", columnNames = {"public_id"})
}, indexes = {
        @Index(name = "ix_users_display_name", columnList = "display_name")
})
public class UserEntity {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "public_id", nullable = false, columnDefinition = "binary(16)")
    private byte[] publicId;

    @Column(name = "display_name", nullable = true, length = 50)
    private String displayName;

    @Enumerated(EnumType.STRING)
    @Column(name = "srs_algorithm", nullable = false, length = 8, columnDefinition = "varchar(8) default 'SM2'")
    private SrsAlgorithm srsAlgorithm;

    @Column(name = "roles", nullable = true, length = 255)
    private String roles;

    @Column(name = "active", nullable = false, columnDefinition = "tinyint(1) default 0")
    private boolean active;

    @Column(name = "auth_version", nullable = false, columnDefinition = "int default 0")
    private int authVersion;

    @Column(name = "auth_updated_at", nullable = false,
            columnDefinition = "datetime default current_timestamp on update current_timestamp")
    private LocalDateTime authUpdatedAt;
}
