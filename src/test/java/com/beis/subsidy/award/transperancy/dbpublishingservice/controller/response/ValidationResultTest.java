package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class ValidationResultTest {

	@Test
	public void testValidationResultTest() {

		ValidationResult validationErrorResult = new ValidationResult();
		List<ValidationErrorResult> validationErrorResults = new ArrayList<>();

		validationErrorResult.setTotalRows(0);
		validationErrorResult.setErrorRows(0);
		validationErrorResult.setMessage("Award saved");
		validationErrorResult.setValidationErrorResult(validationErrorResults);
		assertThat(validationErrorResult).isNotNull();
		assertThat(validationErrorResult.getTotalRows()).isEqualTo(0);
		assertThat(validationErrorResult.getMessage()).isEqualTo("Award saved");
		assertThat(validationErrorResult.getErrorRows()).isEqualTo(0);
		assertThat(validationErrorResult.getValidationErrorResult()).isNotNull();

	}
}
