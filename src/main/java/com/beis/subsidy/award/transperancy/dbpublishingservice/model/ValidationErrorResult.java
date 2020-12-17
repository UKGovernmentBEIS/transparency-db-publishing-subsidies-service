package com.beis.subsidy.award.transperancy.dbpublishingservice.model;

import java.util.List;

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
	

}
