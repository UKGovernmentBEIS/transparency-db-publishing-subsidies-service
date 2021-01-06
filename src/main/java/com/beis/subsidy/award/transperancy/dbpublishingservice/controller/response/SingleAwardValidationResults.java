package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SingleAwardValidationResults {
	
	public int totalErrors=0;
	private List<SingleAwardValidationResult> validationErrorResult;
	private String message;

}
