package com.saymyname.persistence.entity.workspace;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import com.saymyname.persistence.multitenancy.BaseTenantScoped;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@SuperBuilder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "workspace_attributes", indexes = {
        @Index(name = "idx_wa_ws", columnList = "workspace_id"),
        @Index(name = "idx_wa_tenant_attr", columnList = "tenant_id,attribute_id")
})
public class WorkspaceAttributeEntity extends BaseTenantScoped {

    @EqualsAndHashCode.Include
    @ToString.Include
    @EmbeddedId
    private WorkspaceAttributeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("workspaceId")
    @JoinColumn(name = "workspace_id", nullable = false, foreignKey = @ForeignKey(name = "fk_wa_workspace"))
    private WorkspaceEntity workspace;

    @Column(name = "is_enabled", nullable = false, columnDefinition = "tinyint(1) default 1")
    private boolean enabled;

    @Column(name = "display_order_override")
    private Integer displayOrderOverride;

    @Column(name = "filter_override")
    private Boolean filterOverride;

    @Column(name = "sort_override")
    private Boolean sortOverride;

    @Column(name = "required_override")
    private Boolean requiredOverride;

    @Column(name = "created_at", nullable = false, columnDefinition = "datetime default current_timestamp")
    private LocalDateTime createdAt;
}