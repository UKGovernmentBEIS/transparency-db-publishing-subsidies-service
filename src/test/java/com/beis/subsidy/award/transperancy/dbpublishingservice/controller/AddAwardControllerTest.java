package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.SingleAwardValidationResults;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.AddAwardService;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class AddAwardControllerTest {

	@InjectMocks
	private AddAwardController addAwardController;

	AddAwardService addAwardServiceMock;

	SingleAwardValidationResults validationResult = new SingleAwardValidationResults();

	SingleAward awardInputRequest;

	@BeforeEach
	public void setUp() throws Exception {

		addAwardServiceMock = mock(AddAwardService.class);
		MockitoAnnotations.openMocks(this);
		awardInputRequest = new SingleAward();
		awardInputRequest.setSubsidyControlNumber("SC10001");
		awardInputRequest.setSubsidyControlTitle("test123");
		awardInputRequest.setSubsidyObjective("SME support");
		awardInputRequest.setSubsidyInstrument("Loan");
		awardInputRequest.setSubsidyAmountRange("50000-100000");
		awardInputRequest.setSubsidyAmountExact("10000");
		awardInputRequest.setNationalIdType("Charity number");
		awardInputRequest.setNationalId("123456");
		awardInputRequest.setBeneficiaryName("testSingleAward");
		awardInputRequest.setGrantingAuthorityName("BFI");
		awardInputRequest.setLegalGrantingDate("10-01-2019");
		awardInputRequest.setOrgSize("Small");
		awardInputRequest.setSpendingRegion("South East");
		awardInputRequest.setSpendingSector("10.Information and communication");
		awardInputRequest.setLegalGrantingDate("12-12-2019");

	}

	@Test
	public void testAddSubsidyAward() throws ParseException {
		final HttpStatus expectedHttpStatus = HttpStatus.OK;

		when(addAwardServiceMock.validateAward(awardInputRequest)).thenReturn(validationResult);
		addAwardController.addSubsidyAward(awardInputRequest);
		SingleAwardValidationResults validationResult = addAwardServiceMock.validateAward(awardInputRequest);
		assertThat(validationResult.totalErrors).isEqualTo(0);

	}

	@Test
	public void testAddSubsidyValidationAward() throws ParseException {

		awardInputRequest.setLegalGrantingDate("10-FEB-2019");
		awardInputRequest.setGrantingAuthorityName("BFI");
		awardInputRequest = null;

		addAwardController.addSubsidyAward(awardInputRequest);

		assertThat(validationResult.totalErrors).isEqualTo(0);

	}

}
