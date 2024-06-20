package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Beneficiary;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AwardRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.BeneficiaryRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.SubsidyMeasureRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;

public class BulkUploadAwardsServiceTest {

	private static final String CONSTANT_SYSTEM = "SYSTEM";

	@InjectMocks
	private BulkUploadAwardsService bulkAwardServiceMock;
	@InjectMocks
	private ExcelHelper excelHelperMock;

	private AwardService adServiceMock = mock(AwardService.class);

	private final AwardRepository awardRepository = mock(AwardRepository.class);
	private BeneficiaryRepository beneficiaryRepository = mock(BeneficiaryRepository.class);
	private final GrantingAuthorityRepository grepo = mock(GrantingAuthorityRepository.class);
	private SubsidyMeasureRepository smRepository = mock(SubsidyMeasureRepository.class);

	BulkUploadAwards bulkUploadAward;

	BulkUploadAwards bulkUploadAwardStandalone;


	String path = "src/test/resource/Bulk_Upload_Awards_Template_local_success.xlsx";
	String path_error = "src/test/resource/Bulk_Upload_Awards_Template_local_errors.xlsx";

	SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH);

	private InputStream inputStream;

	private InputStream inputStreams;

	@BeforeEach
	public void setUp() throws Exception {

		inputStream = new FileInputStream(path);
		inputStreams = new FileInputStream(path_error);

		bulkUploadAward = new BulkUploadAwards();
		bulkUploadAward.setRow(2);
		bulkUploadAward.setSubsidyControlNumber("SC10000");
		bulkUploadAward.setSubsidyControlTitle("AHDB Generic Promotional Measures scheme");
		bulkUploadAward.setStandaloneAward("No");
		bulkUploadAward.setSubsidyDescription("A description");
		bulkUploadAward.setSubsidyObjective("Environmental protection");
		bulkUploadAward.setSubsidyInstrument("Loan");
		bulkUploadAward.setSubsidyAwardInterest("");
		bulkUploadAward.setSubsidyAmountRange("500000-1000000");
		bulkUploadAward.setSubsidyAmountExact("99.0");
		bulkUploadAward.setNationalIdType("Company Registration Number");
		bulkUploadAward.setNationalId("ab123456");
		bulkUploadAward.setBeneficiaryName("pvk");
		bulkUploadAward.setGrantingAuthorityName("BEIS");
		bulkUploadAward.setLegalGrantingDate("19-Feb-1999");
		bulkUploadAward.setOrgSize("Small organisation");
		bulkUploadAward.setSpendingRegion("[\"South East\", \"North West\"]");
		bulkUploadAward.setSpendingSector("10.Information and communication");
		bulkUploadAward.setGoodsOrServices("Goods");
		bulkUploadAward.setSpecificPolicyObjective("");
		bulkUploadAward.setSubsidyObjectiveOther("abc");
		bulkUploadAward.setSubsidyInstrumentOther("def");

		bulkUploadAwardStandalone = new BulkUploadAwards();
		bulkUploadAwardStandalone.setRow(3);
		bulkUploadAwardStandalone.setSubsidyControlNumber("");
		bulkUploadAwardStandalone.setSubsidyControlTitle("");
		bulkUploadAwardStandalone.setStandaloneAward("Yes");
		bulkUploadAwardStandalone.setSubsidyDescription("A description");
		bulkUploadAwardStandalone.setAuthorityURL("test.co.uk");
		bulkUploadAwardStandalone.setAuthorityURLDescription("test.co.uk description");
		bulkUploadAwardStandalone.setSubsidyObjective("SME support+J3");
		bulkUploadAwardStandalone.setSubsidyInstrument("Tax measures (tax credit, or tax/duty exemption)");
		bulkUploadAwardStandalone.setSubsidyAwardInterest("Subsidies or Schemes of Particular Interest (SSoPI)");
		bulkUploadAwardStandalone.setSubsidyAmountRange("500000-1000000");
		bulkUploadAwardStandalone.setSubsidyAmountExact("99.0");
		bulkUploadAwardStandalone.setNationalIdType("Company Registration Number");
		bulkUploadAwardStandalone.setNationalId("ab123456");
		bulkUploadAwardStandalone.setBeneficiaryName("pvk");
		bulkUploadAwardStandalone.setGrantingAuthorityName("BEIS");
		bulkUploadAwardStandalone.setLegalGrantingDate("19-Feb-1999");
		bulkUploadAwardStandalone.setOrgSize("Small organisation");
		bulkUploadAwardStandalone.setSpendingRegion("South East");
		bulkUploadAwardStandalone.setSpendingSector("10.Information and communication");
		bulkUploadAwardStandalone.setGoodsOrServices("Goods");
		bulkUploadAwardStandalone.setSpecificPolicyObjective("policy objective text if standalone award is selected");
		bulkUploadAwardStandalone.setSubsidyObjectiveOther("abc");
		bulkUploadAwardStandalone.setSubsidyInstrumentOther("def");


		MockitoAnnotations.openMocks(this);

	}



	@Test
	public void testValidateStandaloneAward() throws ParseException, IOException {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"Bulk_Upload_Awards_Template_local_success.xlsx", "multipart/form-data", inputStream);
		ValidationResult result = new ValidationResult();
		result.setErrorRows(0);
		result.setTotalRows(2);
		List<ValidationErrorResult> validationErrorResult = new ArrayList<>();
		result.setMessage("All Awards saved in Database");
		result.setValidationErrorResult(validationErrorResult);
		BulkUploadAwards bulkAward = new BulkUploadAwards();
		Award saveAward = new Award();
		Award expectedAward = new Award();
		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();

		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");

		Date startDate = formatter.parse("01-01-1999");
		Date endDate = formatter.parse("31-12-1999");

		sub.setStartDate(startDate);
		sub.setEndDate(endDate);

		smList.add(sub);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);

		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		List<BulkUploadAwards> bulkUploadAwards = new ArrayList<>();
		bulkUploadAwards.add(bulkUploadAwardStandalone);
		List<Award> awards = new ArrayList();

		Award dbAward = new Award();
		dbAward.setAwardNumber(Long.valueOf(1));
		dbAward.setApprovedBy("system");
		awards.add(dbAward);
		List<SubsidyMeasure> submList = new ArrayList<>();
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		subsidy.setStartDate(startDate);
		subsidy.setEndDate(endDate);
		submList.add(subsidy);
		String role = "Granting Authority Administrator";
		when(adServiceMock.getAllSubsidyMeasures()).thenReturn(submList);
		when(adServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		ValidationResult results = bulkAwardServiceMock.validateFile(mockMultipartFile, role);

		assertThat(results.getErrorRows()).isEqualTo(result.getErrorRows());

	}

	@Test
	public void testValidateAwards() throws ParseException, IOException {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"Bulk_Upload_Awards_Template_local_success.xlsx", "multipart/form-data", inputStream);
		ValidationResult result = new ValidationResult();
		result.setErrorRows(0);
		result.setTotalRows(1);
		List<ValidationErrorResult> validationErrorResult = new ArrayList<>();
		result.setMessage("All Awards saved in Database");
		result.setValidationErrorResult(validationErrorResult);
		BulkUploadAwards bulkAward = new BulkUploadAwards();
		Award saveAward = new Award();
		Award expectedAward = new Award();
		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();

		SubsidyMeasure sub = new SubsidyMeasure();
		sub.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		sub.setScNumber("SC10000");

		Date startDate = formatter.parse("01-01-1999");
		Date endDate = formatter.parse("31-12-1999");

		sub.setStartDate(startDate);
		sub.setEndDate(endDate);

		smList.add(sub);
		GrantingAuthority ga = new GrantingAuthority();
		ga.setGaId(Long.valueOf(1));
		ga.setGrantingAuthorityName("BEIS");
		gaList.add(ga);

		when(beneficiaryRepository.save(beneficiary)).thenReturn(beneficiary);
		when(awardRepository.save(saveAward)).thenReturn(expectedAward);
		when(grepo.findAll()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);

		List<BulkUploadAwards> bulkUploadAwards = new ArrayList<>();
		bulkUploadAwards.add(bulkUploadAward);
		List<Award> awards = new ArrayList();

		Award dbAward = new Award();
		dbAward.setAwardNumber(Long.valueOf(1));
		dbAward.setApprovedBy("system");
		awards.add(dbAward);
		List<SubsidyMeasure> submList = new ArrayList<>();
		SubsidyMeasure subsidy = new SubsidyMeasure();
		subsidy.setSubsidyMeasureTitle("AHDB Generic Promotional Measures scheme");
		subsidy.setScNumber("SC10000");
		subsidy.setStartDate(startDate);
		subsidy.setEndDate(endDate);
		submList.add(subsidy);
		String role = "Granting Authority Administrator";
		when(adServiceMock.getAllSubsidyMeasures()).thenReturn(submList);
		when(adServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);

		ValidationResult results = bulkAwardServiceMock.validateFile(mockMultipartFile, role);

		assertThat(results.getErrorRows()).isEqualTo(result.getErrorRows());

	}

	@Test
	public void testValidateError() throws ParseException, IOException {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"Bulk_Upload_Awards_Template_local_success.xlsx", "multipart/form-data", inputStream);
		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		bulkUploadAward.setLegalGrantingDate("10-12-1999");
		bulkUploadAward.setSubsidyControlNumber("SC10000");
		bulkUploadAward.setNationalId("abc123456");

		ValidationResult result = new ValidationResult();
		result.setErrorRows(4);
		result.setTotalRows(2);
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
		ValidationResult results = bulkAwardServiceMock.validateFile(mockMultipartFile, role);

		assertThat(results.getErrorRows()).isEqualTo(result.getErrorRows());

	}

	@Test
	public void testValidateBeneficiaryError() throws ParseException, IOException {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"Bulk_Upload_Awards_Template_local_success.xlsx", "multipart/form-data", inputStream);
		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		bulkUploadAward.setNationalIdType("VAT Number");
		bulkUploadAward.setBeneficiaryName(null);
		bulkUploadAward.setNationalId("abc123456");
		String role = "Granting Authority Administrator";
		ValidationResult result = new ValidationResult();
		result.setErrorRows(4);
		result.setTotalRows(2);

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
		ValidationResult results = bulkAwardServiceMock.validateFile(mockMultipartFile,role);

		assertThat(results.getErrorRows()).isEqualTo(result.getErrorRows());

	}

	@Test
	public void testValidateVatError() throws ParseException, IOException {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"Bulk_Upload_Awards_Template_local_errors.xlsx", "multipart/form-data", inputStreams);
		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		bulkUploadAward.setNationalIdType("VAT Number");
		bulkUploadAward.setBeneficiaryName(null);
		bulkUploadAward.setNationalId("123456");
		String role = "Granting Authority Administrator";
		ValidationResult result = new ValidationResult();
		result.setErrorRows(41);
		result.setTotalRows(2);

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
		when(adServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);
		ValidationResult results = bulkAwardServiceMock.validateFile(mockMultipartFile, role);

		assertThat(results.getErrorRows()).isEqualTo(result.getErrorRows());

	}

	@Test
	public void testValidateUTRError() throws ParseException, IOException {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"Bulk_Upload_Awards_Template_local_errors.xlsx", "multipart/form-data", inputStreams);
		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		bulkUploadAward.setNationalIdType("UTR Number");
		bulkUploadAward.setBeneficiaryName(null);
		bulkUploadAward.setNationalId("123456");

		ValidationResult result = new ValidationResult();
		result.setErrorRows(41);
		result.setTotalRows(2);

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
		when(adServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);
		ValidationResult results = bulkAwardServiceMock.validateFile(mockMultipartFile,role);

		assertThat(results.getErrorRows()).isEqualTo(result.getErrorRows());

	}

	@Test
	public void testValidateFormatError() throws ParseException, IOException {

		MockMultipartFile mockMultipartFile = new MockMultipartFile("file",
				"Bulk_Upload_Awards_Template_local_errors.xlsx", "multipart/form-data", inputStreams);
		Beneficiary beneficiary = mock(Beneficiary.class);
		List<GrantingAuthority> gaList = new ArrayList<GrantingAuthority>();
		List<SubsidyMeasure> smList = new ArrayList<>();
		List<BulkUploadAwards> awardList = new ArrayList<>();
		bulkUploadAward.setNationalIdType("Company Registration Number");
		bulkUploadAward.setBeneficiaryName(null);
		bulkUploadAward.setNationalId("123456");

		ValidationResult result = new ValidationResult();
		result.setErrorRows(41);
		result.setTotalRows(2);

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
		when(adServiceMock.getAllGrantingAuthorities()).thenReturn(gaList);
		when(smRepository.findAll()).thenReturn(smList);
		ValidationResult results = bulkAwardServiceMock.validateFile(mockMultipartFile, role);

		assertThat(results.getErrorRows()).isEqualTo(result.getErrorRows());

	}

}
