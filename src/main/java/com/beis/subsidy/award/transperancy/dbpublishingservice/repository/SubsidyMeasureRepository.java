package com.beis.subsidy.award.transperancy.dbpublishingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;

import java.util.List;

public interface SubsidyMeasureRepository extends JpaRepository<SubsidyMeasure, String> {

    SubsidyMeasure findByScNumber(String scNumber);

    List<SubsidyMeasure> findBySubsidyMeasureTitle(String subsidyControlTitle);
}
