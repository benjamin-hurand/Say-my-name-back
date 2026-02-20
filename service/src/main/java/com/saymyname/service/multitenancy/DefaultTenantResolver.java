package com.saymyname.service.multitenancy;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DefaultTenantResolver {

    private final JdbcTemplate jdbc;

    public DefaultTenantResolver(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public Long forUser(Long userId) {
        return jdbc.query(
                "SELECT tenant_id FROM user_organizations WHERE user_id=? ORDER BY created_at LIMIT 1",
                ps -> ps.setLong(1, userId),
                rs -> rs.next() ? rs.getLong(1) : null);
    }
}
