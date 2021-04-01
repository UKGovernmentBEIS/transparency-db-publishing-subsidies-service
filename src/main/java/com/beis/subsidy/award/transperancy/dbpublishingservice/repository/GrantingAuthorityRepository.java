package com.beis.subsidy.award.transperancy.dbpublishingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;

public interface GrantingAuthorityRepository extends JpaRepository<GrantingAuthority, Long> {

    /**
     * To get Granting authority details based on granting authority name
     * @param Name - Granting authority name
     * @return GrantingAuthority - Object of GrantingAuthority
     */
    GrantingAuthority findByGrantingAuthorityName(String Name);
}
