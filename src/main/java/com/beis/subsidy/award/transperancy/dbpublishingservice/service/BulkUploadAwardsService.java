package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.BulkUploadAwardsController;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BulkUploadAwardsService {

	@Autowired
	private AwardService awardService;

	/*
	 * the below method validate the excel file passed in request.
	 */
	public ValidationResult validateFile(MultipartFile file) {

		try {

			// Read Excel file
			List<BulkUploadAwards> bulkUploadAwards = ExcelHelper.excelToAwards(file.getInputStream());

			bulkUploadAwards.stream().forEach(System.out::println);
			log.info("Back from Excel to awards...printed list of awards - end");

			// Validation National Id length check
			List<ValidationErrorResult> nationalIdMisingList = validateNationaIdAwards(bulkUploadAwards);

			List<ValidationErrorResult> nationalIdTypeMisingList = validateNationalIdType(bulkUploadAwards);

			// Validation - Beneficiary check
			List<ValidationErrorResult> beneficiaryMisingList = validateBeneficiaryAwards(bulkUploadAwards);

			// TODO - Validation - Make call to process API to get data for subsidy measure
			/*
			 * 3) If incorrect SC number is entered, user system should throw an error
			 * Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> scNumberNameCheckList = validateScNumberScTitle(bulkUploadAwards);

			List<ValidationErrorResult> subsidyControlNumberLengthList = validateSubsidyNumberLength(bulkUploadAwards);

			List<ValidationErrorResult> subsidyControlNumberMismatchList = validateSubsidyControlNumber(
					bulkUploadAwards);

			List<ValidationErrorResult> subsidyMeasureTitleNameLengthList = validateSubsidyMeasureNameLength(
					bulkUploadAwards);

			List<ValidationErrorResult> subsidyPurposeCheckList = validateSubsidyPurpose(bulkUploadAwards);

			/*
			 * validate benificiary name length > 255 Validation Error - Row 6 - Incorrect
			 * SC Number - Correct one SC10002
			 */

			List<ValidationErrorResult> beneficiaryNameErrorList = validateBeneficiaryName(bulkUploadAwards);

			/*
			 *  validate Granting Authority name length > 255 Validation Error - Row 6 -
			 * Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> grantingAuthorityNameErrorList = validateGrantingAuthorityName(
					bulkUploadAwards);

			/*
			 * validate Granting Authority exists in database or not Validation Error - Row
			 * 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> grantingAuthorityErrorList = validateGrantingAuthorityNameinDb(
					bulkUploadAwards);
			
			List<ValidationErrorResult> SubsidyElementFullAmountErrorList = validateSubsidyElementFullAmount(
					bulkUploadAwards);
			
			 
			/*
			 * validate Size of Organization
			 * 
			 */
			List<ValidationErrorResult> sizeOfOrgErrorList = validateSizeOfOrg(bulkUploadAwards);
			/*
			 * validate Spending Region
			 * 
			 */
			List<ValidationErrorResult> spendingRegionErrorList = validateSpendingRegion(bulkUploadAwards);
			/*
			 * validate Spending Sector
			 * 
			 */
			List<ValidationErrorResult> spendingSectorErrorList = validateSpendingSector(bulkUploadAwards);

			/*
			 * validate Goods or Service
			 * 
			 */

			List<ValidationErrorResult> goodsOrServiceErrorList = validateGoodsOrService(bulkUploadAwards);
			
			/*
			 * validate Legal granting Date
			 * 
			 */

			List<ValidationErrorResult> legalGrantingDateErrorList = validateLegalGrantingDate(bulkUploadAwards);
			
			/*
			 * validate Goods or Service
			 * 
			 */

			List<ValidationErrorResult> SubsidyInstrumentErrorList = validateSubsidyInstrument(bulkUploadAwards);
			
			// Merge lists of Validation Errors
			List<ValidationErrorResult> validationErrorResultList = Stream
					.of(scNumberNameCheckList, subsidyMeasureTitleNameLengthList, subsidyPurposeCheckList,
							nationalIdTypeMisingList, nationalIdMisingList, beneficiaryNameErrorList,
							beneficiaryMisingList, subsidyControlNumberLengthList, subsidyControlNumberMismatchList,
							grantingAuthorityNameErrorList, grantingAuthorityErrorList, sizeOfOrgErrorList,
							spendingRegionErrorList, spendingSectorErrorList, goodsOrServiceErrorList,SubsidyInstrumentErrorList,legalGrantingDateErrorList,SubsidyElementFullAmountErrorList)
					.flatMap(x -> x.stream()).collect(Collectors.toList());

			log.info("Final bulk awards list ...printed list of awards - ****");
			bulkUploadAwards.stream().forEach(System.out::println);

			log.info("Final validation errors list ...printing list of errors - start");
			validationErrorResultList.stream().forEach(System.out::println);

			ValidationResult validationResult = new ValidationResult();
			validationResult.setValidationErrorResult(validationErrorResultList);
			validationResult.setTotalRows(bulkUploadAwards.size());
			validationResult.setErrorRows(validationErrorResultList.size());
			validationResult.setMessage((validationErrorResultList.size() > 0) ? "Validation Errors in Uploaded file"
					: "No errors in Uploaded file");

			log.info("Final validation Result object ...printing validationResult - start");
			validationErrorResultList.stream().forEach(System.out::println);

			if (validationResult.getValidationErrorResult().size() == 0) {
				// if(true) {
				
				System.out.println("No validation error in bulk excel template");
				log.info("No validation error in bulk excel template");

				awardService.processBulkAwards(bulkUploadAwards);

				log.info("After calling process api - response = ");
				// validationResult.setMessage(
				// (response.getStatusCode().equals(HttpStatus.CREATED)) ? "All Awards saved in
				// Database" : "Error while saving awards in Database" );
				validationResult
						.setMessage((true ? "All Awards saved in Database" : "Error while saving awards in Database"));
			}

			return validationResult;

		} catch (IOException e) {
			e.printStackTrace();
			
			log.error(" Error in validationResult **** "+e);
			throw new RuntimeException("Fail to store data : " + e.getMessage());
		}

	}

	/*
	 * 
	 * the below method validate either SC number or Sc Title exist in the file.
	 */
	private List<ValidationErrorResult> validateScNumberScTitle(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for eaither Sc number or Sc Title must be exist in the input file.
		 */

		List<BulkUploadAwards> ScNumberScTitleErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (((award.getSubsidyControlNumber() == null
						|| StringUtils.isEmpty(award.getSubsidyControlNumber()))
						&& (award.getSubsidyControlTitle() == null
								|| StringUtils.isEmpty(award.getSubsidyControlTitle())))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationScNumberScTitlResultList = new ArrayList<>();
		validationScNumberScTitlResultList = ScNumberScTitleErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E",
						"Either Subsidy Control number or Subsidy title field is mandatory."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Either Subsidy Control number or Subsidy title should enter = "
				+ validationScNumberScTitlResultList);

		return validationScNumberScTitlResultList;
	}

	/*
	 * 
	 * the below method validate Size of Organization entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSizeOfOrg(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for Size of Organization entered in the input file.
		 */

		List<BulkUploadAwards> sizeOfOrgErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getOrgSize() == null || StringUtils.isEmpty(award.getOrgSize()))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationSizeOfOrgErrorListResultList = new ArrayList<>();
		validationSizeOfOrgErrorListResultList = sizeOfOrgErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "L",
						"Size of Organization  field is mandatory."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Size of Organizationshould enter = "
				+ validationSizeOfOrgErrorListResultList);

		return validationSizeOfOrgErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Subsidy Purpose entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSubsidyPurpose(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for Size of Organization entered in the input file.
		 */
		
		List<BulkUploadAwards> subsidyPurposeErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getSubsidyObjective() == null || StringUtils.isEmpty(award.getSubsidyObjective()))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationSubsidyObjectiveErrorListResultList = new ArrayList<>();
		validationSubsidyObjectiveErrorListResultList = subsidyPurposeErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "C",
						"Subsidy Objective  field is mandatory."))
				.collect(Collectors.toList());
		
		
		List<BulkUploadAwards> subsidyPurposeOtherErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getSubsidyObjectiveOther() .length() > 255)))
				.collect(Collectors.toList());
		if(subsidyPurposeOtherErrorRecordsList.size()>0) {
		
		validationSubsidyObjectiveErrorListResultList = subsidyPurposeOtherErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "D",
						"Subsidy Objective- other field lenth is > 255 characters"))
				.collect(Collectors.toList());
		}

		log.info("Validation Result Error list - validateSubsidyObjective = "
				+ validationSubsidyObjectiveErrorListResultList);

		return validationSubsidyObjectiveErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Spending Region entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSpendingRegion(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for Size of SpendingRegion entered in the input file.
		 */

		List<BulkUploadAwards> spendingRegionErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getSpendingRegion() == null || StringUtils.isEmpty(award.getSpendingRegion()))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationspendingRegionErrorListResultList = new ArrayList<>();
		validationspendingRegionErrorListResultList = spendingRegionErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "P",
						"Spending Region  field is mandatory."))
				.collect(Collectors.toList());

		List<BulkUploadAwards> spendingRegionOtherErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSpendingRegion().length() > 255))).collect(Collectors.toList());
		
		if(spendingRegionOtherErrorRecordsList.size()>0) {
		
		validationspendingRegionErrorListResultList = spendingRegionOtherErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "P",
						"Spending Region other  field length > 255 charactres."))
				.collect(Collectors.toList());
		}
		
		log.info("Validation Result Error list - Spending Region should enter = "
				+ validationspendingRegionErrorListResultList);

		return validationspendingRegionErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Spending sector entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSpendingSector(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for Size of Spending Sector entered in the input file.
		 */

		List<BulkUploadAwards> spendingSectorErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getSpendingSector() == null || StringUtils.isEmpty(award.getSpendingSector()))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationspendingSectorErrorListResultList = new ArrayList<>();
		validationspendingSectorErrorListResultList = spendingSectorErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "Q",
						"Spending Sector  field is mandatory."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Spending Sector  should enter = "
				+ validationspendingSectorErrorListResultList);

		return validationspendingSectorErrorListResultList;
	}
	
	
	/*
	 * 
	 * the below method validate Subsidy Amount field.
	 */
	private List<ValidationErrorResult> validateSubsidyElementFullAmount(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for SubsidyElementFullAmount entered in the input file.
		 */

		List<BulkUploadAwards> subsidyAmountExactErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> (((award.getSubsidyInstrument()!=null && !award.getSubsidyInstrument().startsWith("Tax"))&& (award.getSubsidyAmountExact() == null || StringUtils.isEmpty(award.getSubsidyAmountExact())))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationSubsidyAmountExactErrorResultList = new ArrayList<>();
		validationSubsidyAmountExactErrorResultList = subsidyAmountExactErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "H",
						"Subsidy Element Full Amount is mandatory."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Subsidy Element Full Amount = "
				+ validationSubsidyAmountExactErrorResultList);

		return validationSubsidyAmountExactErrorResultList;
	}
	
	
	/*
	 * 
	 * the below method validate SubsidyInstrument .
	 */
	private List<ValidationErrorResult> validateSubsidyInstrument(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for SubsidyInstrument mandatory check .
		 */

		List<BulkUploadAwards> SubsidyInstrumentErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyInstrument()== null || StringUtils.isEmpty(award.getSubsidyInstrument())))).collect(Collectors.toList());
		
		List<ValidationErrorResult> validationSubsidyInstrumentErrorListResultList = new ArrayList<>();
		validationSubsidyInstrumentErrorListResultList = SubsidyInstrumentErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E",
						"Subsidy Instrument is Mandatory."))
				.collect(Collectors.toList());
		
		List<BulkUploadAwards> SubsidyInstrumentOtherErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyInstrumentOther()!=null && award.getSubsidyInstrumentOther().length() > 255))).collect(Collectors.toList());
		
		if(SubsidyInstrumentOtherErrorRecordsList.size() > 0) {
		validationSubsidyInstrumentErrorListResultList = SubsidyInstrumentOtherErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "F",
						"Subsidy Instrument-other length > 255 characters."))
				.collect(Collectors.toList());
		}
		List<BulkUploadAwards> SubsidyInstrumentTaxErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyInstrument()!=null && award.getSubsidyInstrument().startsWith("Tax"))&& (award.getSubsidyAmountRange()==null || StringUtils.isEmpty(award.getSubsidyAmountRange())))).collect(Collectors.toList());
		
		if(SubsidyInstrumentTaxErrorRecordsList.size() > 0) {
		validationSubsidyInstrumentErrorListResultList = SubsidyInstrumentTaxErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "G",
						"Subsidy Element Full Amount Range is mandatory when Subsidy Instrument is Tax Measure ."))
				.collect(Collectors.toList());
		}
		log.info("Validation Result Error list - Subsidy Instrument "
				+ validationSubsidyInstrumentErrorListResultList);

		return validationSubsidyInstrumentErrorListResultList;
	}
 
	/*
	 * 
	 * the below method validate Goods or Service entered or not in the file.
	 */
	private List<ValidationErrorResult> validateGoodsOrService(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for GoodsOrService entered in the input file.
		 */

		List<BulkUploadAwards> goodsOrServiceErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getGoodsOrServices() == null || StringUtils.isEmpty(award.getGoodsOrServices()))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationgoodsOrServiceErrorListResultList = new ArrayList<>();
		validationgoodsOrServiceErrorListResultList = goodsOrServiceErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "O",
						"Goods or Service  field is mandatory."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Goods or Service should enter = "
				+ validationgoodsOrServiceErrorListResultList);

		return validationgoodsOrServiceErrorListResultList;
	}

	
	
	/*
	 * 
	 * the below method validate Granting date .
	 */
	private List<ValidationErrorResult> validateLegalGrantingDate(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for Legal Granting Date  entered in the input file.
		 */

		List<BulkUploadAwards> legalGrantingDateErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getLegalGrantingDate() == null || StringUtils.isEmpty(award.getLegalGrantingDate()))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationlegalGrantingDateErrorListResultList = new ArrayList<>();
		validationlegalGrantingDateErrorListResultList = legalGrantingDateErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "N",
						"Legal Granting Date is Mandatory."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Legal Granting Date is Mandatory = "
				+ validationlegalGrantingDateErrorListResultList);

		return validationlegalGrantingDateErrorListResultList;
	}
	
	
	private List<ValidationErrorResult> validateSubsidyControlNumber(List<BulkUploadAwards> bulkUploadAwards) {

		log.info("Calling awardService.getAllSubsidyMeasures()... - start");
		
		List<SubsidyMeasure> smList = awardService.getAllSubsidyMeasures();
		
		log.info("Calling processServiceproxy.getAllSubsidyMeasures()... - end");

		List<String> subsidyControlNumberTitleList = smList.stream().map(sm -> sm.getScNumber())
				.collect(Collectors.toList());

		log.info("subsidyControlNumberTitleList - String list " + subsidyControlNumberTitleList);
		//
		
		List<BulkUploadAwards> subsidyControlNumberErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getSubsidyControlNumber() != null && award.getSubsidyControlTitle() != null
						&& !subsidyControlNumberTitleList.contains(award.getSubsidyControlNumber()))
				.collect(Collectors.toList());

		
		subsidyControlNumberErrorRecordsList.stream().forEach(System.out::println);
		log.info(
				"Back validation-3 - subsidy measure title mismatch check...printing list of awards with subsidy measure number error - end");

		// validation scnumber with sctitle.

		List<BulkUploadAwards> subsidyControlNumberExistsList = bulkUploadAwards.stream()
				.filter(requestAward -> !StringUtils.isEmpty(requestAward.getSubsidyControlNumber())
						&& !StringUtils.isEmpty(requestAward.getSubsidyControlTitle()))
				.collect(Collectors.toList());

		subsidyControlNumberExistsList.stream().forEach(System.out::println);
		List<BulkUploadAwards> subsidyControlNumberWithNameErrorRecordsList = subsidyControlNumberExistsList.stream()
				.filter(requestAward -> smList.stream().noneMatch(
						bulkAward -> ((bulkAward.getScNumber().equals(requestAward.getSubsidyControlNumber()))
								&& (bulkAward.getSubsidyMeasureTitle().equals(requestAward.getSubsidyControlTitle())))))
				.collect(Collectors.toList());

		
		List<ValidationErrorResult> validationSubsidyControlNumberResultList = new ArrayList<>();
		validationSubsidyControlNumberResultList = subsidyControlNumberErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "B",
						"Subsidy Control doest not exists."))
				.collect(Collectors.toList());

		validationSubsidyControlNumberResultList = subsidyControlNumberWithNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "B",
						"Subsidy Control number does not match with title."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Subsidy Measure Number mismatch error = "
				+ validationSubsidyControlNumberResultList);

		return validationSubsidyControlNumberResultList;
	}

	/*
	 * the below method validate the nationalId
	 */

	private List<ValidationErrorResult> validateNationalIdType(List<BulkUploadAwards> bulkUploadAwards) {

		List<BulkUploadAwards> beneficiaryMissingErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getNationalIdType() == null))).collect(Collectors.toList());

		List<ValidationErrorResult> validationNationalIdTypeResultList = new ArrayList<>();
		validationNationalIdTypeResultList = beneficiaryMissingErrorRecordsList.stream().map(
				award -> new ValidationErrorResult(String.valueOf(award.getRow()), "I", "National Id Type is mandatory."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - National ID  missing error = " + validationNationalIdTypeResultList);

		return validationNationalIdTypeResultList;
	}

	private List<ValidationErrorResult> validateBeneficiaryAwards(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * 2) If the ‘National ID type’ is a UTR or a VAT number, then validate if the
		 * beneficiary name is entered and if not return an error as above. Validation
		 * Error - Row 9 - Beneficiary missing
		 * 
		 */
		// TODO - Validation 2 - National ID Type UTR/ VAT, then check beneficiary
		// present - implement filter method
		List<BulkUploadAwards> beneficiaryMissingErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (((award.getNationalIdType()!=null) && (award.getNationalIdType().equals("UTR Number")
						|| award.getNationalIdType().equals("VAT Number")))
						&& (award.getBeneficiaryName() == null)))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationBeneficiaryIdResultList = new ArrayList<>();
		validationBeneficiaryIdResultList = beneficiaryMissingErrorRecordsList.stream().map(
				award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E", "Enter the Beneficiary Name."))
				.collect(Collectors.toList());

		log.info(
				"Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);

		return validationBeneficiaryIdResultList;
	}

	/*
	 *
	 * the below method validate the benificiary name length (>255 chars)
	 */
	private List<ValidationErrorResult> validateBeneficiaryName(List<BulkUploadAwards> bulkUploadAwards) {

		List<BulkUploadAwards> beneficiaryNameErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getBeneficiaryName()!=null && award.getBeneficiaryName().length() > 255))).collect(Collectors.toList());

		List<ValidationErrorResult> validationBeneficiaryIdResultList = new ArrayList<>();
		validationBeneficiaryIdResultList = beneficiaryNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "K",
						"Beneficiary name is too long, it should be 255 characters or fewer"))
				.collect(Collectors.toList());

		log.info(
				"Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);

		return validationBeneficiaryIdResultList;
	}

	/*
	 *
	 * the below method validate the AuthorityName name length (>255 chars)
	 */
	private List<ValidationErrorResult> validateGrantingAuthorityName(List<BulkUploadAwards> bulkUploadAwards) {

		List<BulkUploadAwards> validateGrantingAuthorityNameErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getGrantingAuthorityName()!=null && award.getGrantingAuthorityName().length() > 255))).collect(Collectors.toList());

		List<ValidationErrorResult> validationGrantingAuthorityResultList = new ArrayList<>();
		validationGrantingAuthorityResultList = validateGrantingAuthorityNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "H",
						"Please enter a valid Granting Authority"))
				.collect(Collectors.toList());
		
		List<BulkUploadAwards> validateGrantingAuthorityMissingErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (award.getGrantingAuthorityName()==null)).collect(Collectors.toList());
		if(validateGrantingAuthorityMissingErrorRecordsList.size()>0) {
		validationGrantingAuthorityResultList = validateGrantingAuthorityMissingErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "H",
						"Granting Authority name is Mandatory"))
				.collect(Collectors.toList());
		}

		log.info("Validation Result Error list - Granting Authority missing error = "
				+ validationGrantingAuthorityResultList);

		return validationGrantingAuthorityResultList;
	}

	/*
	 *
	 * the below method validate the subsidy Measure Title length (>255 chars)
	 */
	private List<ValidationErrorResult> validateSubsidyMeasureNameLength(List<BulkUploadAwards> bulkUploadAwards) {

		List<BulkUploadAwards> validateSubsidyMeasureNameErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyControlTitle().length() > 255))).collect(Collectors.toList());

		List<ValidationErrorResult> validationSubsidyMeasureNameResultList = new ArrayList<>();
		validationSubsidyMeasureNameResultList = validateSubsidyMeasureNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E",
						"Subsidy Measure Title must be 255 characters or fewer "))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Subsidy Measure Title must be 255 characters or fewer = "
				+ validationSubsidyMeasureNameResultList);

		return validationSubsidyMeasureNameResultList;
	}

	/*
	 *
	 * the below method validate the Subsidy number length (>7 chars)
	 */
	private List<ValidationErrorResult> validateSubsidyNumberLength(List<BulkUploadAwards> bulkUploadAwards) {

		List<BulkUploadAwards> validateSubsidyNumberLengthErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyControlNumber().length() > 7))).collect(Collectors.toList());

		List<ValidationErrorResult> validationSubsidyNumberLengthResultList = new ArrayList<>();
		validationSubsidyNumberLengthResultList = validateSubsidyNumberLengthErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E",
						"Subsidy Control Number must be 7 characters or fewer"))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Beneficiary Name missing error = "
				+ validationSubsidyNumberLengthResultList);

		return validationSubsidyNumberLengthResultList;
	}

	/*
	 *
	 * the below method validate the AuthorityName name exist in data base or not
	 * (table=Granting_Authority)
	 */
	private List<ValidationErrorResult> validateGrantingAuthorityNameinDb(List<BulkUploadAwards> bulkUploadAwards) {

		log.info("Calling processServiceproxy.getAllGrantingAuthorities()... - start");
		
		List<GrantingAuthority> grantingAuthorityList = awardService.getAllGrantingAuthorities();
		
		log.info("smList = " + grantingAuthorityList);
		log.info("Calling processServiceproxy.getAllSubsidyMeasures()... - end");

		List<String> grantingAuthorityNamesList = grantingAuthorityList.stream()
				.map(grantingAuthority -> grantingAuthority.getGrantingAuthorityName()).collect(Collectors.toList());

		log.info("Granting Authority - String list " + grantingAuthorityNamesList);

		List<BulkUploadAwards> grantingAuthorityNameErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getGrantingAuthorityName() != null
						&& !grantingAuthorityNamesList.contains(award.getGrantingAuthorityName()))
				.collect(Collectors.toList());

		log.info("Back validation-6 - Granting Authority Name  measure check...with data base table  error - start");
		grantingAuthorityNameErrorRecordsList.stream().forEach(System.out::println);
		log.info(
				"Back validation-6 - Granting Authority Name  measure check...with data base table  error - start - end");

		List<ValidationErrorResult> validationGrantingAuthorityNameResultList = new ArrayList<>();
		validationGrantingAuthorityNameResultList = grantingAuthorityNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "H",
						"Please enter a valid Granting Authority."))
				.collect(Collectors.toList());

		log.info("Validation Result Error list - Granting Authority Name error = "
				+ validationGrantingAuthorityNameResultList);

		return validationGrantingAuthorityNameResultList;
	}

	private List<ValidationErrorResult> validateNationaIdAwards(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * 1) National ID MUST consist of 8 numbers or 2 letters followed by 6 numbers
		 * e.g. 12345678, AB123456 Validation Error - Row 5 - National ID less than 8
		 * char
		 * 
		 * 2) If the ‘National ID type’ is a UTR or a VAT number, then validate if the
		 * beneficiary name is entered and if not return an error as above. Validation
		 * Error - Row 9 - Beneficiary missing
		 * 
		 */

		// TODO - Validation 1 - National ID length check - implement filter method
		List<BulkUploadAwards> nationsIdErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalId().length() > 10).collect(Collectors.toList());
		
		List<BulkUploadAwards> nationsIdMissingRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalId()==null || StringUtils.isEmpty(award.getNationalId())).collect(Collectors.toList());

		log.info(
				"Back validation-1 - national ID length check...printing list of awards with nation id length error - start");
		nationsIdErrorRecordsList.stream().forEach(System.out::println);
		log.info(
				"Back validation-1 - national ID length check...printing list of awards with nation id length error - end");

		List<ValidationErrorResult> validationNationalIdResultList = new ArrayList<>();
		validationNationalIdResultList = nationsIdErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
						"National ID  must be 10 characters or fewer."))
				.collect(Collectors.toList());
		
		if(nationsIdMissingRecordsList.size()>0) {
			validationNationalIdResultList = nationsIdMissingRecordsList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
							"National ID  is Mandatory."))
					.collect(Collectors.toList());
		}
		
		List<BulkUploadAwards> nationsIdVATErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("VAT Number") && (award.getNationalId().length() > 9 || !award.getNationalId().matches("[0-9]+"))).collect(Collectors.toList());
		
		if(nationsIdVATErrorRecordsList.size()>0) {
		validationNationalIdResultList = nationsIdVATErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
						"invalid VAT number."))
				.collect(Collectors.toList());
		}
		
		List<BulkUploadAwards> nationsIdUTRErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("UTR Number") && !award.getNationalId().matches("[0-9]+")).collect(Collectors.toList());
		
		if(nationsIdUTRErrorRecordsList.size()>0) {
		validationNationalIdResultList = nationsIdUTRErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
						"invalid UTR number."))
				.collect(Collectors.toList());
		}

		log.info("Validation Result Error list - National ID missing error = " + validationNationalIdResultList);

		return validationNationalIdResultList;

	}

	public List<Award> getallAwards() {

		return awardService.getallAwards();

	}

}
