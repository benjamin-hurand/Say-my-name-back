// src/main/java/com/saymyname/persistence/repository/ChangeRequestRepositoryCustom.java
package com.saymyname.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.saymyname.core.model.people.ChangeRequestListQuery;
import com.saymyname.persistence.entity.organization.ChangeRequestEntity;

public interface ChangeRequestRepositoryCustom {
    Page<ChangeRequestEntity> searchAdmin(ChangeRequestListQuery query, Pageable pageable);
}
