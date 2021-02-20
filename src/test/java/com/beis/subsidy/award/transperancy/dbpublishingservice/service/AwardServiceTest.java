package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;

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

public class AwardServiceTest {

	private static final String CONSTANT_SYSTEM = "SYSTEM";

	@InjectMocks
	private AwardService awardServiceMock;

	private final AwardRepository awardRepository = mock(AwardRepository.class);
	private BeneficiaryRepository beneficiaryRepository = mock(BeneficiaryRepository.class);
	private final GrantingAuthorityRepository grepo = mock(GrantingAuthorityRepository.class);
	private SubsidyMeasureRepository smRepository = mock(SubsidyMeasureRepository.class);

	BulkUploadAwards bulkUploadAward;

	SingleAward awardInputRequest;

	@BeforeEach
	public void setUp() throws Exception {

		awardInputRequest = new SingleAward();
		// awardInputRequest.setRow(2);
		awardInputRequest.setSubsidyControlNumber("SC10000");
		awardInputRequest.setSubsidyControlTitle("AHDB Generic Promotional Measures scheme");
		awardInputRequest.setSubsidyObjective("SME support");
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
		when(awardServiceMock.createAward(awardInputRequest,role)).thenReturn(expectedAward);
		Award award = awardServiceMock.createAward(awardInputRequest,role);
		assertNotNull(award);
		assertThat(award.getApprovedBy()).isEqualTo("test");
	}

	@Test
	public void testProcessAwards() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
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
		beneficiary.setBeneficiaryName("testName");
		expectedAward.setApprovedBy("test");
		String role = "Granting Authority Administrator";
		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);
		when(awardServiceMock.createAward(awardInputRequest,role)).thenReturn(expectedAward);
		List<Award> awards = awardServiceMock.processBulkAwards(awardList,role);
		assertNotNull(awards);

	}

	@Test
	public void testProcessAwardsError() throws ParseException {

		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		bulkUploadAward.setLegalGrantingDate("10-12-1999");
		bulkUploadAward.setSubsidyControlNumber("SC1000000");

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
		List<Award> awards = awardServiceMock.processBulkAwards(awardList,role);
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
		Award award = awardServiceMock.createAward(awardInputRequest, role);
		assertNotNull(award);

	}
}
