package com.saymyname.persistence.entity.workspace;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
@ToString
@Embeddable
public class WorkspaceAttributeId implements Serializable {

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "attribute_id", nullable = false)
    private Long attributeId;
}
