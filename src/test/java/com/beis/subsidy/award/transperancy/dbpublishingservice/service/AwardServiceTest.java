package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.UserPrinciple;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

public class AwardServiceTest {

	private static final String CONSTANT_SYSTEM = "SYSTEM";

	@InjectMocks
	private AwardService awardServiceMock;

	private final AwardRepository awardRepository = mock(AwardRepository.class);
	private BeneficiaryRepository beneficiaryRepository = mock(BeneficiaryRepository.class);
	private final GrantingAuthorityRepository grepo = mock(GrantingAuthorityRepository.class);
	private SubsidyMeasureRepository smRepository = mock(SubsidyMeasureRepository.class);
	private final AdminProgramRepository adminProgramRepository = mock(AdminProgramRepository.class);

	BulkUploadAwards bulkUploadAward;

	UserPrinciple userPrinciple;

	SingleAward awardInputRequest;

	Award mockAward;

	GrantingAuthority grantingAuthority;

	@BeforeEach
	public void setUp() throws Exception {

		awardInputRequest = new SingleAward();
		// awardInputRequest.setRow(2);
		awardInputRequest.setSubsidyControlNumber("SC10000");
		awardInputRequest.setSubsidyControlTitle("AHDB Generic Promotional Measures scheme");
		awardInputRequest.setSubsidyObjective("Environmental protection");
		awardInputRequest.setSubsidyInstrument("Loan");
		awardInputRequest.setSubsidyAmountRange("500000 - 1000000");
		awardInputRequest.setSubsidyAmountExact("99.0");
		awardInputRequest.setNationalIdType("Charity number");
		awardInputRequest.setNationalId("12345678");
		awardInputRequest.setBeneficiaryName("pvk");
		awardInputRequest.setGrantingAuthorityName("BEIS");
		awardInputRequest.setLegalGrantingDate("19-02-1999");
		awardInputRequest.setOrgSize("Small organisation");
		awardInputRequest.setSpendingRegion("South East");
		awardInputRequest.setSpendingSector("10.Information and communication");
		awardInputRequest.setGoodsOrServices("Goods");
		awardInputRequest.setSpendingSector("10.Information and communication");
		awardInputRequest.setSubsidyObjectiveOther("abc");
		awardInputRequest.setSubsidyInstrumentOther("def");
		awardInputRequest.setSubsidyAwardInterest("");

		bulkUploadAward = new BulkUploadAwards();
		bulkUploadAward.setRow(2);
		bulkUploadAward.setSubsidyControlNumber("SC10000");
		bulkUploadAward.setSubsidyControlTitle("AHDB Generic Promotional Measures scheme");
		bulkUploadAward.setSubsidyObjective("SME support");
		bulkUploadAward.setSubsidyInstrument("Loan");
		bulkUploadAward.setSubsidyAmountRange("500000 - 1000000");
		bulkUploadAward.setSubsidyAmountExact("99.0");
		bulkUploadAward.setNationalIdType("Company Registration Number");
		bulkUploadAward.setNationalId("ab123456");
		bulkUploadAward.setBeneficiaryName("pvk");
		bulkUploadAward.setGrantingAuthorityName("BEIS");
		bulkUploadAward.setLegalGrantingDate("19-Feb-1999");
		bulkUploadAward.setOrgSize("Small organisation");
		bulkUploadAward.setSpendingRegion("South East");
		bulkUploadAward.setSpendingSector("10.Information and communication");
		bulkUploadAward.setGoodsOrServices("Goods");
		bulkUploadAward.setSubsidyObjectiveOther("abc");
		bulkUploadAward.setSubsidyInstrumentOther("def");

		userPrinciple = new UserPrinciple();
		userPrinciple.setUserName("Beis Admin");
		userPrinciple.setRole("Beis Administrator");
		userPrinciple.setPassword("password123");
		userPrinciple.setGrantingAuthorityGroupId(1);
		userPrinciple.setGrantingAuthorityGroupName("TEST GA");

		grantingAuthority = new GrantingAuthority();
		grantingAuthority.setGrantingAuthorityName("TEST GA");
		grantingAuthority.setStatus("Active");
		grantingAuthority.setGaId(26L);

		mockAward = new Award();
		mockAward.setAwardNumber(123L);
		mockAward.setGrantingAuthority(grantingAuthority);

		MockitoAnnotations.openMocks(this);

	}

	@Test
	public void testCreateAwards() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		String role = "Granting Authority Administrator";
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
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
		when(adminProgramRepository.findById(anyString()).orElse(null)).thenReturn(null);
		when(awardServiceMock.createAward(awardInputRequest,role)).thenReturn(expectedAward);
		Award award = awardServiceMock.createAward(awardInputRequest,role);
		assertNotNull(award);
		assertThat(award.getApprovedBy()).isEqualTo("test");
	}

	@Test
	public void testProcessAwards() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		AuditLogs auditLogs = mock(AuditLogs.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		awardList.add(bulkUploadAward);
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);
		Award expectedAward = new Award();
		Award saveAward = new Award();
		List<Award> savedAwardsList = new ArrayList<>();
		savedAwardsList.add(mockAward);
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		String role = "Granting Authority Administrator";
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(awardRepository.saveAll(any())).thenReturn(savedAwardsList);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);
		when(awardServiceMock.createAward(awardInputRequest,role)).thenReturn(expectedAward);
		when(adminProgramRepository.findById(anyString()).orElse(null)).thenReturn(null);
		List<Award> awards = awardServiceMock.processBulkAwards(awardList, role, userPrinciple);
		assertNotNull(awards);
	}

	@Test
	public void testProcessAwardsError() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		bulkUploadAward.setLegalGrantingDate("10-12-1999");
		bulkUploadAward.setSubsidyControlNumber("SC10000");

		String role = "Granting Authority Administrator";
		awardList.add(bulkUploadAward);
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);
		Award expectedAward = new Award();
		Award saveAward = new Award();
		List<Award> savedAwardsList = new ArrayList<>();
		savedAwardsList.add(mockAward);
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(awardRepository.saveAll(any())).thenReturn(savedAwardsList);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);
		when(awardServiceMock.createAward(awardInputRequest,role)).thenReturn(expectedAward);
		when(adminProgramRepository.findById(anyString()).orElse(null)).thenReturn(null);
		List<Award> awards = awardServiceMock.processBulkAwards(awardList,role,userPrinciple);
		assertNotNull(awards);

	}

	@Test
	public void testProcessAwardError() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		awardInputRequest.setLegalGrantingDate("10-Feb-1999");

		String role = "Granting Authority Administrator";
		awardList.add(bulkUploadAward);
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
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
		when(awardServiceMock.createAward(awardInputRequest,role)).thenReturn(expectedAward);
		when(adminProgramRepository.findById(anyString()).orElse(null)).thenReturn(null);
		Award award = awardServiceMock.createAward(awardInputRequest,role);
		assertNotNull(award);

	}

	@Test
	public void testValidationError() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		awardInputRequest.setNationalIdType("VAT Number");
		awardInputRequest.setNationalId("123");
		String role = "Granting Authority Administrator";
		awardList.add(bulkUploadAward);
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
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
		when(awardServiceMock.createAward(awardInputRequest, role)).thenReturn(expectedAward);
		when(adminProgramRepository.findById(anyString()).orElse(null)).thenReturn(null);
		Award award = awardServiceMock.createAward(awardInputRequest, role);
		assertNotNull(award);

	}

	@Test
	public void testUTRError() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		awardInputRequest.setNationalIdType("UTR Number");
		awardInputRequest.setNationalId("123");

		String role = "Granting Authority Administrator";
		awardList.add(bulkUploadAward);
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
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
		when(awardServiceMock.createAward(awardInputRequest,role)).thenReturn(expectedAward);
		when(adminProgramRepository.findById(anyString()).orElse(null)).thenReturn(null);
		Award award = awardServiceMock.createAward(awardInputRequest, role);
		assertNotNull(award);

	}

	@Test
	public void testNationalIdError() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		awardInputRequest.setNationalIdType("UTR Number");
		awardInputRequest.setNationalId("1234567899");
		String role = "Granting Authority Administrator";
		awardList.add(bulkUploadAward);
		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");
		smList.add(sub);
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
		when(awardServiceMock.createAward(awardInputRequest,role)).thenReturn(expectedAward);
		when(adminProgramRepository.findById(anyString()).orElse(null)).thenReturn(null);
		Award award = awardServiceMock.createAward(awardInputRequest, role);
		assertNotNull(award);

	}
}
