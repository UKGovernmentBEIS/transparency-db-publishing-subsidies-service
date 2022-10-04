package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleAward {
	
	private String awardNumber;
	private String standaloneAward;
	private String subsidyAwardDescription;
	private String subsidyControlTitle;
	private String subsidyControlNumber;
	private String nationalIdType;
	private String nationalId;
	private String beneficiaryName;
	private String orgSize;
	private String subsidyInstrument;
	private String subsidyObjective;
	private String subsidyAmountRange;
	private String subsidyAmountExact;
	private String legalGrantingDate;
	private String publishedAwardDate;
	private String grantingAuthorityName;
	private String goodsOrServices;
	private String spendingRegion;
	private String spendingSector;
	private String subsidyObjectiveOther;
	private String subsidyInstrumentOther;
	private String status;
	private String reason;
}
