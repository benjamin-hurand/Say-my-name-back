package com.saymyname.persistence.entity.organization;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.core.model.enums.ChangeAction;
import com.saymyname.core.model.enums.ChangeRequestItemStatus;
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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "change_request_items", indexes = {
        @Index(name = "idx_cri_request", columnList = "change_request_id"),
        @Index(name = "idx_cri_ws_request", columnList = "change_request_id"),
        @Index(name = "idx_cri_tenant", columnList = "tenant_id"),
        @Index(name = "idx_cri_fact", columnList = "fact_id")
})
public class ChangeRequestItemEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "change_request_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cri_change_request"))
    private ChangeRequestEntity changeRequest;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 16)
    private ChangeAction action;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fact_id", foreignKey = @ForeignKey(name = "fk_cri_fact"))
    private FactEntity fact;

    @Column(name = "proposed_value", length = 512)
    private String proposedValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "resolution_status", nullable = false, length = 16)
    private ChangeRequestItemStatus resolutionStatus;

    @Column(name = "resolution_comment", length = 512)
    private String resolutionComment;
}
