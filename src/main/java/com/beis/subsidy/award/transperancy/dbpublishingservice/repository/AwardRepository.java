package com.beis.subsidy.award.transperancy.dbpublishingservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;

public interface AwardRepository extends JpaRepository<Award, Long> {

}
