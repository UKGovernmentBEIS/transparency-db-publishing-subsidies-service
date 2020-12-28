package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ValidationErrorResult {

	public String row;
	public String columns;
	public String errorMessages;
	
	public ValidationErrorResult(String errorMessages) {
		this.errorMessages=errorMessages;
		
	}
}
