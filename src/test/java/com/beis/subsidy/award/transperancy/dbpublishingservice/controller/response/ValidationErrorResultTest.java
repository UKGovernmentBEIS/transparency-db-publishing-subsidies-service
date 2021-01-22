package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class ValidationErrorResultTest {

	@Test
	public void testvalidationErrorResult() {

		ValidationErrorResult validationErrorResult = new ValidationErrorResult("Test");

		validationErrorResult.setColumns("0");
		validationErrorResult.setErrorMessages("All Awards saved in Database");
		assertThat(validationErrorResult).isNotNull();
		assertThat(validationErrorResult.getErrorMessages()).isNotNull();

	}

	@Test
	public void testvalidationErrorResults() {

		ValidationErrorResult validationErrorResult = new ValidationErrorResult();

		validationErrorResult.setColumns("0");
		validationErrorResult.setErrorMessages("All Awards saved in Database");
		assertThat(validationErrorResult).isNotNull();
		assertThat(validationErrorResult.getErrorMessages()).isNotNull();

	}
}
