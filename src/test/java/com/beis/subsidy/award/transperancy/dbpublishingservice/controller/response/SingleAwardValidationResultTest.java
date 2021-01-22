package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SingleAwardValidationResultTest {

	@Test
	public void testSingleAwardValidationResult() {

		SingleAwardValidationResults singleAwardValidationResultTest = new SingleAwardValidationResults();

		SingleAwardValidationResult singleAwardValidationResult = new SingleAwardValidationResult();
		singleAwardValidationResultTest.setTotalErrors(0);
		singleAwardValidationResultTest.setMessage("Award saved in Database");
		List<SingleAwardValidationResult> validationErrorResult = new ArrayList<>();
		singleAwardValidationResultTest.setValidationErrorResult(validationErrorResult);

		assertThat(singleAwardValidationResultTest).isNotNull();
		assertThat(singleAwardValidationResultTest.getMessage()).isNotNull();
		assertThat(singleAwardValidationResultTest.getValidationErrorResult()).isNotNull();
		assertThat(singleAwardValidationResultTest.getTotalErrors()).isEqualTo(0);

	}

	@Test
	public void testSingleAwardValidationResults() {

		List<SingleAwardValidationResult> validationErrorResult = new ArrayList<>();

		SingleAwardValidationResults singleAwardValidationResultTest = new SingleAwardValidationResults(0,
				validationErrorResult, "test");

		singleAwardValidationResultTest.setTotalErrors(0);
		singleAwardValidationResultTest.setMessage("Award saved in Database");

		singleAwardValidationResultTest.setValidationErrorResult(validationErrorResult);

		assertThat(singleAwardValidationResultTest).isNotNull();
		assertThat(singleAwardValidationResultTest.getMessage()).isNotNull();
		assertThat(singleAwardValidationResultTest.getValidationErrorResult()).isNotNull();
		assertThat(singleAwardValidationResultTest.getTotalErrors()).isEqualTo(0);

	}
}
