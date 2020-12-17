package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 
 * Search Input object - represents search input criteria for public search service
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchInput {

	private String beneficiaryName;
	
	private String subsidyMeasureTitle;
	
	private String susidyObjective;
	
	private String spendingRegion;
	
}
