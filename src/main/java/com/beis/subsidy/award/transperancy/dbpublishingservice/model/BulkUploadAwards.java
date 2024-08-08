package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BulkUploadAwards {
	
	private int row;
	private String column;
	private String subsidyControlTitle;
	private String subsidyControlNumber;
	private String adminProgramNumber;
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
	private String standaloneAward;
	private String subsidyDescription;
	private String specificPolicyObjective;
	private String authorityURL;
	private String authorityURLDescription;
	private String subsidyAwardInterest;
	private String spei;
}
