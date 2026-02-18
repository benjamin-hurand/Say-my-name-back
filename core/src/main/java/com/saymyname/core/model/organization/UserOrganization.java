package com.saymyname.core.model.organization;

import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.enums.PersonLinkStatus;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class UserOrganization {
    Long userId;
    Long organizationId;
    OrgRole role;
    String displayName;
    Instant createdAt;
    Long personId;
    PersonLinkStatus personLinkStatus;
    boolean canPickPerson;
    boolean canCreatePerson;
    boolean pickRequiresApproval;
    boolean createRequiresApproval;
    MembershipStatus status;
    Long preferredEmailId;
}