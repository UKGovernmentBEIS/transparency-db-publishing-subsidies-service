package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AdminProgramRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.AccessManagementConstant;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.AwardUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class BulkUploadAwardsService {

	@Autowired
	private AwardService awardService;

	@Autowired
	private AdminProgramRepository adminProgramRepository;

	private final HashMap<String, String> columnMapping = new HashMap<String, String>()
	{{
		put("SC Number", "A");
		put("Title", "B");
		put("AP Number", "C");
		put("Standalone", "D");
		put("Subsidies or Schemes of Interest (SSoI) or Subsidies or Schemes of Particular Interest (SSoPI)", "E");
		put("Specific Policy Objective", "F");
		put("Description", "G");
        put("Public Authority URL", "H");
		put("Public Authority URL Description", "I");
		put("Legal Basis","J");
		put("Services of Public Economic Interest (SPEI)", "K");
		put("Objective", "L");
		put("Objective Other", "M");
		put("Instrument", "N");
		put("Instrument Other", "O");
		put("Full Range", "P");
		put("Full Exact", "Q");
		put("ID Type", "R");
		put("ID", "S");
		put("Beneficiary", "T");
		put("Size of Org", "U");
		put("GA Name", "V");
		put("Legal Granting Date", "W");
		put("Goods Services", "X");
		put("Region", "Y");
		put("Sector", "Z");

	}};


	/*
	 * the below method validate the excel file passed in request.
	 */
	public ValidationResult validateFile(MultipartFile file, String role) {

		try {

			Boolean isLatestVersion = ExcelHelper.validateColumnCount(file.getInputStream());

			if(!isLatestVersion){
				ValidationResult validationResult = new ValidationResult();

				ValidationErrorResult validationErrorResult = new ValidationErrorResult();
				validationErrorResult.setRow("All");
				validationErrorResult.setColumns("All");
				validationErrorResult.setErrorMessages("The version of the template being used is not up to date. Please re-download and use the latest version.");

				validationResult.setTotalRows(1);
				validationResult.setErrorRows(1);
				validationResult.setValidationErrorResult(Arrays.asList(validationErrorResult));

				return validationResult;
			}

			// Read Excel file
			List<BulkUploadAwards> bulkUploadAwards = ExcelHelper.excelToAwards(file.getInputStream());

			log.info("Back from Excel to awards...printed list of awards - end");

			// Validation National Id length check
			List<ValidationErrorResult> nationalIdMissingList = validateNationalIdAwards(bulkUploadAwards);

			List<ValidationErrorResult> nationalIdTypeMissingList = validateNationalIdType(bulkUploadAwards);


			/*
			 * 3) If incorrect SC number is entered, user system should throw an error
			 * Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> scNumberNameCheckList = validateScNumberScTitle(bulkUploadAwards);

			//List<ValidationErrorResult> scNumberStatusList = validateScNumberStatus(bulkUploadAwards,);

			//List<ValidationErrorResult> subsidyControlNumberLengthList = validateSubsidyNumberLength(bulkUploadAwards);

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

			List<ValidationErrorResult> StandaloneAwardErrorList = validateStandaloneAward(bulkUploadAwards);

			List<ValidationErrorResult> AuthorityURLErrorList = validateAuthorityURL(bulkUploadAwards);
			List<ValidationErrorResult> AuthorityURLDescriptionErrorList = validateAuthorityURLDescription(bulkUploadAwards);

			List<ValidationErrorResult> SubsidyDescriptionErrorList = validateSubsidyDescription(bulkUploadAwards);

			List<ValidationErrorResult> SpecificPolicyObjectiveErrorList = validateSpecificPolicyObjective(bulkUploadAwards);

			List<ValidationErrorResult> SubsidyTaxRangeAmountErrorList = validateSubsidyAmountRange(bulkUploadAwards);

			List<ValidationErrorResult> adminProgramNumberErrorList = validateAdminProgramNumber(bulkUploadAwards);

			List<ValidationErrorResult> SubsidyAwardInterestErrorList = validateSubsidyAwardInterest(
					bulkUploadAwards);

			List<ValidationErrorResult> subsidySpeiErrorList = validateSubsidySpei(
					bulkUploadAwards);

			List<ValidationErrorResult> LegalBasisErrorList = validateLegalBasis(
					bulkUploadAwards);

			// Merge lists of Validation Errors
			List<ValidationErrorResult> validationErrorResultList = Stream
					.of(scNumberNameCheckList, subsidyMeasureTitleNameLengthList, subsidyPurposeCheckList,
							nationalIdTypeMissingList, nationalIdMissingList, beneficiaryNameErrorList, subsidyControlNumberMismatchList,
							grantingAuthorityNameErrorList, grantingAuthorityErrorList, sizeOfOrgErrorList,
							spendingRegionErrorList, spendingSectorErrorList, goodsOrServiceErrorList,SubsidyInstrumentErrorList,
							legalGrantingDateErrorList,SubsidyElementFullAmountErrorList, StandaloneAwardErrorList,
							AuthorityURLErrorList, AuthorityURLDescriptionErrorList, SubsidyDescriptionErrorList, SpecificPolicyObjectiveErrorList,
							SubsidyTaxRangeAmountErrorList, adminProgramNumberErrorList, SubsidyAwardInterestErrorList, subsidySpeiErrorList,LegalBasisErrorList)
					.flatMap(x -> x.stream()).collect(Collectors.toList());

			log.info("Final validation errors list ...printing list of errors - start");

			ValidationResult validationResult = new ValidationResult();
			validationResult.setValidationErrorResult(validationErrorResultList);
			validationResult.setTotalRows(bulkUploadAwards.size());
			validationResult.setErrorRows(validationErrorResultList.size());
			validationResult.setMessage((!validationErrorResultList.isEmpty()) ? "Validation Errors in Uploaded file"
					: "No errors in Uploaded file");

			log.info("Final validation Result object ...printing validationResult - start");

			if (validationResult.getValidationErrorResult().size() == 0) {

				log.info("No validation error in bulk excel template");

				awardService.processBulkAwards(bulkUploadAwards,role);
			}

			return validationResult;

		} catch (IOException e) {
			log.error(" Error in validationResult **** ", e);
			throw new RuntimeException("Fail to store data : " + e.getMessage());
		}

	}

	private List<ValidationErrorResult> validateAuthorityURLDescription(List<BulkUploadAwards> bulkUploadAwards) {
		return bulkUploadAwards.stream().filter(award -> award.getAuthorityURL() != null && award.getAuthorityURLDescription().trim().length() > 255)
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Public Authority URL Description"),
						"The public authority policy URL description must be 255 characters or less")).collect(Collectors.toList());
	}

	private List<ValidationErrorResult> validateAuthorityURL(List<BulkUploadAwards> bulkUploadAwards) {
		return bulkUploadAwards.stream().filter(award -> award.getAuthorityURL() != null && award.getAuthorityURL().trim().length() > 500)
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Public Authority URL"),
						"The public authority policy URL must be 500 characters or less")).collect(Collectors.toList());
	}

	private List<ValidationErrorResult> validateSubsidyAwardInterest(List<BulkUploadAwards> bulkUploadAwards) {

		List<BulkUploadAwards> subsidyAwardInterestErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getStandaloneAward() == "Yes") && (award.getSubsidyAwardInterest() == null || StringUtils.isEmpty(award.getSubsidyAwardInterest()))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationSubsidyAwardInterestErrorResultList = new ArrayList<>();
		validationSubsidyAwardInterestErrorResultList = subsidyAwardInterestErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Subsidies or Schemes of Interest (SSoI) or Subsidies or Schemes of Particular Interest (SSoPI)"),
						"When the award is standalone you must select Subsidies or Schemes of Interest (SSoI), Subsidies or Schemes of Particular Interest (SSoPI) or Neither"))
				.collect(Collectors.toList());

		return validationSubsidyAwardInterestErrorResultList;
	}
	private List<ValidationErrorResult> validateAdminProgramNumber(List<BulkUploadAwards> bulkUploadAwards) {
		List<ValidationErrorResult> errorList = new ArrayList<>();

		// ✅ 1. Format validation — Must match AP followed by 5 digits
		List<BulkUploadAwards> invalidFormatAwards = bulkUploadAwards.stream()
				.filter(award -> award.getAdminProgramNumber() != null && !award.getAdminProgramNumber().matches("^AP\\d{5}$"))
				.collect(Collectors.toList());

		errorList.addAll(invalidFormatAwards.stream()
				.map(award -> new ValidationErrorResult(
						String.valueOf(award.getRow()),
						columnMapping.get("AP Number"),
						"The admin program number must start with AP, followed by 5 digits."
				))
				.collect(Collectors.toList()));

		// ✅ Only process valid-format AP numbers for remaining checks
		List<BulkUploadAwards> validFormatAwards = bulkUploadAwards.stream()
				.filter(award -> award.getAdminProgramNumber() != null && award.getAdminProgramNumber().matches("^AP\\d{5}$"))
				.collect(Collectors.toList());

		// ✅ 2. AP Exists
		List<BulkUploadAwards> adminProgramExistsErrorList = validFormatAwards.stream()
				.filter(award -> {
					AdminProgram adminProgram = adminProgramRepository.findById(award.getAdminProgramNumber()).orElse(null);
					return adminProgram == null;
				}).collect(Collectors.toList());

		errorList.addAll(adminProgramExistsErrorList.stream()
				.map(award -> new ValidationErrorResult(
						String.valueOf(award.getRow()),
						columnMapping.get("AP Number"),
						"Admin program " + award.getAdminProgramNumber() + " does not exist."
				))
				.collect(Collectors.toList()));

		// ✅ 3. AP Active
		List<BulkUploadAwards> adminProgramActiveErrorList = validFormatAwards.stream()
				.filter(award -> {
					AdminProgram adminProgram = adminProgramRepository.findById(award.getAdminProgramNumber()).orElse(null);
					return adminProgram != null && !"active".equalsIgnoreCase(adminProgram.getStatus());
				}).collect(Collectors.toList());

		errorList.addAll(adminProgramActiveErrorList.stream()
				.map(award -> new ValidationErrorResult(
						String.valueOf(award.getRow()),
						columnMapping.get("AP Number"),
						"Admin program " + award.getAdminProgramNumber() + " is not active."
				))
				.collect(Collectors.toList()));

		// ✅ 4. AP SC Match
		List<BulkUploadAwards> adminProgramSchemeMatchErrorList = validFormatAwards.stream()
				.filter(award -> {
					AdminProgram adminProgram = adminProgramRepository.findById(award.getAdminProgramNumber()).orElse(null);
					return adminProgram != null && !adminProgram.getSubsidyMeasure().getScNumber().equalsIgnoreCase(award.getSubsidyControlNumber());
				}).collect(Collectors.toList());

		errorList.addAll(adminProgramSchemeMatchErrorList.stream()
				.map(award -> new ValidationErrorResult(
						String.valueOf(award.getRow()),
						columnMapping.get("AP Number"),
						"Admin program " + award.getAdminProgramNumber() + " is not associated to scheme " + award.getSubsidyControlNumber() + "."
				))
				.collect(Collectors.toList()));

		return errorList;
	}


	private List<ValidationErrorResult> validateSubsidyDescription(List<BulkUploadAwards> bulkUploadAwards) {
		List<BulkUploadAwards> subsidyDescriptionErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (
							award.getSubsidyDescription() != null && award.getSubsidyDescription().length() > 10000)
					)
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationSubsidyDescriptionResultList = new ArrayList<>();
		validationSubsidyDescriptionResultList = subsidyDescriptionErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Description"),
						"The subsidy award description must be 10000 characters or less."))
				.collect(Collectors.toList());

		return validationSubsidyDescriptionResultList;
	}

	private List<ValidationErrorResult> validateSpecificPolicyObjective(List<BulkUploadAwards> bulkUploadAwards) {
		List<ValidationErrorResult> errorList = new ArrayList<>();
		List<BulkUploadAwards> specificPolicyObjectiveErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (
						award.getSpecificPolicyObjective() != null && award.getSpecificPolicyObjective().length() > 1500)
				)
				.collect(Collectors.toList());

			errorList.addAll(specificPolicyObjectiveErrorRecordsList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Specific Policy Objective"),
							"The specific policy objective must be 1500 characters or less."))
					.collect(Collectors.toList()));

		List<BulkUploadAwards> validateSpecificPolicyObjectiveMissingErrorList = bulkUploadAwards.stream()
				.filter(award -> (Objects.equals(award.getStandaloneAward(), "Yes") && award.getSpecificPolicyObjective() == null)).collect(Collectors.toList());


			errorList.addAll(validateSpecificPolicyObjectiveMissingErrorList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Specific Policy Objective"),
							"You must enter the specific policy objective."))
					.collect(Collectors.toList()));


		List<BulkUploadAwards> specificPolicyObjectiveWhenStandaloneAwardErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (Objects.equals(award.getStandaloneAward(), "No") && award.getSpecificPolicyObjective() != null)
				)
				.collect(Collectors.toList());

			errorList.addAll(specificPolicyObjectiveWhenStandaloneAwardErrorRecordsList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Specific Policy Objective"),
							"Specific Policy Objective is only saved and published for standalone awards. If you are submitting an in-scheme award, please include the policy objective in the Subsidy Description field (Field G) instead "))
					.collect(Collectors.toList()));

		return errorList;
	}

	private List<ValidationErrorResult> validateLegalBasis(List<BulkUploadAwards> bulkUploadAwards) {
		List<BulkUploadAwards> legalBasisErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (
						award.getLegalBasis() != null && award.getLegalBasis().length() > 5000)
				)
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationLegalBasisResultList = new ArrayList<>();
		validationLegalBasisResultList = legalBasisErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Legal Basis"),
						"The legal basis must be 5000 characters or less."))
				.collect(Collectors.toList());

		// Condition 2: Null or empty
		List<BulkUploadAwards> legalBasisMissingList = bulkUploadAwards.stream()
				.filter(award -> award.getLegalBasis() == null || award.getLegalBasis().trim().isEmpty())
				.collect(Collectors.toList());

		List<ValidationErrorResult> missingErrors = legalBasisMissingList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Legal Basis"),
						"You must enter a legal basis."))
				.collect(Collectors.toList());

		validationLegalBasisResultList.addAll(missingErrors);


		return validationLegalBasisResultList;
	}

	private List<ValidationErrorResult> validateSubsidySpei(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * validation for subsidy SPEI entered in the input file.
		 */

		List<String> speiAcceptedOptions = Arrays.asList("Yes", "No");

		List<BulkUploadAwards> speiErrorRecordsList = bulkUploadAwards.stream().filter(
						award -> (((award.getSpei() == null || StringUtils.isEmpty(award.getSpei()))
								|| (!speiAcceptedOptions.contains(award.getSpei())))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationSpeiErrorListResultList = new ArrayList<>();
		validationSpeiErrorListResultList = speiErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Services of Public Economic Interest (SPEI)"),
						"You must select if the award is a Services of Public Economic Interest (SPEI) or not. Accepted values are 'Yes' or 'No'"))
				.collect(Collectors.toList());

		return validationSpeiErrorListResultList;
	}

	private List<ValidationErrorResult> validateStandaloneAward(List<BulkUploadAwards> bulkUploadAwards) {
		List<BulkUploadAwards> standaloneAwardErrorRecordsList = bulkUploadAwards.stream()
				.filter(
						award -> (
								((award.getStandaloneAward() == null || StringUtils.isEmpty(award.getStandaloneAward()))
										|| !(!award.getStandaloneAward().equalsIgnoreCase("yes") || !award.getStandaloneAward().equalsIgnoreCase("no")))
						)
					)
				.collect(Collectors.toList());

		List<BulkUploadAwards> standaloneAwardWithSCErrorRecordsList = bulkUploadAwards.stream()
				.filter(
						award -> (
								(award.getStandaloneAward().equalsIgnoreCase("yes") && (
										!StringUtils.isEmpty(award.getSubsidyControlNumber()) || !StringUtils.isEmpty(award.getSubsidyControlTitle()) || !StringUtils.isEmpty(award.getAdminProgramNumber())
										))
						)
				)
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationStandaloneAwardResultList = new ArrayList<>();
		validationStandaloneAwardResultList = standaloneAwardErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Standalone"),
						"You must provide the standalone status of the award. This must be 'Yes' or 'No'."))
				.collect(Collectors.toList());

		validationStandaloneAwardResultList.addAll(
				standaloneAwardWithSCErrorRecordsList.stream()
						.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Standalone"),
								"If 'Standalone Award' is 'Yes', you must not provide an SC number, Scheme Title, or AP number"))
						.collect(Collectors.toList())
		);

		return validationStandaloneAwardResultList;
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
								|| StringUtils.isEmpty(award.getSubsidyControlTitle())) && (!award.getStandaloneAward().equalsIgnoreCase("yes")))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationScNumberScTitlResultList = new ArrayList<>();
		validationScNumberScTitlResultList = ScNumberScTitleErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("SC Number"),
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
		List<ValidationErrorResult> validationSizeOfOrgErrorListResultList = new ArrayList<>();
		List<String> orgSizeOptions = Arrays.asList("SME", "Large", "Not specified");

		List<BulkUploadAwards> sizeOfOrgMatchOptionsErrorList = bulkUploadAwards.stream()
				.filter(award -> ((award.getOrgSize() != null && !orgSizeOptions.contains(award.getOrgSize()))))
				.collect(Collectors.toList());
		validationSizeOfOrgErrorListResultList.addAll(sizeOfOrgMatchOptionsErrorList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Size of Org"),
						"You must select one of the following options for organisation size: SME, Large or Not specified"))
				.collect(Collectors.toList()));


		List<BulkUploadAwards> sizeOfOrgErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getOrgSize() == null || StringUtils.isEmpty(award.getOrgSize()))))
				.collect(Collectors.toList());

		validationSizeOfOrgErrorListResultList.addAll(sizeOfOrgErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Size of Org"),
						"You must select an organisation size."))
				.collect(Collectors.toList()));

		return validationSizeOfOrgErrorListResultList;
	}

	/*
	 *
	 * the below method validate Subsidy Purpose entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSubsidyPurpose(List<BulkUploadAwards> bulkUploadAwards) {
		List<ValidationErrorResult> subsidyObjectiveErrorResultList = new ArrayList<>();

		List<BulkUploadAwards> objectiveFormatErrorList = bulkUploadAwards.stream()
				.filter(award -> {
					if (award.getSubsidyObjective() != null) {
						ArrayList<String> objectiveList = new ArrayList<>(Arrays.asList(award.getSubsidyObjective().toLowerCase().trim().split("\\s*\\|\\s*")));
						int otherIndex = -1;
						for (int i = 0; i < objectiveList.size(); i++) {
							if(objectiveList.get(i).startsWith("other")) {
								otherIndex = i;
							}
						}
						if(otherIndex >= 0){
							objectiveList.remove(otherIndex);
						}

						if(objectiveList.size() > 0 && !Objects.equals(objectiveList.get(0), "")) {
							return (!new HashSet<>(AccessManagementConstant.SUBSIDY_OBJECTIVES).containsAll(objectiveList));
						}
					}
					return false;
				}).collect(Collectors.toList());


		ArrayList<String> objectiveErrorList = new ArrayList<>();

		for (BulkUploadAwards scheme : objectiveFormatErrorList) {
			ArrayList<String> objectiveList = new ArrayList<>(Arrays.asList(scheme.getSubsidyObjective().toLowerCase().trim().split("\\s*\\|\\s*")));
			for (int i = 0; i < objectiveList.size(); i++) {
				if (!AccessManagementConstant.SUBSIDY_OBJECTIVES.contains(objectiveList.get(i))) {
					String currentError = objectiveList.get(i);
					objectiveErrorList.add(objectiveList.get(i));
					subsidyObjectiveErrorResultList.add(new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Objective"),
							"The following purpose(s) are incorrect '" + currentError + "'. Check that the spelling and punctuation matches the purpose list."));
				}
			}
		}

		List<BulkUploadAwards> validateObjectiveOtherMissingErrorList = bulkUploadAwards.stream()
				.filter(award -> (award.getSubsidyObjectiveOther() == null && award.getSubsidyObjective() == null)).collect(Collectors.toList());


		if (validateObjectiveOtherMissingErrorList.size() > 0){
			subsidyObjectiveErrorResultList.addAll(validateObjectiveOtherMissingErrorList.stream()
					.map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Objective - Other"),
							"You must enter a Purpose or Other Purpose."))
					.collect(Collectors.toList()));
		}

		List<BulkUploadAwards> validateObjectiveOtherCharLimitErrorList = bulkUploadAwards.stream()
				.filter(scheme -> (scheme.getSubsidyObjectiveOther() != null && scheme.getSubsidyObjectiveOther().length() > 255)).collect(Collectors.toList());


		if (validateObjectiveOtherCharLimitErrorList.size() > 0){
			subsidyObjectiveErrorResultList.addAll(validateObjectiveOtherCharLimitErrorList.stream()
					.map(scheme -> new ValidationErrorResult(String.valueOf(scheme.getRow()), columnMapping.get("Objective - Other"),
							"You cannot have more than 255 characters for the Other Purpose field."))
					.collect(Collectors.toList()));
		}
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

		List<ValidationErrorResult> validationSpendingRegionErrorListResultList = new ArrayList<>();
		validationSpendingRegionErrorListResultList.addAll(spendingRegionErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Region"),
						"You must select the region that the recipient organisation is based in."))
				.collect(Collectors.toList()));

		// Check that the regions provided are allowed
		List<BulkUploadAwards> spendingRegionInvalidStringRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSpendingRegion()!=null && !new HashSet<>(Arrays.asList(AccessManagementConstant.REGIONS_LOWER)).containsAll(Arrays.asList(award.getSpendingRegion().toLowerCase().trim().split("\\s*,\\s*")))))).collect(Collectors.toList());
		validationSpendingRegionErrorListResultList.addAll(spendingRegionInvalidStringRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Region"),
						"Spending Region(s) must contain only the following: " + Arrays.toString(AccessManagementConstant.REGIONS)))
				.collect(Collectors.toList()));

		// check that there are no duplicated regions
		List<BulkUploadAwards> spendingRegionDuplicateStringRecordsList = bulkUploadAwards.stream()
				.filter(award -> {
					if(award.getSpendingRegion() == null){
						return false;
					}
					String[] spendingRegionArray = award.getSpendingRegion().toLowerCase().trim().split("\\s*,\\s*");
					Set<String> spendingRegionSet = new HashSet<>(Arrays.asList(spendingRegionArray));
					return spendingRegionSet.size() != spendingRegionArray.length;
				})
				.collect(Collectors.toList());

		validationSpendingRegionErrorListResultList.addAll(spendingRegionDuplicateStringRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Region"),
						"Spending Region(s) must not contain duplicate entries"))
				.collect(Collectors.toList()));

		List<BulkUploadAwards> spendingRegionNationalErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> {
					if(award.getSpendingRegion() == null){
						return false;
					}
					String[] spendingRegionArray = award.getSpendingRegion().toLowerCase().trim().split("\\s*,\\s*");
					return Arrays.asList(spendingRegionArray).contains("uk-wide") && spendingRegionArray.length > 1;
				})
				.collect(Collectors.toList());

		validationSpendingRegionErrorListResultList.addAll(spendingRegionNationalErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Region"),
						"Spending Region(s) should not contain other regions if 'UK-wide' is included"))
				.collect(Collectors.toList()));

		log.info("Validation Result Error list - Spending Region should enter = " + validationSpendingRegionErrorListResultList.size());

		return validationSpendingRegionErrorListResultList;
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Sector"),
						"You must select the sector that the recipient organisation belongs to."))
				.collect(Collectors.toList());

		return validationspendingSectorErrorListResultList;
	}


	private List<ValidationErrorResult> validateSubsidyElementFullAmount(List<BulkUploadAwards> bulkUploadAwards) {

		List<BulkUploadAwards> subsidyAmountExactErrorRecordsList = bulkUploadAwards.stream().filter(
						award -> (((award.getSubsidyInstrument() != null && !award.getSubsidyInstrument().startsWith("Tax")) &&
								StringUtils.isBlank(award.getSubsidyAmountExact()))))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationSubsidyAmountExactErrorResultList = subsidyAmountExactErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Exact"),
						"For non-tax measure subsidies, enter 'N/A' in column " + columnMapping.get("Full Range") + ". You can enter the exact subsidy amount in column " + columnMapping.get("Full Exact") + "."))
				.collect(Collectors.toList());

		List<BulkUploadAwards> subsidyAmountFormatErrorRecordsList = bulkUploadAwards.stream().filter(
						award -> (((award.getSubsidyInstrument() != null && !award.getSubsidyInstrument().startsWith("Tax")) &&
								award.getSubsidyAmountExact() != null && !ExcelHelper.isNumeric(award.getSubsidyAmountExact()))))
				.collect(Collectors.toList());

		if (!subsidyAmountFormatErrorRecordsList.isEmpty()) {
			validationSubsidyAmountExactErrorResultList.addAll(subsidyAmountFormatErrorRecordsList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Exact"),
							"Subsidy Element Full Amount should be numeric."))
					.collect(Collectors.toList()));
		}

		List<BulkUploadAwards> SubsidyFullAmountInapplicableErrorList = bulkUploadAwards.stream()
				.filter(award -> award.getSubsidyInstrument() != null &&
						award.getSubsidyInstrument().startsWith("Tax") &&
						StringUtils.isNotBlank(award.getSubsidyAmountExact()))
				.collect(Collectors.toList());

		if (!SubsidyFullAmountInapplicableErrorList.isEmpty()) {
			validationSubsidyAmountExactErrorResultList.addAll(SubsidyFullAmountInapplicableErrorList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Exact"),
							"Subsidy Element Full Amount field is only applicable to non-tax measure subsidy forms. Remove the amount value or change the subsidy form."))
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Instrument"),
						"Subsidy Instrument is Mandatory."))
				.collect(Collectors.toList());

		List<BulkUploadAwards> SubsidyInstrumentOthersErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ((award.getSubsidyInstrument()!= null && ("Other".equalsIgnoreCase(award.getSubsidyInstrument()) && (award.getSubsidyInstrumentOther()==null || StringUtils.isEmpty(award.getSubsidyInstrumentOther()))))))
				.collect(Collectors.toList());
		validationSubsidyInstrumentErrorListResultList.addAll(SubsidyInstrumentOthersErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Instrument Other"),
						"Subsidy Instrument-other field is mandatory when Subsidy Instrument is Other"))
				.collect(Collectors.toList()));

		List<BulkUploadAwards> SubsidyInstrumentOtherErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyInstrumentOther()!=null &&
						award.getSubsidyInstrumentOther().length() > 255))).collect(Collectors.toList());

		if(SubsidyInstrumentOtherErrorRecordsList.size() > 0) {
		validationSubsidyInstrumentErrorListResultList = SubsidyInstrumentOtherErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Instrument Other"),
						"Subsidy Instrument-other length > 255 characters."))
				.collect(Collectors.toList());
		}
		List<BulkUploadAwards> SubsidyInstrumentTaxErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyInstrument()!=null && award.getSubsidyInstrument().startsWith("Tax"))&& (award.getSubsidyAmountRange()==null ||
						StringUtils.isEmpty(award.getSubsidyAmountRange())) && (award.getSubsidyAmountExact()!=null ||
						!StringUtils.isEmpty(award.getSubsidyAmountExact())))).collect(Collectors.toList());

		if(SubsidyInstrumentTaxErrorRecordsList.size() > 0) {
		validationSubsidyInstrumentErrorListResultList = SubsidyInstrumentTaxErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Range"),
						"For tax measure subsidies, the amount must be 0. You can select a subsidy range instead in column " + columnMapping.get("Full Range") + "."))
				.collect(Collectors.toList());
		}

		return validationSubsidyInstrumentErrorListResultList;
	}


	private List<ValidationErrorResult> validateSubsidyAmountRange(List<BulkUploadAwards> bulkUploadAwards){

		List<ValidationErrorResult> validationTaxRangeAmountErrorResultList = new ArrayList<>();

		List<BulkUploadAwards> SubsidyTaxRangeInapplicableErrorList = bulkUploadAwards.stream()
				.filter(award -> (award.getSubsidyInstrument()!= null && !award.getSubsidyInstrument().startsWith("Tax")
						&& !(award.getSubsidyAmountRange() == null
						|| StringUtils.isEmpty(award.getSubsidyAmountRange())
						|| StringUtils.contains(award.getSubsidyAmountRange().toUpperCase(), "N/A")))
				).collect(Collectors.toList());

		if(!SubsidyTaxRangeInapplicableErrorList.isEmpty()) {
			validationTaxRangeAmountErrorResultList = SubsidyTaxRangeInapplicableErrorList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Range"),
							"Subsidy Full Amount Range is only applicable to tax measure subsidy forms. Remove the range value or change the subsidy form."))
					.collect(Collectors.toList());
		}

		List<BulkUploadAwards> taxBulkUploadAwards = bulkUploadAwards.stream()
				.filter(award -> (award.getSubsidyInstrument()!=null && award.getSubsidyInstrument().startsWith("Tax"))).collect(Collectors.toList());

		List<BulkUploadAwards> SubsidyTaxRangeAmountErrorList = taxBulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyAmountRange() == null || StringUtils.isEmpty(award.getSubsidyAmountRange()))&&
						(award.getSubsidyAmountExact() == null || StringUtils.isEmpty(award.getSubsidyAmountExact())))).collect(Collectors.toList());

		if(SubsidyTaxRangeAmountErrorList.size() > 0) {
			validationTaxRangeAmountErrorResultList = SubsidyTaxRangeAmountErrorList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Range"),
							"For tax measure subsidies, the tax range amount is mandatory"))
					.collect(Collectors.toList());
		}

		List<BulkUploadAwards> SubsidyInstrumentTaxAmountRangeNotNumericError = taxBulkUploadAwards.stream()
				.filter(award -> (award.getSubsidyAmountRange() != null && (((award.getSubsidyAmountRange()).split("-")).length) == 2) &&
						(((!ExcelHelper.isNumeric(award.getSubsidyAmountRange().split("-")[0]) ||
								!ExcelHelper.isNumeric(award.getSubsidyAmountRange().split("-")[1])))
						)).collect(Collectors.toList());

		if(SubsidyInstrumentTaxAmountRangeNotNumericError.size() > 0) {
			validationTaxRangeAmountErrorResultList = SubsidyInstrumentTaxAmountRangeNotNumericError.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Range"),
							"For tax measure subsidies, the tax range amount must be in the format of a number range. The values must be numeric and separated by a '-'"))
					.collect(Collectors.toList());
		}

		List<BulkUploadAwards> SubsidyInstrumentTaxAmountRangeError = taxBulkUploadAwards.stream()
				.filter(award -> (award.getSubsidyAmountRange() == null || (((award.getSubsidyAmountRange()).split("-")).length) != 2)).collect(Collectors.toList());

		if(SubsidyInstrumentTaxAmountRangeError.size() > 0) {
			validationTaxRangeAmountErrorResultList = SubsidyInstrumentTaxAmountRangeError.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Range"),
							"For tax measure subsidies, the tax range amount must be in the format of a number range. For example, 0-100000 or 100001-300000"))
					.collect(Collectors.toList());
		}

		List<BulkUploadAwards> SubsidyInstrumentTaxAmountRangeInvalidError = taxBulkUploadAwards.stream()
				.filter(award -> ((award.getSubsidyAmountRange() != null && (((award.getSubsidyAmountRange()).split("-")).length) == 2) &&
						(((Integer.parseInt(award.getSubsidyAmountRange().split("-")[0]) >=
								Integer.parseInt(award.getSubsidyAmountRange().split("-")[1])))
						))).collect(Collectors.toList());

		if(SubsidyInstrumentTaxAmountRangeInvalidError.size() > 0) {
			validationTaxRangeAmountErrorResultList = SubsidyInstrumentTaxAmountRangeInvalidError.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Full Range"),
							"Invalid subsidy tax range. The lower bound of the subsidy tax range cannot be larger than or equal to the upper bound"))
					.collect(Collectors.toList());
		}

		return validationTaxRangeAmountErrorResultList;
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Goods Services"),
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Legal Granting Date"),
						"You must enter the date that the subsidy was awarded."))
				.collect(Collectors.toList());

		List<BulkUploadAwards> legalGrantingDateFormatErrorRecordsList = bulkUploadAwards.stream().filter(
				award -> ("invalid".equalsIgnoreCase(award.getLegalGrantingDate())))
				.collect(Collectors.toList());
		validationlegalGrantingDateErrorListResultList.addAll(legalGrantingDateFormatErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Legal Granting Date"),
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
						sm -> ((award.getSubsidyControlNumber().equals(sm.getScNumber())) &&
								(!isLegalGrantingDateWithinSchemeDate(award,sm)))
				)).collect(Collectors.toList());

		List<BulkUploadAwards> legalGrantingDateWithinMeasureDateTitleNoNumberErrorRecordsList = subsidyControlTitleExistsNoNumberList.stream()
				.filter(award -> smList.stream().anyMatch(
						sm -> ((award.getSubsidyControlTitle().equals(sm.getSubsidyMeasureTitle())) &&
								(!isLegalGrantingDateWithinSchemeDate(award,sm)))
				)).collect(Collectors.toList());

		validationlegalGrantingDateErrorListResultList.addAll(legalGrantingDateWithinMeasureDateNumberErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Legal Granting Date"),
						"The legal granting date of the subsidy award must be within the start and end dates of the associated subsidy scheme."))
				.collect(Collectors.toList()));

		validationlegalGrantingDateErrorListResultList.addAll(legalGrantingDateWithinMeasureDateTitleNoNumberErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Legal Granting Date"),
						"The legal granting date of the subsidy award must be within the start and end dates of the associated subsidy scheme. Please add the SC number for this scheme as there may be multiple schemes with this title."))
				.collect(Collectors.toList()));


		return validationlegalGrantingDateErrorListResultList;
	}

	private List<ValidationErrorResult> validateSubsidyControlNumber(List<BulkUploadAwards> bulkUploadAwards) {

		log.info("Inside validateSubsidyControlNumber()");

		List<SubsidyMeasure> smList = awardService.getAllSubsidyMeasures();

		// Build map of SC number to title for quick lookup
		Map<String, String> scNumberToTitleMap = smList.stream()
				.collect(Collectors.toMap(SubsidyMeasure::getScNumber, SubsidyMeasure::getSubsidyMeasureTitle));

		List<ValidationErrorResult> validationErrorResults = new ArrayList<>();

		for (BulkUploadAwards award : bulkUploadAwards) {

			if ("yes".equalsIgnoreCase(award.getStandaloneAward())) {
				continue; // skip validation for standalone
			}

			String scNumber = StringUtils.trimToEmpty(award.getSubsidyControlNumber());
			String scTitle = StringUtils.trimToEmpty(award.getSubsidyControlTitle());

			boolean hasScNumber = !scNumber.isEmpty();
			boolean hasScTitle = !scTitle.isEmpty();

			// Case 1: Neither SC Number nor SC Title provided
			if (!hasScNumber && !hasScTitle) {
				validationErrorResults.add(new ValidationErrorResult(
						String.valueOf(award.getRow()),
						columnMapping.get("SC Number"),
						"You must enter either a subsidy control number or a subsidy scheme title."
				));
				continue;
			}

			// Case 2: Both provided, but mismatch
			if (hasScNumber && hasScTitle) {
				String expectedTitle = scNumberToTitleMap.get(scNumber);
				if (expectedTitle == null || !expectedTitle.equalsIgnoreCase(scTitle)) {
					validationErrorResults.add(new ValidationErrorResult(
							String.valueOf(award.getRow()),
							columnMapping.get("SC Number"),
							"Subsidy Control number does not match with title."
					));
				}
				continue;

			}

			// Case 3: Only SC Number provided — check if it's valid format and known
			if (hasScNumber) {
				if (!scNumber.matches("^SC\\d{5}$")) {
					validationErrorResults.add(new ValidationErrorResult(
							String.valueOf(award.getRow()),
							columnMapping.get("SC Number"),
							"The subsidy control number must start with SC, followed by 5 digits."
					));
				}
				continue;

			}
			// Case 4: Only Title provided — check if any SC Number maps to it
			if (hasScTitle && !scNumberToTitleMap.containsValue(scTitle)) {
				validationErrorResults.add(new ValidationErrorResult(
						String.valueOf(award.getRow()),
						columnMapping.get("SC Number"),
						"Subsidy scheme title is invalid or not linked to any known SC number."
				));
				continue;
			}

			//Case 5: Multiple schemes found with same title
			if (hasScTitle) {
				long count = 0;
				for (SubsidyMeasure sm : smList) {
					if (sm.getSubsidyMeasureTitle() != null && sm.getSubsidyMeasureTitle().equalsIgnoreCase(scTitle)) {
						count++;
					}
				}

				if (count > 1) {
					validationErrorResults.add(new ValidationErrorResult(
							String.valueOf(award.getRow()),
							columnMapping.get("SC Number"),
							"Multiple schemes have the same title. Please enter the subsidy control number."
					));
				}
			}


		}

		// Optional: Add SC Number Status validations here
		validationErrorResults.addAll(validateScNumberStatus(smList, bulkUploadAwards));

		return validationErrorResults;
	}



	private List<ValidationErrorResult> validateScNumberStatus(List<SubsidyMeasure> smList,
										List<BulkUploadAwards> bulkUploadAwards) {

		List<ValidationErrorResult> scNumberInActiveResultList = new ArrayList<>();
		bulkUploadAwards.forEach( award -> {

			if(isScNumberStatusActive(smList,award.getSubsidyControlNumber())) {

				scNumberInActiveResultList.add(new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("SC Number"),
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
				award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID Type"), "You must select an ID type for the recipient organisation."))
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Beneficiary"),
						"Beneficiary name field is Mandatory"))
				.collect(Collectors.toList());

		List<BulkUploadAwards> beneficiaryNameErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> ((award.getBeneficiaryName()!=null && award.getBeneficiaryName().length() > 255))).collect(Collectors.toList());


		validationBeneficiaryIdResultList .addAll( beneficiaryNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Beneficiary"),
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("GA Name"),
						"The public authority name must be less than 255 characters."))
				.collect(Collectors.toList());

		List<BulkUploadAwards> validateGrantingAuthorityMissingErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> (award.getGrantingAuthorityName()==null)).collect(Collectors.toList());
		if(validateGrantingAuthorityMissingErrorRecordsList.size()>0) {
			validationGrantingAuthorityResultList = validateGrantingAuthorityMissingErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("GA Name"),
						"You must enter the name of the public authority."))
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("Title"),
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("SC Number"),
						"The subsidy control number must start with SC, followed by 5 digits."))
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

		log.info("Public Authority - String list size " + grantingAuthorityNamesList.size());

		List<BulkUploadAwards> grantingAuthorityNameErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getGrantingAuthorityName() != null
						&& !grantingAuthorityNamesList.contains(award.getGrantingAuthorityName()))
				.collect(Collectors.toList());

		List<ValidationErrorResult> validationGrantingAuthorityNameResultList = new ArrayList<>();
		validationGrantingAuthorityNameResultList = grantingAuthorityNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("GA Name"),
						"You must enter the name of the public authority."))
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
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID"),
						"National ID  must be less than  or 10 characters"))
				.collect(Collectors.toList());

		if(nationsIdMissingRecordsList.size()>0) {
			validationNationalIdResultList = nationsIdMissingRecordsList.stream()
					.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID"),
							"National ID  field is Mandatory."))
					.collect(Collectors.toList());
		}

		List<BulkUploadAwards> nationsIdVATErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("VAT Number") && (award.getNationalId()!=null && (award.getNationalId().length() != 9 || !award.getNationalId().matches("[0-9]+")))).collect(Collectors.toList());

		if(nationsIdVATErrorRecordsList.size()>0) {
		validationNationalIdResultList.addAll(nationsIdVATErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID"),
						"The VAT number must be 9 digits."))
				.collect(Collectors.toList()));
		}

		List<BulkUploadAwards> nationsIdUTRErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("UTR Number") && (award.getNationalId()!=null && (award.getNationalId().length() != 10 ||  !award.getNationalId().matches("[0-9]+")))).collect(Collectors.toList());

		if(nationsIdUTRErrorRecordsList.size()>0) {
		validationNationalIdResultList.addAll(nationsIdUTRErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID"),
						"The UTR number must be 10 digits."))
				.collect(Collectors.toList()));
		}

		List<BulkUploadAwards> nationsIdCharityErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType()
						.equalsIgnoreCase("Charity Number") && (award.getNationalId()!=null &&
						(award.getNationalId().length() > 8 ||
								!award.getNationalId().matches("[0-9]+")))).collect(Collectors.toList());

		validationNationalIdResultList.addAll(nationsIdCharityErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID"),
						"The charity commission number must be 8 digits. This may include a dash (-) before the last digit."))
				.collect(Collectors.toList()));


		List<BulkUploadAwards> nationsIdCompanyNumberFormatErrorRecordsList = bulkUploadAwards.stream()
				.filter(award -> award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("Company Registration Number")
						&& (!AwardUtils.validateCompanyNumber(award.getNationalId()))).collect(Collectors.toList());

		validationNationalIdResultList.addAll(nationsIdCompanyNumberFormatErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), columnMapping.get("ID"),
						"The company number must be 8 characters using only letters and numbers."))
				.collect(Collectors.toList()));

		return validationNationalIdResultList;

	}
	/**
	 *
	 */

	private boolean isLegalGrantingDateWithinSchemeDate (BulkUploadAwards award, SubsidyMeasure sm){
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
		boolean legalGrantingDateWithinSchemeDate = false;

		try {
			if (award.getLegalGrantingDate() == null) {
				return false;
			}
			if (sm.getEndDate() == null && !sdf.parse(award.getLegalGrantingDate()).before(sm.getStartDate())){
				legalGrantingDateWithinSchemeDate = true;
			} else if (sm.getEndDate() == null && sdf.parse(award.getLegalGrantingDate()).before(sm.getStartDate())) {
				legalGrantingDateWithinSchemeDate = false;
			} else {
				legalGrantingDateWithinSchemeDate = !(sdf.parse(award.getLegalGrantingDate()).after(sm.getEndDate()) ||
						sdf.parse(award.getLegalGrantingDate()).before(sm.getStartDate()));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return legalGrantingDateWithinSchemeDate;
	}
}
