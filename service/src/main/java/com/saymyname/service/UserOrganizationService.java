package com.saymyname.service;

import com.saymyname.core.model.enums.OrgRole;
import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.persistence.dao.organization.UserOrganizationDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserOrganizationService {

    private final UserOrganizationDao dao;

    public UserOrganizationService(UserOrganizationDao dao) {
        this.dao = dao;
    }

    /** Récupère toutes les organisations d’un utilisateur avec son rôle */
    public List<UserOrganization> getOrganizationsForUser(Long userId) {
        return dao.findByUserId(userId);
    }

    public Optional<OrgRole> findRoleForCurrentOrg(Long userId) {
        if (userId == null)
            return Optional.empty();
        return dao.findRoleForCurrentOrg(userId);
    }
}
