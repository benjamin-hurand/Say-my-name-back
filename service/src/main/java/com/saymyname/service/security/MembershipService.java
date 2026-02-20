package com.saymyname.service.security;

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import com.saymyname.core.model.enums.OrgRole;

import java.util.List;
import java.util.Map;

@Service
public class MembershipService {
    private final NamedParameterJdbcTemplate tpl;

    public MembershipService(NamedParameterJdbcTemplate tpl) {
        this.tpl = tpl;
    }

    public boolean hasAtLeast(Long userId, Long tenantId, OrgRole required) {
        String sql = "SELECT role FROM user_organizations WHERE user_id=:u AND tenant_id=:t";
        List<String> roles = tpl.query(sql, Map.of("u", userId, "t", tenantId), (rs, i) -> rs.getString(1));
        if (roles.isEmpty())
            return false;
        OrgRole actual = OrgRole.valueOf(roles.get(0));
        return actual.ordinal() >= required.ordinal();
    }
}
