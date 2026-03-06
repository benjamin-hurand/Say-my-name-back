package com.saymyname.core.model.tenant;

import java.time.LocalDateTime;
import com.saymyname.core.model.enums.tenant.TenantKind;

public interface Tenant {

    Long getId();

    TenantKind getKind();

    LocalDateTime getCreatedAt();
}