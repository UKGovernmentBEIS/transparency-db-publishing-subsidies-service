package com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SingleAwardValidationResultTest {

	@Test
	public void testSingleAwardValidationResult() {

		SingleAwardValidationResults singleAwardValidationResultTest = new SingleAwardValidationResults();

		singleAwardValidationResultTest.setTotalErrors(0);
		singleAwardValidationResultTest.setMessage("Award saved in Database");
		singleAwardValidationResultTest.setAwardNumber("1234");
		List<SingleAwardValidationResult> validationErrorResult = new ArrayList<>();
		singleAwardValidationResultTest.setValidationErrorResult(validationErrorResult);

		assertThat(singleAwardValidationResultTest).isNotNull();
		assertThat(singleAwardValidationResultTest.getMessage()).isNotNull();
		assertThat(singleAwardValidationResultTest.getValidationErrorResult()).isNotNull();
		assertThat(singleAwardValidationResultTest.getTotalErrors()).isEqualTo(0);
		assertThat(singleAwardValidationResultTest.getAwardNumber()).isEqualTo("1234");

	}

	@Test
	public void testSingleAwardValidationLineResults() {

		List<SingleAwardValidationResult> validationErrorResult = new ArrayList<>();

		SingleAwardValidationResults singleAwardValidationResultTest = new SingleAwardValidationResults(0,
				validationErrorResult, "test", "1234");

		singleAwardValidationResultTest.setValidationErrorResult(validationErrorResult);

		assertThat(singleAwardValidationResultTest).isNotNull();
		assertThat(singleAwardValidationResultTest.getMessage()).isNotNull();
		assertThat(singleAwardValidationResultTest.getValidationErrorResult()).isNotNull();
		assertThat(singleAwardValidationResultTest.getTotalErrors()).isEqualTo(0);
		assertThat(singleAwardValidationResultTest.getAwardNumber()).isEqualTo("1234");

	}
}
