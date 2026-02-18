package com.saymyname.webapp.multitenancy;

import com.saymyname.core.multitenancy.TenantContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import org.hibernate.Session;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class TenantHibernateFilter implements Filter {

    @PersistenceContext
    EntityManager em;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        Session session = em.unwrap(Session.class);
        org.hibernate.Filter f = session.enableFilter("tenantFilter");
        Long tenantId = TenantContext.get();
        if (tenantId != null) {
            f.setParameter("tenantId", tenantId);
        }
        try {
            chain.doFilter(req, res);
        } finally {
            session.disableFilter("tenantFilter");
        }
    }
}
