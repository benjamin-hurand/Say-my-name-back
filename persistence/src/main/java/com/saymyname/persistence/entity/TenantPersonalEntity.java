package com.saymyname.persistence.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = true)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "tenant_personals", uniqueConstraints = {
                @UniqueConstraint(name = "uk_tenant_personal_owner", columnNames = { "owner_user_id" })
})
@PrimaryKeyJoinColumn(name = "tenant_id", foreignKey = @ForeignKey(name = "fk_tenant_personal_tenant"))
@DiscriminatorValue("PERSONAL")
public class TenantPersonalEntity extends TenantEntity {

        @ManyToOne(fetch = FetchType.LAZY, optional = false)
        @JoinColumn(name = "owner_user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_tenant_personal_owner"))
        private UserEntity ownerUser;
}