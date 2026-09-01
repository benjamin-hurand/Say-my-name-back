package com.saymyname.persistence.entity.organization;

import java.util.ArrayList;
import java.util.List;

import com.saymyname.persistence.multitenancy.BaseTenantScoped;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "persons", uniqueConstraints = {
                // Hibernate orders inherited composite joins identifier-first: (id, tenant_id).
                @UniqueConstraint(name = "uq_persons_id_tenant", columnNames = { "id", "tenant_id" })
}, indexes = {
                @Index(name = "idx_persons_tenant", columnList = "tenant_id")
})
public class PersonEntity extends BaseTenantScoped {

        @EqualsAndHashCode.Include
        @ToString.Include
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id", nullable = false)
        private Long id;

        @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        private List<FactEntity> facts = new ArrayList<>();

        @OneToMany(mappedBy = "person", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        private List<PhotoEntity> photos = new ArrayList<>();
}
