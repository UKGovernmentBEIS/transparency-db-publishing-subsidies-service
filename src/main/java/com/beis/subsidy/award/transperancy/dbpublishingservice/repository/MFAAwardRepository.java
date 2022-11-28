package com.beis.subsidy.award.transperancy.dbpublishingservice.repository;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.MFAGrouping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface MFAAwardRepository extends JpaRepository<MFAAward, Long>, JpaSpecificationExecutor<MFAAward> {
    MFAAward findByMfaAwardNumber(long mfaAwardNumber);

    List<MFAAward> findByMfaGroupingNumber(String mfaGroupingNumber);
}
