package com.saymyname.persistence.entity.organization.course;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import com.saymyname.core.model.enums.course.CourseTargetScope;
import com.saymyname.core.model.enums.PopulationScope;
import com.saymyname.core.model.enums.course.CourseStatus;
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
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "courses", uniqueConstraints = {
                @UniqueConstraint(name = "uq_courses_tenant_id", columnNames = { "tenant_id", "id" })
}, indexes = {
                @Index(name = "idx_courses_tenant_user", columnList = "tenant_id,user_id"),
                @Index(name = "idx_courses_tenant_status", columnList = "tenant_id,status"),
                @Index(name = "idx_courses_tenant_target_attr", columnList = "tenant_id,target_attribute_id"),
                @Index(name = "idx_courses_tenant_user_attr", columnList = "tenant_id,user_id,target_attribute_id"),
                @Index(name = "idx_courses_tenant_user_status", columnList = "tenant_id,user_id,status")
})
public class CourseEntity extends BaseTenantScoped {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_courses_user"))
        private UserEntity user;

        @Enumerated(EnumType.STRING)
        @Column(name = "target_scope", nullable = false, length = 32, columnDefinition = "varchar(32) default 'ATTRIBUTE'")
        private CourseTargetScope targetScope;

        @Column(name = "target_attribute_id")
        private Long targetAttributeId;

        @Enumerated(EnumType.STRING)
        @Column(name = "status", nullable = false, length = 12)
        private CourseStatus status;

        @Column(name = "current_round", nullable = false)
        private int currentRound;

        @Enumerated(EnumType.STRING)
        @Column(name = "population_scope", nullable = false, length = 32)
        private PopulationScope populationScope;

        @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
        private LocalDateTime createdAt;

        @Column(name = "updated_at", nullable = false, columnDefinition = "datetime default current_timestamp on update current_timestamp")
        private LocalDateTime updatedAt;

        @Column(name = "last_accessed_at")
        private LocalDateTime lastAccessedAt;
}
