package com.beis.subsidy.award.transperancy.dbpublishingservice.repository;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MFAGroupingRepository extends JpaRepository<MFAGrouping, String>, JpaSpecificationExecutor<MFAGrouping> {
    MFAGrouping findByMfaGroupingNumber(String mfaGroupingNumber);

    List<MFAGrouping> findByMfaGroupingName(String mfaGroupingName);
}
