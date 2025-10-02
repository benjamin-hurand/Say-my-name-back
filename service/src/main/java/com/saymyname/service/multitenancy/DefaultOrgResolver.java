package com.saymyname.service.multitenancy;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DefaultOrgResolver {
    private final JdbcTemplate jdbc;

    public DefaultOrgResolver(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long forUser(Long userId) {
        Long orgId = jdbc.query(
                "SELECT organization_id FROM user_organizations WHERE user_id=? ORDER BY created_at LIMIT 1",
                ps -> ps.setLong(1, userId),
                rs -> rs.next() ? rs.getLong(1) : null);
        if (orgId != null)
            return orgId;
        // fallback : l’org "default"
        return jdbc.queryForObject("SELECT id FROM organizations WHERE `key`='default' LIMIT 1", Long.class);
    }
}
