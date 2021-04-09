package com.beis.subsidy.award.transperancy.dbpublishingservice.repository;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;
import org.springframework.data.jpa.repository.JpaRepository;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;

import java.util.List;

public interface AwardRepository extends JpaRepository<Award, Long> {

    List<Award> findBySubsidyMeasure(SubsidyMeasure subsidyMeasure);
    Award findByAwardNumber(Long awardNumber);
}
