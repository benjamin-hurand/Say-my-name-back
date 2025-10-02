// com/saymyname/webapp/multitenancy/OrgHibernateFilter.java
package com.saymyname.webapp.multitenancy;

import com.saymyname.core.multitenancy.OrgContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
// ⚠️ CECI est le Filter servlet :
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

// On peut aussi importer l'API Hibernate Filter comme *type de variable*, pas comme interface à implémenter
import org.hibernate.Session;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class OrgHibernateFilter implements Filter {

    @PersistenceContext
    EntityManager em;

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        Session session = em.unwrap(Session.class);
        // Ici on utilise le type org.hibernate.Filter juste comme variable
        org.hibernate.Filter f = session.enableFilter("orgFilter");
        Long orgId = OrgContext.get();
        if (orgId != null) {
            f.setParameter("orgId", orgId);
        }
        try {
            chain.doFilter(req, res);
        } finally {
            session.disableFilter("orgFilter");
        }
    }
}
