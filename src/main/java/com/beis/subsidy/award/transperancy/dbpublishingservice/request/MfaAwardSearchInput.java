package com.beis.subsidy.award.transperancy.dbpublishingservice.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 
 * Search Input object - represents search input criteria for subsidy scheme service
 *
 */
@NoArgsConstructor
@Setter
@Getter
public class MfaAwardSearchInput {
	private String searchName;
	private Integer pageNumber;
	private Integer totalRecordsPerPage;
	private String[] sortBy;
	private String status;
}
