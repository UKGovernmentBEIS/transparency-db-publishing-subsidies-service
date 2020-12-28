package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AddAwardService {

	@Autowired
	private AwardService awardService;

	/*
	 * the below method validate the Award given in  request.
	 */
	public ValidationResult validateAward(SingleAward award) {

		try {

			
			log.info("input Award is "+award);

			// Validation National Id length check
			List<ValidationErrorResult> nationalIdMisingList = validateNationaIdAwards(award);

			List<ValidationErrorResult> nationalIdTypeMisingList = validateNationalIdType(award);

			// Validation - Beneficiary check
			List<ValidationErrorResult> beneficiaryMisingList = validateBeneficiaryAwards(award);

			// TODO - Validation - Make call to process API to get data for subsidy measure
			/*
			 * 3) If incorrect SC number is entered, user system should throw an error
			 * Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> scNumberNameCheckList = validateScNumberScTitle(award);

			List<ValidationErrorResult> subsidyControlNumberLengthList = validateSubsidyNumberLength(award);

			List<ValidationErrorResult> subsidyControlNumberMismatchList = validateSubsidyControlNumber(
					award);

			List<ValidationErrorResult> subsidyMeasureTitleNameLengthList = validateSubsidyMeasureNameLength(
					award);

			List<ValidationErrorResult> subsidyPurposeCheckList = validateSubsidyPurpose(award);

			/*
			 * validate benificiary name length > 255 Validation Error - Row 6 - Incorrect
			 * SC Number - Correct one SC10002
			 */

			List<ValidationErrorResult> beneficiaryNameErrorList = validateBeneficiaryName(award);

			/*
			 *  validate Granting Authority name length > 255 Validation Error - Row 6 -
			 * Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> grantingAuthorityNameErrorList = validateGrantingAuthorityName(
					award);

			/*
			 * validate Granting Authority exists in database or not Validation Error - Row
			 * 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> grantingAuthorityErrorList = validateGrantingAuthorityNameinDb(
					award);
			
			List<ValidationErrorResult> SubsidyElementFullAmountErrorList = validateSubsidyElementFullAmount(
					award);
			
			 
			/*
			 * validate Size of Organization
			 * 
			 */
			List<ValidationErrorResult> sizeOfOrgErrorList = validateSizeOfOrg(award);
			/*
			 * validate Spending Region
			 * 
			 */
			List<ValidationErrorResult> spendingRegionErrorList = validateSpendingRegion(award);
			/*
			 * validate Spending Sector
			 * 
			 */
			List<ValidationErrorResult> spendingSectorErrorList = validateSpendingSector(award);

			/*
			 * validate Goods or Service
			 * 
			 */

			List<ValidationErrorResult> goodsOrServiceErrorList = validateGoodsOrService(award);
			
			/*
			 * validate Legal granting Date
			 * 
			 */

			List<ValidationErrorResult> legalGrantingDateErrorList = validateLegalGrantingDate(award);
			
			/*
			 * validate Goods or Service
			 * 
			 */

			List<ValidationErrorResult> SubsidyInstrumentErrorList = validateSubsidyInstrument(award);
			
			// Merge lists of Validation Errors
			List<ValidationErrorResult> validationErrorResultList = Stream
					.of(scNumberNameCheckList, subsidyMeasureTitleNameLengthList, subsidyPurposeCheckList,
							nationalIdTypeMisingList, nationalIdMisingList, beneficiaryNameErrorList,
							beneficiaryMisingList, subsidyControlNumberLengthList, subsidyControlNumberMismatchList,
							grantingAuthorityNameErrorList, grantingAuthorityErrorList, sizeOfOrgErrorList,
							spendingRegionErrorList, spendingSectorErrorList, goodsOrServiceErrorList,SubsidyInstrumentErrorList,legalGrantingDateErrorList,SubsidyElementFullAmountErrorList)
					.flatMap(x -> x.stream()).collect(Collectors.toList());

		

			log.info("Final validation errors list ...printing list of errors - start");

			ValidationResult validationResult = new ValidationResult();
			validationResult.setValidationErrorResult(validationErrorResultList);
			//validationResult.setTotalRows(bulkUploadAwards.size());
			/*validationResult.setErrorRows(validationErrorResultList.size());
			validationResult.setMessage((validationErrorResultList.size() > 0) ? "Validation Errors in Uploaded file"
					: "No errors in Uploaded file");

			log.info("Final validation Result object ...printing validationResult - start");
			validationErrorResultList.stream().forEach(System.out::println);*/

			if (validationResult.getValidationErrorResult().size() == 0) {
				
				log.info("No validation error in bulk excel template");

				awardService.createAward(award);

				log.info("After calling process api - response = ");
				
				validationResult
						.setMessage((true ? "All Awards saved in Database" : "Error while saving awards in Database"));
			}
			return validationResult;

		} catch (Exception e) {
			e.printStackTrace();
			
			log.error(" Error in validationResult **** "+e);
			throw new RuntimeException("Fail to store data : " + e.getMessage());
		}

	}

	/*
	 * 
	 * the below method validate either SC number or Sc Title exist in the file.
	 */
	private List<ValidationErrorResult> validateScNumberScTitle(SingleAward award) {

		/*
		 * validation for eaither Sc number or Sc Title must be exist in the request.
		 */
		List<ValidationErrorResult> validationScNumberScTitlResultList = new ArrayList<>();
		if(award.getSubsidyControlNumber() == null || award.getSubsidyControlTitle()==null){
			validationScNumberScTitlResultList.add(new ValidationErrorResult("Either Subsidy Control number or Subsidy title field is mandatory."));
		}

		log.info("Validation Result Error list - Either Subsidy Control number or Subsidy title should enter = "
				+ validationScNumberScTitlResultList);

		return validationScNumberScTitlResultList;
	}

	/*
	 * 
	 * the below method validate Size of Organization entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSizeOfOrg(SingleAward award) {

		/*
		 * validation for Size of Organization entered in the input file.
		 */
		List<ValidationErrorResult> validationSizeOfOrgErrorListResultList = new ArrayList<>();
		if(award.getOrgSize() == null || StringUtils.isEmpty(award.getOrgSize())){
			validationSizeOfOrgErrorListResultList.add(new ValidationErrorResult("Size of Organization  field is mandatory."));
		}

		
		log.info("Validation Result Error list - Size of Organizationshould enter = "
				+ validationSizeOfOrgErrorListResultList);

		return validationSizeOfOrgErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Subsidy Purpose entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSubsidyPurpose(SingleAward award) {

		/*
		 * validation for Size of Organization entered in the input file.
		 */
		List<ValidationErrorResult> validationSubsidyObjectiveErrorListResultList = new ArrayList<>();
		
		if(award.getSubsidyObjective() == null || StringUtils.isEmpty(award.getSubsidyObjective())) {
			validationSubsidyObjectiveErrorListResultList.add(new ValidationErrorResult("Subsidy Objective  field is mandatory."));
		}
		if(award.getSubsidyObjective()!=null && award.getSubsidyObjectiveOther() .length() > 255){
			validationSubsidyObjectiveErrorListResultList.add(new ValidationErrorResult("Subsidy Objective- other field lenth is > 255 characters."));
		}
			
		
		log.info("Validation Result Error list - validateSubsidyObjective = "
				+ validationSubsidyObjectiveErrorListResultList);

		return validationSubsidyObjectiveErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Spending Region entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSpendingRegion(SingleAward award) {

		/*
		 * validation for Size of SpendingRegion entered in the input file.
		 */

		List<ValidationErrorResult> validationspendingRegionErrorListResultList = new ArrayList<>();
		
		if(award.getSpendingRegion() == null || StringUtils.isEmpty(award.getSpendingRegion())) {
			validationspendingRegionErrorListResultList.add(new ValidationErrorResult("Spending Region  field is mandatory."));
		}
		if(award.getSpendingRegion().length() > 255){
			validationspendingRegionErrorListResultList.add(new ValidationErrorResult("Spending Region other  field length > 255 charactres."));
		}
		
		
		log.info("Validation Result Error list - Spending Region should enter = "
				+ validationspendingRegionErrorListResultList);

		return validationspendingRegionErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Spending sector entered or not in the file.
	 */
	private List<ValidationErrorResult> validateSpendingSector(SingleAward award) {

		/*
		 * validation for Size of Spending Sector entered in the input file.
		 */
		List<ValidationErrorResult> validationspendingSectorErrorListResultList = new ArrayList<>();

		if(award.getSpendingSector() == null || StringUtils.isEmpty(award.getSpendingSector())) {
			validationspendingSectorErrorListResultList.add(new ValidationErrorResult("Spending Sector  field is mandatory."));
		}
		
		log.info("Validation Result Error list - Spending Sector  should enter = "
				+ validationspendingSectorErrorListResultList);

		return validationspendingSectorErrorListResultList;
	}
	
	
	/*
	 * 
	 * the below method validate Subsidy Amount field.
	 */
	private List<ValidationErrorResult> validateSubsidyElementFullAmount(SingleAward award) {

		/*
		 * validation for SubsidyElementFullAmount entered in the input file.
		 */
		List<ValidationErrorResult> validationSubsidyAmountExactErrorResultList = new ArrayList<>();
		if((award.getSubsidyInstrument()!=null && !award.getSubsidyInstrument().startsWith("Tax"))&& (award.getSubsidyAmountExact() == null || StringUtils.isEmpty(award.getSubsidyAmountExact()))) {
			validationSubsidyAmountExactErrorResultList.add(new ValidationErrorResult("Subsidy Element Full Amount is mandatory."));
		}
		
		log.info("Validation Result Error list - Subsidy Element Full Amount = "
				+ validationSubsidyAmountExactErrorResultList);

		return validationSubsidyAmountExactErrorResultList;
	}
	
	
	/*
	 * 
	 * the below method validate SubsidyInstrument .
	 */
	private List<ValidationErrorResult> validateSubsidyInstrument(SingleAward award) {

		/*
		 * validation for SubsidyInstrument mandatory check .
		 */
		List<ValidationErrorResult> validationSubsidyInstrumentErrorListResultList = new ArrayList<>();
		if(award.getSubsidyInstrument()== null || StringUtils.isEmpty(award.getSubsidyInstrument())) {
			validationSubsidyInstrumentErrorListResultList.add(new ValidationErrorResult("Subsidy Instrument is Mandatory."));
		}
		
		if(award.getSubsidyInstrumentOther()!=null && award.getSubsidyInstrumentOther().length() > 255){
			
			validationSubsidyInstrumentErrorListResultList.add(new ValidationErrorResult("Subsidy Instrument-other length > 255 characters."));
		}
		
		if((award.getSubsidyInstrument()!=null && award.getSubsidyInstrument().startsWith("Tax"))&& (award.getSubsidyAmountRange()==null || StringUtils.isEmpty(award.getSubsidyAmountRange()))) {
			
			validationSubsidyInstrumentErrorListResultList.add(new ValidationErrorResult("Subsidy Element Full Amount Range is mandatory when Subsidy Instrument is Tax Measure ."));
		}
		
		
		log.info("Validation Result Error list - Subsidy Instrument "
				+ validationSubsidyInstrumentErrorListResultList);

		return validationSubsidyInstrumentErrorListResultList;
	}
 
	/*
	 * 
	 * the below method validate Goods or Service entered or not in the file.
	 */
	private List<ValidationErrorResult> validateGoodsOrService(SingleAward award) {

		/*
		 * validation for GoodsOrService entered in the input file.
		 */
		List<ValidationErrorResult> validationgoodsOrServiceErrorListResultList = new ArrayList<>();

		if(award.getGoodsOrServices() == null || StringUtils.isEmpty(award.getGoodsOrServices())){
			validationgoodsOrServiceErrorListResultList.add(new ValidationErrorResult("Goods or Service  field is mandatory"));
		}
		
		log.info("Validation Result Error list - Goods or Service should enter = "
				+ validationgoodsOrServiceErrorListResultList);

		return validationgoodsOrServiceErrorListResultList;
	}

	
	
	/*
	 * 
	 * the below method validate Granting date .
	 */
	private List<ValidationErrorResult> validateLegalGrantingDate(SingleAward award) {

		/*
		 * validation for Legal Granting Date  entered in the input file.
		 */
		List<ValidationErrorResult> validationlegalGrantingDateErrorListResultList = new ArrayList<>();
		if(award.getLegalGrantingDate() == null || StringUtils.isEmpty(award.getLegalGrantingDate())) {
			validationlegalGrantingDateErrorListResultList.add(new ValidationErrorResult("Legal Granting Date is Mandatory."));
		}
		
		log.info("Validation Result Error list - Legal Granting Date is Mandatory = "
				+ validationlegalGrantingDateErrorListResultList);

		return validationlegalGrantingDateErrorListResultList;
	}
	
	
	private List<ValidationErrorResult> validateSubsidyControlNumber(SingleAward award) {

		log.info("Calling awardService.getAllSubsidyMeasures()... - start");
		
		List<SubsidyMeasure> smList = awardService.getAllSubsidyMeasures();
		
		log.info("Calling processServiceproxy.getAllSubsidyMeasures()... - end");

		List<String> subsidyControlNumberTitleList = smList.stream().map(sm -> sm.getScNumber())
				.collect(Collectors.toList());

		log.info("subsidyControlNumberTitleList - String list " + subsidyControlNumberTitleList);
		//
		List<ValidationErrorResult> validationSubsidyControlNumberResultList = new ArrayList<>();
		
		if(award.getSubsidyControlNumber() != null && award.getSubsidyControlTitle() != null
						&& !subsidyControlNumberTitleList.contains(award.getSubsidyControlNumber())) {
			validationSubsidyControlNumberResultList.add(new ValidationErrorResult("Subsidy Control doest not exists."));
			
		}
		
		if(smList.stream().noneMatch(
				bulkAward -> ((bulkAward.getScNumber().equals(award.getSubsidyControlNumber()))
						&& (bulkAward.getSubsidyMeasureTitle().equals(award.getSubsidyControlTitle()))))){
			validationSubsidyControlNumberResultList.add(new ValidationErrorResult("Subsidy Control number does not match with title."));
			
					
				}
		
			
		
		log.info(
				"Back validation-3 - subsidy measure title mismatch check...printing list of awards with subsidy measure number error - end");

		// validation scnumber with sctitle.

		
		log.info("Validation Result Error list - Subsidy Measure Number mismatch error = "
				+ validationSubsidyControlNumberResultList);

		return validationSubsidyControlNumberResultList;
	}

	/*
	 * the below method validate the nationalId
	 */

	private List<ValidationErrorResult> validateNationalIdType(SingleAward award) {

		List<ValidationErrorResult> validationNationalIdResultList = new ArrayList<>();
		if(award.getNationalIdType() == null) {
			validationNationalIdResultList.add(new ValidationErrorResult("National Id Type is mandatory."));
		}
		

		log.info("Validation Result Error list - National ID  missing error = " + validationNationalIdResultList);

		return validationNationalIdResultList;
	}

	private List<ValidationErrorResult> validateBeneficiaryAwards(SingleAward award) {

		/*
		 * 2) If the ‘National ID type’ is a UTR or a VAT number, then validate if the
		 * beneficiary name is entered and if not return an error as above. Validation
		 * Error - Row 9 - Beneficiary missing
		 * 
		 */
		// TODO - Validation 2 - National ID Type UTR/ VAT, then check beneficiary
		// present - implement filter method
		
		List<ValidationErrorResult> validationBeneficiaryIdResultList = new ArrayList<>();
		if((award.getNationalIdType()!=null) && (award.getNationalIdType().equals("UTR Number")
						|| award.getNationalIdType().equals("VAT Number"))
						&& (award.getBeneficiaryName() == null)) {
			validationBeneficiaryIdResultList.add(new ValidationErrorResult("Enter the Beneficiary Name."));
		}
		
		log.info(
				"Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);

		return validationBeneficiaryIdResultList;
	}

	/*
	 *
	 * the below method validate the benificiary name length (>255 chars)
	 */
	private List<ValidationErrorResult> validateBeneficiaryName(SingleAward award) {

				
		List<ValidationErrorResult> validationBeneficiaryIdResultList = new ArrayList<>();
		
		if(award.getBeneficiaryName()!=null && award.getBeneficiaryName().length() > 255) {
			validationBeneficiaryIdResultList.add(new ValidationErrorResult("Beneficiary name is too long, it should be 255 characters or fewer."));
		}

		
		log.info(
				"Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);

		return validationBeneficiaryIdResultList;
	}

	/*
	 *
	 * the below method validate the AuthorityName name length (>255 chars)
	 */
	private List<ValidationErrorResult> validateGrantingAuthorityName(SingleAward award) {

		List<ValidationErrorResult> validationGrantingAuthorityResultList = new ArrayList<>();
		
		if(award.getGrantingAuthorityName()==null) {
			validationGrantingAuthorityResultList.add(new ValidationErrorResult("Granting Authority name is Mandatory."));
		}
		
		if(award.getGrantingAuthorityName()!=null && award.getGrantingAuthorityName().length() > 255){
			validationGrantingAuthorityResultList.add(new ValidationErrorResult("Please enter a valid Granting Authority."));
		}
		
		
		log.info("Validation Result Error list - Granting Authority missing error = "
				+ validationGrantingAuthorityResultList);

		return validationGrantingAuthorityResultList;
	}

	/*
	 *
	 * the below method validate the subsidy Measure Title length (>255 chars)
	 */
	private List<ValidationErrorResult> validateSubsidyMeasureNameLength(SingleAward award) {

		List<ValidationErrorResult> validationSubsidyMeasureNameResultList = new ArrayList<>();
		if(award.getSubsidyControlTitle().length() > 255) {
			validationSubsidyMeasureNameResultList.add(new ValidationErrorResult("Subsidy Measure Title must be 255 characters or fewer"));
		}
		
		log.info("Validation Result Error list - Subsidy Measure Title must be 255 characters or fewer = "
				+ validationSubsidyMeasureNameResultList);

		return validationSubsidyMeasureNameResultList;
	}

	/*
	 *
	 * the below method validate the Subsidy number length (>7 chars)
	 */
	private List<ValidationErrorResult> validateSubsidyNumberLength(SingleAward award) {
		
		List<ValidationErrorResult> validationSubsidyNumberLengthResultList = new ArrayList<>();
		if(award.getSubsidyControlNumber().length() > 7){
			validationSubsidyNumberLengthResultList.add(new ValidationErrorResult("Subsidy Control Number must be 7 characters or fewer."));
		}

		log.info("Validation Result Error list - Beneficiary Name missing error = "
				+ validationSubsidyNumberLengthResultList);

		return validationSubsidyNumberLengthResultList;
	}

	/*
	 *
	 * the below method validate the AuthorityName name exist in data base or not
	 * (table=Granting_Authority)
	 */
	private List<ValidationErrorResult> validateGrantingAuthorityNameinDb(SingleAward award) {

		log.info("Calling processServiceproxy.getAllGrantingAuthorities()... - start");
		
		List<GrantingAuthority> grantingAuthorityList = awardService.getAllGrantingAuthorities();
		
		log.info("smList = " + grantingAuthorityList);
		log.info("Calling processServiceproxy.getAllSubsidyMeasures()... - end");

		List<String> grantingAuthorityNamesList = grantingAuthorityList.stream()
				.map(grantingAuthority -> grantingAuthority.getGrantingAuthorityName()).collect(Collectors.toList());

		log.info("Granting Authority - String list " + grantingAuthorityNamesList);

		List<ValidationErrorResult> validationGrantingAuthorityNameResultList = new ArrayList<>();
		
		if(award.getGrantingAuthorityName() != null
						&& !grantingAuthorityNamesList.contains(award.getGrantingAuthorityName())){
			validationGrantingAuthorityNameResultList.add(new ValidationErrorResult("Please enter a valid Granting Authority."));
			
		}
		
			
		
		log.info("Validation Result Error list - Granting Authority Name error = "
				+ validationGrantingAuthorityNameResultList);

		return validationGrantingAuthorityNameResultList;
	}

	private List<ValidationErrorResult> validateNationaIdAwards(SingleAward award) {

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
		List<ValidationErrorResult> validationNationalIdResultList = new ArrayList<>();
		
		if(award.getNationalId()==null) {
			validationNationalIdResultList.add(new ValidationErrorResult("National ID  is Mandatory."));
			log.info("validation fail National ID  is Mandatory.");
		}
		
		
		if(award.getNationalId().length() > 10) {
			validationNationalIdResultList.add(new ValidationErrorResult("National ID  must be 10 characters or fewer."));
			log.info("validation fail National ID  must be 10 characters or fewer");
		}
		
		if(award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("VAT Number") && (award.getNationalId().length() > 9 || !award.getNationalId().matches("[0-9]+"))){
			validationNationalIdResultList.add(new ValidationErrorResult("invalid VAT number."));
			log.info("invalid VAT number.");
		}
		if(award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("UTR Number") && !award.getNationalId().matches("[0-9]+")) {
			validationNationalIdResultList.add(new ValidationErrorResult("invalid UTR Number."));
			log.info("invalid UTR number.");
		}
		
		

		log.info("Validation Result Error list - National ID  = " + validationNationalIdResultList);

		return validationNationalIdResultList;

	}

	
}
