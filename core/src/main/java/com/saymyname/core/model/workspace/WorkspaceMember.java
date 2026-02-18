package com.saymyname.core.model.workspace;

import com.saymyname.core.model.enums.workspace.PersonLinkStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceMemberStatus;
import com.saymyname.core.model.enums.workspace.WorkspaceRole;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class WorkspaceMember {
    Long workspaceId;
    Long userId;
    WorkspaceRole role;
    WorkspaceMemberStatus status;
    String displayName;
    Instant createdAt;
    Long personId;
    PersonLinkStatus personLinkStatus;
    boolean canPickPerson;
    boolean canCreatePerson;
    boolean pickRequiresApproval;
    boolean createRequiresApproval;
    Long preferredEmailId;
}