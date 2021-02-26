package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.stereotype.Service;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.SingleAwardValidationResults;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Beneficiary;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AwardRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.BeneficiaryRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.SubsidyMeasureRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AddAwardServiceTest {

	private static final String CONSTANT_SYSTEM = "SYSTEM";

	@InjectMocks
	private AddAwardService addawardServiceMock;

	private AwardService awardServiceMock = mock(AwardService.class);

	SingleAward awardInputRequest;

	private final AwardRepository awardRepository = mock(AwardRepository.class);
	private BeneficiaryRepository beneficiaryRepository = mock(BeneficiaryRepository.class);
	private final GrantingAuthorityRepository grepo = mock(GrantingAuthorityRepository.class);
	private SubsidyMeasureRepository smRepository = mock(SubsidyMeasureRepository.class);

	@BeforeEach
	public void setUp() throws Exception {

		awardInputRequest = new SingleAward();
		awardInputRequest.setSubsidyControlNumber("SC10000");
		awardInputRequest.setSubsidyControlTitle("AHDB Generic Promotional Measures scheme");
		awardInputRequest.setSubsidyObjective("SME support");
		awardInputRequest.setSubsidyInstrument("Loan");
		awardInputRequest.setSubsidyAmountRange("500000 - 1000000");
		awardInputRequest.setSubsidyAmountExact("99.0");
		awardInputRequest.setNationalIdType("Company Registration Number");
		awardInputRequest.setNationalId("ab123456");
		awardInputRequest.setBeneficiaryName("pvk");
		awardInputRequest.setGrantingAuthorityName("BEIS");
		awardInputRequest.setLegalGrantingDate("19-02-1999");
		awardInputRequest.setOrgSize("Small organisation");
		awardInputRequest.setSpendingRegion("South East");
		awardInputRequest.setSpendingSector("10.Information and communication");
		awardInputRequest.setGoodsOrServices("Goods");
		awardInputRequest.setSubsidyObjectiveOther("abc");
		awardInputRequest.setSubsidyInstrumentOther("def");
		MockitoAnnotations.openMocks(this);
	}

	@Test
	public void testValidategAwards() throws ParseException {

		String role = "Granting Authority Administrator";
		List<SubsidyMeasure> submList = new ArrayList<>();
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		submList.add(subsidy);
		List<SubsidyMeasure> smList = new ArrayList<>();
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);

		Beneficiary beneficiary = mock(Beneficiary.class);
		Award saveAward = new Award();
		Award expectedAward = new Award();
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		SingleAwardValidationResults expectedResult = new SingleAwardValidationResults();
		expectedResult.setTotalErrors(1);
		expectedResult.setMessage("Award saved in Database");

		when(awardServiceMock.getAllSubsidyMeasures()).thenReturn(submList);

		when(awardServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		SingleAwardValidationResults results = addawardServiceMock.validateAward(awardInputRequest, role,null,null,"test");

		assertThat(results.getTotalErrors()).isEqualTo(expectedResult.getTotalErrors());

	}

	@Test
	public void testNationalIdError() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		awardInputRequest.setNationalIdType("UTR Number");
		awardInputRequest.setNationalId("12345678999");

		List<SubsidyMeasure> submList = new ArrayList<>();
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		submList.add(subsidy);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);
		Award expectedAward = new Award();
		Award saveAward = new Award();
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		SingleAwardValidationResults expectedResult = new SingleAwardValidationResults();
		expectedResult.setTotalErrors(2);
		expectedResult.setMessage("Award saved in Database");
		String role = "Granting Authority Administrator";
		when(awardServiceMock.getAllSubsidyMeasures()).thenReturn(submList);

		when(awardServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		SingleAwardValidationResults results = addawardServiceMock.validateAward(awardInputRequest, role,null,null,"test");

		assertThat(results.getTotalErrors()).isEqualTo(expectedResult.getTotalErrors());

	}

	@Test
	public void testNationalIdErrors() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		awardInputRequest.setNationalIdType("UTR Number");
		awardInputRequest.setNationalId("ab12345678");
		awardInputRequest.setBeneficiaryName(null);
		awardInputRequest.setSubsidyControlNumber("SC12345679");
		awardInputRequest.setGoodsOrServices(null);
		awardInputRequest.setSubsidyInstrument(null);
		String role = "Granting Authority Administrator";
		List<SubsidyMeasure> submList = new ArrayList<>();
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		submList.add(subsidy);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);
		Award expectedAward = new Award();
		Award saveAward = new Award();
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		SingleAwardValidationResults expectedResult = new SingleAwardValidationResults();
		expectedResult.setTotalErrors(6);
		expectedResult.setMessage("Award saved in Database");

		when(awardServiceMock.getAllSubsidyMeasures()).thenReturn(submList);

		when(awardServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		SingleAwardValidationResults results = addawardServiceMock.validateAward(awardInputRequest, role,null,null,"test");

		assertThat(results.getTotalErrors()).isEqualTo(expectedResult.getTotalErrors());

	}

	@Test
	public void testvalidateErrors() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		awardInputRequest.setNationalIdType(null);
		awardInputRequest.setNationalId(null);
		String role = "Granting Authority Administrator";
		List<SubsidyMeasure> submList = new ArrayList<>();
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		submList.add(subsidy);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);
		Award expectedAward = new Award();
		Award saveAward = new Award();
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		SingleAwardValidationResults expectedResult = new SingleAwardValidationResults();
		expectedResult.setTotalErrors(3);
		expectedResult.setMessage("Award saved in Database");

		when(awardServiceMock.getAllSubsidyMeasures()).thenReturn(submList);

		when(awardServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		SingleAwardValidationResults results = addawardServiceMock.validateAward(awardInputRequest,role,null,null,"test");

		assertThat(results.getTotalErrors()).isEqualTo(expectedResult.getTotalErrors());

	}

	@Test
	public void testvalidateAwardErrors() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		String role = "Granting Authority Administrator";
		awardInputRequest.setOrgSize(null);
		awardInputRequest.setNationalIdType("VAT Number");

		awardInputRequest.setNationalId("123456");

		List<SubsidyMeasure> submList = new ArrayList<>();
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		submList.add(subsidy);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);
		Award expectedAward = new Award();
		Award saveAward = new Award();
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		SingleAwardValidationResults expectedResult = new SingleAwardValidationResults();
		expectedResult.setTotalErrors(3);
		expectedResult.setMessage("Award saved in Database");

		when(awardServiceMock.getAllSubsidyMeasures()).thenReturn(submList);

		when(awardServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		SingleAwardValidationResults results = addawardServiceMock.validateAward(awardInputRequest,role,null,null,"test");

		assertThat(results.getTotalErrors()).isEqualTo(expectedResult.getTotalErrors());

	}

	@Test
	public void testvalidateUTRErrors() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();

		awardInputRequest.setOrgSize(null);
		awardInputRequest.setNationalIdType("UTR Number");

		awardInputRequest.setNationalId("123456");

		List<SubsidyMeasure> submList = new ArrayList<>();
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		submList.add(subsidy);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);
		Award expectedAward = new Award();
		Award saveAward = new Award();
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		SingleAwardValidationResults expectedResult = new SingleAwardValidationResults();
		expectedResult.setTotalErrors(3);
		expectedResult.setMessage("Award saved in Database");
		String role = "Granting Authority Administrator";

		when(awardServiceMock.getAllSubsidyMeasures()).thenReturn(submList);

		when(awardServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		SingleAwardValidationResults results = addawardServiceMock.validateAward(awardInputRequest,role,null,null,"test");

		assertThat(results.getTotalErrors()).isEqualTo(expectedResult.getTotalErrors());

	}

	@Test
	public void testvalidateCharityrrors() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();

		String role = "Granting Authority Administrator";
		awardInputRequest.setOrgSize(null);
		awardInputRequest.setNationalIdType("Charity Number");

		awardInputRequest.setNationalId("1234567899");

		List<SubsidyMeasure> submList = new ArrayList<>();
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		submList.add(subsidy);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);
		Award expectedAward = new Award();
		Award saveAward = new Award();
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		SingleAwardValidationResults expectedResult = new SingleAwardValidationResults();
		expectedResult.setTotalErrors(3);
		expectedResult.setMessage("Award saved in Database");

		when(awardServiceMock.getAllSubsidyMeasures()).thenReturn(submList);

		when(awardServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		SingleAwardValidationResults results = addawardServiceMock.validateAward(awardInputRequest, role,null,null,"test");

		assertThat(results.getTotalErrors()).isEqualTo(expectedResult.getTotalErrors());

	}

}
