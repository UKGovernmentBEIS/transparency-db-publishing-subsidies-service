package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationResult;
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
	public ValidationResult validateFile(MultipartFile file, String role) {

		try {

			// Read Excel file
			List<BulkUploadAwards> bulkUploadAwards = ExcelHelper.excelToAwards(file.getInputStream());

			log.info("Back from Excel to awards...printed list of awards - end");

			// Validation National Id length check
			List<ValidationErrorResult> nationalIdMissingList = validateNationalIdAwards(bulkUploadAwards);

			List<ValidationErrorResult> nationalIdTypeMissingList = validateNationalIdType(bulkUploadAwards);


			/*
			 * 3) If incorrect SC number is entered, user system should throw an error
			 * Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> scNumberNameCheckList = validateScNumberScTitle(bulkUploadAwards);

			//List<ValidationErrorResult> scNumberStatusList = validateScNumberStatus(bulkUploadAwards,);

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
			List<ValidationErrorResult> grantingAuthorityErrorList = validateGrantingAuthorityNameInDb(
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
							nationalIdTypeMissingList, nationalIdMissingList, beneficiaryNameErrorList,
							subsidyControlNumberLengthList, subsidyControlNumberMismatchList,
							grantingAuthorityNameErrorList, grantingAuthorityErrorList, sizeOfOrgErrorList,
							spendingRegionErrorList, spendingSectorErrorList, goodsOrServiceErrorList,SubsidyInstrumentErrorList,legalGrantingDateErrorList,SubsidyElementFullAmountErrorList)
					.flatMap(x -> x.stream()).collect(Collectors.toList());

			log.info("Final validation errors list ...printing list of errors - start");

			ValidationResult validationResult = new ValidationResult();
			validationResult.setValidationErrorResult(validationErrorResultList);
			validationResult.setTotalRows(bulkUploadAwards.size());
			validationResult.setErrorRows(validationErrorResultList.size());
			validationResult.setMessage((validationErrorResultList.size() > 0) ? "Validation Errors in Uploaded file"
					: "No errors in Uploaded file");

			log.info("Final validation Result object ...printing validationResult - start");

			if (validationResult.getValidationErrorResult().size() == 0) {

				log.info("No validation error in bulk excel template");

				awardService.processBulkAwards(bulkUploadAwards,role);

				log.info("After calling process api - response = ");
				validationResult
						.setMessage((true ? "All Awards saved in Database" : "Error while saving awards in Database"));
			}

			return validationResult;

		} catch (IOException e) {
			log.error(" Error in validationResult **** ", e);
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "A",
						"You must enter either a subsidy control number or a subsidy scheme title."))
				.collect(Collectors.toList());

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
						"You must select an organisation size."))
				.collect(Collectors.toList());

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

		List<ValidationErrorResult> subsidyObjectiveErrorResultList = new ArrayList<>();
		subsidyObjectiveErrorResultList = subsidyPurposeErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "C",
						"Subsidy Objective  field is mandatory."))
				.collect(Collectors.toList());
		
		List<BulkUploadAwards> subsidyPurposeOthersErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getSubsidyObjective()!= null && ("Other".equalsIgnoreCase(award.getSubsidyObjective())
						&& (award.getSubsidyObjectiveOther()==null || StringUtils.isEmpty(award.getSubsidyObjectiveOther()))))))
				.collect(Collectors.toList());
		subsidyObjectiveErrorResultList.addAll(subsidyPurposeOthersErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "D",
						"Subsidy Objective- other field is mandatory when Subsidy Objective is Other"))
				.collect(Collectors.toList()));
		
		List<BulkUploadAwards> subsidyPurposeOtherErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getSubsidyObjectiveOther()!=null && award.getSubsidyObjectiveOther() .length() > 255)))
				.collect(Collectors.toList());
		if (subsidyPurposeOtherErrorRecordsList.size()>0) {

			subsidyObjectiveErrorResultList.addAll(subsidyPurposeOtherErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "D",
						"Subsidy Objective- other field length is > 255 characters"))
				.collect(Collectors.toList()));
		}

		log.info("Validation Result Error list - validateSubsidyObjective = "
				+ subsidyObjectiveErrorResultList);

		return subsidyObjectiveErrorResultList;
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
						"You must select the region that the recipient organisation is based in."))
				.collect(Collectors.toList());

		List<BulkUploadAwards> spendingRegionOtherErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSpendingRegion()!=null && award.getSpendingRegion().length() > 255))).collect(Collectors.toList());
		
		if(spendingRegionOtherErrorRecordsList.size()>0) {
		
		validationspendingRegionErrorListResultList = spendingRegionOtherErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "P",
						"Spending Region other  field length > 255 characters."))
				.collect(Collectors.toList());
		}
		
		log.info("Validation Result Error list - Spending Region should enter = " + validationspendingRegionErrorListResultList.size());

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
						"You must select the sector that the recipient organisation belongs to."))
				.collect(Collectors.toList());

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
				award -> (((award.getSubsidyInstrument()!=null && !award.getSubsidyInstrument().startsWith("Tax"))&&
						(award.getSubsidyAmountExact() == null || StringUtils.isEmpty(award.getSubsidyAmountExact())))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationSubsidyAmountExactErrorResultList = new ArrayList<>();
		validationSubsidyAmountExactErrorResultList = subsidyAmountExactErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "H",
						"For non-tax measure subsidies, enter 'N/A' in column G. You can enter the exact subsidy amount in column H."))
				.collect(Collectors.toList());
		List<BulkUploadAwards> subsidyAmountFormatErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> (((award.getSubsidyInstrument()!=null && !award.getSubsidyInstrument().startsWith("Tax"))&&
						(award.getSubsidyAmountExact()!=null && !ExcelHelper.isNumeric(award.getSubsidyAmountExact()) ))))
				.collect(Collectors.toList());

		if(subsidyAmountFormatErrorRecordsList.size()>0) {
			validationSubsidyAmountExactErrorResultList.addAll(subsidyAmountFormatErrorRecordsList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "H",
							"Subsidy Element Full Amount should be numeric."))
					.collect(Collectors.toList()));
			}
		
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
		
		List<BulkUploadAwards> SubsidyInstrumentOthersErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getSubsidyInstrument()!= null && ("Other".equalsIgnoreCase(award.getSubsidyInstrument()) && (award.getSubsidyInstrumentOther()==null || StringUtils.isEmpty(award.getSubsidyInstrumentOther()))))))
				.collect(Collectors.toList());
		validationSubsidyInstrumentErrorListResultList.addAll(SubsidyInstrumentOthersErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "F",
						"Subsidy Instrument-other field is mandatory when Subsidy Instrument is Other"))
				.collect(Collectors.toList()));
		
		List<BulkUploadAwards> SubsidyInstrumentOtherErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyInstrumentOther()!=null &&
						award.getSubsidyInstrumentOther().length() > 255))).collect(Collectors.toList());
		
		if(SubsidyInstrumentOtherErrorRecordsList.size() > 0) {
		validationSubsidyInstrumentErrorListResultList = SubsidyInstrumentOtherErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "F",
						"Subsidy Instrument-other length > 255 characters."))
				.collect(Collectors.toList());
		}
		List<BulkUploadAwards> SubsidyInstrumentTaxErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyInstrument()!=null && award.getSubsidyInstrument().startsWith("Tax"))&&
						(award.getSubsidyAmountRange()==null || StringUtils.isEmpty(award.getSubsidyAmountRange())))).collect(Collectors.toList());
		
		if(SubsidyInstrumentTaxErrorRecordsList.size() > 0) {
		validationSubsidyInstrumentErrorListResultList = SubsidyInstrumentTaxErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "G",
						"For tax measure subsidies, the amount must be 0. You can select a subsidy range instead in column G."))
				.collect(Collectors.toList());
		}

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
						"You must select what the recipient organisation provides. This will be either goods or services."))
				.collect(Collectors.toList());

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
						"You must enter the date that the subsidy was awarded."))
				.collect(Collectors.toList());
		
		List<BulkUploadAwards> legalGrantingDateFormatErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ("invalid".equalsIgnoreCase(award.getLegalGrantingDate())))
				.collect(Collectors.toList());
		validationlegalGrantingDateErrorListResultList.addAll(legalGrantingDateFormatErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "N",
						"You must enter the date that the subsidy was awarded in the following format: DD/MM/YYYY."))
				.collect(Collectors.toList()));

		List<SubsidyMeasure> smList = awardService.getAllSubsidyMeasures();

		List<BulkUploadAwards> subsidyControlNumberExistsList = bulkUploadAwards.stream()
				.filter(requestAward -> !StringUtils.isEmpty(requestAward.getSubsidyControlNumber()))
				.collect(Collectors.toList());

		List<BulkUploadAwards> subsidyControlTitleExistsNoNumberList = bulkUploadAwards.stream()
				.filter(requestAward -> StringUtils.isEmpty(requestAward.getSubsidyControlNumber())
						&& !StringUtils.isEmpty(requestAward.getSubsidyControlTitle()))
				.collect(Collectors.toList());

		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

		List<BulkUploadAwards> legalGrantingDateWithinMeasureDateNumberErrorRecordsList = subsidyControlNumberExistsList.stream()
				.filter(award -> smList.stream().anyMatch(
						sm -> {
							try {
								return ((award.getSubsidyControlNumber().equals(sm.getScNumber())) &&
										(sdf.parse(award.getLegalGrantingDate()).after(sm.getEndDate()) ||
												sdf.parse(award.getLegalGrantingDate()).before(sm.getStartDate())));
							} catch (ParseException e) {
								return true;
							}
						}
				)).collect(Collectors.toList());

		List<BulkUploadAwards> legalGrantingDateWithinMeasureDateTitleNoNumberErrorRecordsList = subsidyControlTitleExistsNoNumberList.stream()
				.filter(award -> smList.stream().anyMatch(
						sm -> {
							try {
								return ((award.getSubsidyControlTitle().equals(sm.getSubsidyMeasureTitle())) &&
										(sdf.parse(award.getLegalGrantingDate()).after(sm.getEndDate()) ||
												sdf.parse(award.getLegalGrantingDate()).before(sm.getStartDate())));
							} catch (ParseException e) {
								return true;
							}
						}
				)).collect(Collectors.toList());

		validationlegalGrantingDateErrorListResultList.addAll(legalGrantingDateWithinMeasureDateNumberErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "N",
						"The legal granting date of the subsidy award must be within the start and end dates of the associated subsidy scheme."))
				.collect(Collectors.toList()));

		validationlegalGrantingDateErrorListResultList.addAll(legalGrantingDateWithinMeasureDateTitleNoNumberErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "N",
						"The legal granting date of the subsidy award must be within the start and end dates of the associated subsidy scheme. Please add the SC number for this scheme as there may be multiple schemes with this title."))
				.collect(Collectors.toList()));


		return validationlegalGrantingDateErrorListResultList;
	}
	
	
	private List<ValidationErrorResult> validateSubsidyControlNumber(List<BulkUploadAwards> bulkUploadAwards) {

		log.info("{} :: Inside validateSubsidyControlNumber()");
		
		List<SubsidyMeasure> smList = awardService.getAllSubsidyMeasures();

		List<String> subsidyControlNumberTitleList = smList.stream().map(sm -> sm.getScNumber())
				.collect(Collectors.toList());

		log.info("subsidyControlNumber size - String {}:: " , subsidyControlNumberTitleList.size());

		List<BulkUploadAwards> subsidyControlNumberErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getSubsidyControlNumber() != null
						&& !subsidyControlNumberTitleList.contains(award.getSubsidyControlNumber()))
				.collect(Collectors.toList());



		// validation scnumber with sctitle.

		List<BulkUploadAwards> subsidyControlNumberExistsList = bulkUploadAwards.stream()
				.filter(requestAward -> !StringUtils.isEmpty(requestAward.getSubsidyControlNumber())
						&& !StringUtils.isEmpty(requestAward.getSubsidyControlTitle()))
				.collect(Collectors.toList());

		List<BulkUploadAwards> scNumberWithNameErrorRecordsList = subsidyControlNumberExistsList.stream()
				.filter(requestAward -> smList.stream().noneMatch(
						bulkAward -> ((bulkAward.getScNumber().equals(requestAward.getSubsidyControlNumber()))
								&& (bulkAward.getSubsidyMeasureTitle().equals(requestAward.getSubsidyControlTitle())))))
				.collect(Collectors.toList());

		
		List<ValidationErrorResult> validationSubsidyControlNumberResultList = new ArrayList<>();
		validationSubsidyControlNumberResultList = subsidyControlNumberErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "A",
						"The subsidy control number must start with SC, followed by 6 digits."))
				.collect(Collectors.toList());
		List<ValidationErrorResult> validationScNumberNotMatchWithTitle = new ArrayList<>();
		validationScNumberNotMatchWithTitle = scNumberWithNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "A",
						"Subsidy Control number does not match with title."))
				.collect(Collectors.toList());
		validationSubsidyControlNumberResultList.addAll(validationScNumberNotMatchWithTitle);

		validationSubsidyControlNumberResultList.addAll(validateScNumberStatus(smList,bulkUploadAwards));

		return validationSubsidyControlNumberResultList;
	}


	private List<ValidationErrorResult> validateScNumberStatus(List<SubsidyMeasure> smList,
										List<BulkUploadAwards> bulkUploadAwards) {

		List<ValidationErrorResult> scNumberInActiveResultList = new ArrayList<>();
		bulkUploadAwards.forEach( award -> {

			if(isScNumberStatusActive(smList,award.getSubsidyControlNumber())) {

				scNumberInActiveResultList.add(new ValidationErrorResult(String.valueOf(award.getRow()), "A",
						"Subsidy Control status not active."));
			}});
		return scNumberInActiveResultList;
	}

	private boolean isScNumberStatusActive(List<SubsidyMeasure> subsidyMeasures,
										   String scNumberReq) {
		boolean isinValid= false;
		for (SubsidyMeasure subsidyMeasure : subsidyMeasures) {
			if(subsidyMeasure.getScNumber().equals(scNumberReq) &&
					"Inactive".equals(subsidyMeasure.getStatus())) {
				isinValid = true;
				break;
			}
		}
		return isinValid;
	}

	/*
	 * the below method validate the nationalId
	 */

	private List<ValidationErrorResult> validateNationalIdType(List<BulkUploadAwards> bulkUploadAwards) {

		List<BulkUploadAwards> beneficiaryMissingErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getNationalIdType() == null))).collect(Collectors.toList());

		List<ValidationErrorResult> validationNationalIdTypeResultList = new ArrayList<>();
		validationNationalIdTypeResultList = beneficiaryMissingErrorRecordsList.stream().map(
				award -> new ValidationErrorResult(String.valueOf(award.getRow()), "I", "You must select an ID type for the recipient organisation."))
				.collect(Collectors.toList());

			return validationNationalIdTypeResultList;
	}

	/*
	 *
	 * the below method validate the beneficiary name length (>255 chars)
	 */
	private List<ValidationErrorResult> validateBeneficiaryName(List<BulkUploadAwards> bulkUploadAwards) {

		
		List<BulkUploadAwards> beneficiaryNameMissingErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getBeneficiaryName()==null  || StringUtils.isEmpty(award.getBeneficiaryName())))).collect(Collectors.toList());
		
		List<ValidationErrorResult> validationBeneficiaryIdResultList = new ArrayList<>();
		validationBeneficiaryIdResultList = beneficiaryNameMissingErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "K",
						"Beneficiary name field is Mandatory"))
				.collect(Collectors.toList());
		
		List<BulkUploadAwards> beneficiaryNameErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getBeneficiaryName()!=null && award.getBeneficiaryName().length() > 255))).collect(Collectors.toList());

		
		validationBeneficiaryIdResultList .addAll( beneficiaryNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "K",
						"Beneficiary name is too long, it should be 255 characters or fewer"))
				.collect(Collectors.toList()));


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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "M",
						"The granting authority name must be less than 255 characters."))
				.collect(Collectors.toList());
		
		List<BulkUploadAwards> validateGrantingAuthorityMissingErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (award.getGrantingAuthorityName()==null)).collect(Collectors.toList());
		if(validateGrantingAuthorityMissingErrorRecordsList.size()>0) {
			validationGrantingAuthorityResultList = validateGrantingAuthorityMissingErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "M",
						"You must enter the name of the granting authority."))
				.collect(Collectors.toList());
		}

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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "B",
						"The subsidy scheme name must be less than 255 characters."))
				.collect(Collectors.toList());

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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "A",
						"The subsidy control number must start with SC, followed by 6 digits."))
				.collect(Collectors.toList());

		return validationSubsidyNumberLengthResultList;
	}

	/*
	 *
	 * the below method validate the AuthorityName name exist in data base or not
	 * (table=Granting_Authority)
	 */
	private List<ValidationErrorResult> validateGrantingAuthorityNameInDb(List<BulkUploadAwards> bulkUploadAwards) {

		log.info("Calling validateGrantingAuthorityNameInDb... - start");
		
		List<GrantingAuthority> grantingAuthorityList = awardService.getAllGrantingAuthorities();
		
		List<String> grantingAuthorityNamesList = grantingAuthorityList.stream()
				.map(grantingAuthority -> grantingAuthority.getGrantingAuthorityName()).collect(Collectors.toList());

		log.info("Granting Authority - String list size " + grantingAuthorityNamesList.size());

		List<BulkUploadAwards> grantingAuthorityNameErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getGrantingAuthorityName() != null
						&& !grantingAuthorityNamesList.contains(award.getGrantingAuthorityName()))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationGrantingAuthorityNameResultList = new ArrayList<>();
		validationGrantingAuthorityNameResultList = grantingAuthorityNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "M",
						"You must enter the name of the granting authority."))
				.collect(Collectors.toList());

		return validationGrantingAuthorityNameResultList;
	}

	private List<ValidationErrorResult> validateNationalIdAwards(List<BulkUploadAwards> bulkUploadAwards) {

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

		List<BulkUploadAwards> nationsIdErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalId()!=null && award.getNationalId().length() > 10).collect(Collectors.toList());
		
		List<BulkUploadAwards> nationsIdMissingRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalId()==null || StringUtils.isEmpty(award.getNationalId())).collect(Collectors.toList());


		List<ValidationErrorResult> validationNationalIdResultList = new ArrayList<>();
		validationNationalIdResultList = nationsIdErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
						"National ID  must be less than  or 10 characters"))
				.collect(Collectors.toList());
		
		if(nationsIdMissingRecordsList.size()>0) {
			validationNationalIdResultList = nationsIdMissingRecordsList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
							"National ID  field is Mandatory."))
					.collect(Collectors.toList());
		}
		
		List<BulkUploadAwards> nationsIdVATErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("VAT Number") && (award.getNationalId()!=null && (award.getNationalId().length() != 9 || !award.getNationalId().matches("[0-9]+")))).collect(Collectors.toList());
		
		if(nationsIdVATErrorRecordsList.size()>0) {
		validationNationalIdResultList.addAll(nationsIdVATErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
						"The VAT number must be 9 digits."))
				.collect(Collectors.toList()));
		}
		
		List<BulkUploadAwards> nationsIdUTRErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("UTR Number") && (award.getNationalId()!=null && (award.getNationalId().length() != 10 ||  !award.getNationalId().matches("[0-9]+")))).collect(Collectors.toList());
		
		if(nationsIdUTRErrorRecordsList.size()>0) {
		validationNationalIdResultList.addAll(nationsIdUTRErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
						"The UTR number must be 10 digits."))
				.collect(Collectors.toList()));
		}
		
		List<BulkUploadAwards> nationsIdCharityErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType()
						.equalsIgnoreCase("Charity Number") && (award.getNationalId()!=null &&
						(award.getNationalId().length() > 8 ||
								!award.getNationalId().matches("[0-9]+")))).collect(Collectors.toList());

		validationNationalIdResultList.addAll(nationsIdCharityErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
						"The charity commission number must be 8 digits. This may include a dash (-) before the last digit."))
				.collect(Collectors.toList()));
		
		
		List<BulkUploadAwards> nationsIdCompanyNumberFormatErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("Company Registration Number")
						&& (award.getNationalId()!=null && (!validateCompanyNumber(award.getNationalId())))).collect(Collectors.toList());
		
		validationNationalIdResultList.addAll(nationsIdCompanyNumberFormatErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "J",
						"The company number must be in one of the following formats:8 digits 2 letters, followed by 6 digits"))
				.collect(Collectors.toList()));

		return validationNationalIdResultList;

	}
	/**
	 * 
	 */
	private boolean validateCompanyNumber(String companyNumber) {

		int charCount = 0;
		int degitCount = 0;
		boolean isFormat = true;
		int firstOccurence = -1;

		if(companyNumber.length()!=8) {
			return false;
		}
		for (int i = 0; i < companyNumber.length(); i++) {
			if (Character.isLetter(companyNumber.charAt(i))) {
				charCount++;
				if (firstOccurence < 0) {
					firstOccurence = i;

				} else {
					if (i - firstOccurence > 1) {
						isFormat = false;
					}
				}
			} else if (Character.isDigit(companyNumber.charAt(i))) {
				degitCount++;
			}
		}

		if ((charCount > 0) && (!isFormat || (charCount > 2 || degitCount > 6))) {
			return false;
		} else if (charCount == 0 && degitCount == 8) {
			return true;
		} else {
			return true;
		}
	}
}
