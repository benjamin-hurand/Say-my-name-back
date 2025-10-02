package com.saymyname.persistence.dao.organization;

import com.saymyname.core.model.organization.UserOrganization;
import com.saymyname.persistence.entity.organization.UserOrganizationEntity;
import com.saymyname.persistence.mapper.organization.UserOrganizationEntityMapper;
import com.saymyname.persistence.repository.UserOrganizationRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@Transactional(readOnly = true)
public class UserOrganizationDao {

    private final UserOrganizationRepository repository;
    private final UserOrganizationEntityMapper mapper;

    public UserOrganizationDao(UserOrganizationRepository repository, UserOrganizationEntityMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<UserOrganization> findByUserId(Long userId) {
        List<UserOrganizationEntity> entities = repository.findByIdUserId(userId);
        return entities.stream().map(mapper::toModel).collect(Collectors.toList());
    }
}
