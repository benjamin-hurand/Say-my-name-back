package com.oxyl.persistence.repository;

import com.oxyl.persistence.entity.CompanyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    
    CompanyEntity findByCompanyName(String companyName);
}
