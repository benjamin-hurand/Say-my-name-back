package com.saymyname.core.model.organization;

import com.saymyname.core.model.enums.MembershipStatus;
import com.saymyname.core.model.enums.OrgRole;
import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class OrgMemberRow {
    Long userId;
    Long organizationId;
    String displayName;
    String email;
    OrgRole role;
    Long personId;
    String personLabel;
    MembershipStatus status;
    Instant joinedAt;
}