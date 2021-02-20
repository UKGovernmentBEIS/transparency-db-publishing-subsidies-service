package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.SingleAwardValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.SingleAwardValidationResults;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AddAwardService {

	@Autowired
	private AwardService awardService;

	@Value("${loggingComponentName}")
	private String loggingComponentName;

	/*
	 * the below method validate the Award given in  request.
	 */
	public SingleAwardValidationResults validateAward(SingleAward award, String role) {

		//try {

			log.info("{} :: Inside validateAward Award", loggingComponentName);

			// Validation National Id length check
			List<SingleAwardValidationResult> nationalIdMisingList = validateNationalIdAwards(award);

			List<SingleAwardValidationResult> nationalIdTypeMisingList = validateNationalIdType(award);

			// Validation - Beneficiary check
			List<SingleAwardValidationResult> beneficiaryMisingList = validateBeneficiaryAwards(award);

			/*
			 * 3) If incorrect SC number is entered, user system should throw an error
			 * Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<SingleAwardValidationResult> scNumberNameCheckList = validateScNumberScTitle(award);

			//List<SingleAwardValidationResult> subsidyControlNumberLengthList = validateSubsidyNumberLength(award);

			List<SingleAwardValidationResult> subsidyControlNumberMismatchList = validateSubsidyControlNumber(
					award);

			List<SingleAwardValidationResult> subsidyMeasureTitleNameLengthList = validateSubsidyMeasureNameLength(
					award);

			List<SingleAwardValidationResult> subsidyPurposeCheckList = validateSubsidyPurpose(award);

			/*
			 * validate benificiary name length > 255 Validation Error - Row 6 - Incorrect
			 * SC Number - Correct one SC10002
			 */

			List<SingleAwardValidationResult> beneficiaryNameErrorList = validateBeneficiaryName(award);

			/*
			 *  validate Granting Authority name length > 255 Validation Error - Row 6 -
			 * Incorrect SC Number - Correct one SC10002
			 */
			List<SingleAwardValidationResult> grantingAuthorityNameErrorList = validateGrantingAuthorityName(
					award);

			/*
			 * validate Granting Authority exists in database or not Validation Error - Row
			 * 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<SingleAwardValidationResult> grantingAuthorityErrorList = validateGrantingAuthorityNameinDb(
					award);
			
			List<SingleAwardValidationResult> SubsidyElementFullAmountErrorList = validateSubsidyElementFullAmount(
					award);
			
			 
			/*
			 * validate Size of Organization
			 * 
			 */
			List<SingleAwardValidationResult> sizeOfOrgErrorList = validateSizeOfOrg(award);
			/*
			 * validate Spending Region
			 * 
			 */
			List<SingleAwardValidationResult> spendingRegionErrorList = validateSpendingRegion(award);
			/*
			 * validate Spending Sector
			 * 
			 */
			List<SingleAwardValidationResult> spendingSectorErrorList = validateSpendingSector(award);

			/*
			 * validate Goods or Service
			 * 
			 */

			List<SingleAwardValidationResult> goodsOrServiceErrorList = validateGoodsOrService(award);
			
			/*
			 * validate Legal granting Date
			 * 
			 */

			List<SingleAwardValidationResult> legalGrantingDateErrorList = validateLegalGrantingDate(award);
			
			/*
			 * validate Goods or Service
			 * 
			 */

			List<SingleAwardValidationResult> SubsidyInstrumentErrorList = validateSubsidyInstrument(award);
			
			// Merge lists of Validation Errors
			List<SingleAwardValidationResult> validationErrorResultList = Stream
					.of(scNumberNameCheckList, subsidyMeasureTitleNameLengthList, subsidyPurposeCheckList,
							nationalIdTypeMisingList, nationalIdMisingList, beneficiaryNameErrorList,
							beneficiaryMisingList, subsidyControlNumberMismatchList,
							grantingAuthorityNameErrorList, grantingAuthorityErrorList, sizeOfOrgErrorList,
							spendingRegionErrorList, spendingSectorErrorList, goodsOrServiceErrorList,SubsidyInstrumentErrorList,legalGrantingDateErrorList,SubsidyElementFullAmountErrorList)
					.flatMap(x -> x.stream()).collect(Collectors.toList());

		

			log.info("{} :: Final validation errors list ...printing list of errors - start", loggingComponentName);

			SingleAwardValidationResults validationResult = new SingleAwardValidationResults();
			validationResult.setValidationErrorResult(validationErrorResultList);
			if (validationResult.getValidationErrorResult().size() == 0) {
				
				log.info("{}::No validation error in bulk excel template", loggingComponentName);

				Award savedAward =awardService.createAward(award, role);

				log.info("{} :: After calling process api - response = ", loggingComponentName);
				validationResult.setTotalErrors(0);
				validationResult
						.setMessage((true ? savedAward.getAwardNumber()+" Award saved in Database" : "Error while saving awards in Database"));
			}else {
				validationResult.setTotalErrors(validationResult.getValidationErrorResult().size());
				validationResult.setMessage("validation error");
				
			}
			return validationResult;

		/*} catch (Exception e) {
			log.error("{}:: Error in validationResult **** {}",loggingComponentName, e);
			throw new RuntimeException("Fail to store data : " + e.getMessage());
		}*/

	}

	/*
	 * 
	 * the below method validate either SC number or Sc Title exist in the file.
	 */
	private List<SingleAwardValidationResult> validateScNumberScTitle(SingleAward award) {

		/*
		 * validation for eaither Sc number or Sc Title must be exist in the request.
		 */
		
		List<SingleAwardValidationResult> validationScNumberScTitlResultList = new ArrayList<>();
		if(StringUtils.isEmpty(award.getSubsidyControlNumber()) && StringUtils.isEmpty(award.getSubsidyControlTitle())){
			validationScNumberScTitlResultList.add(new SingleAwardValidationResult("subsidyControlNumber or subsidyControlTitle","Either Subsidy Control number or Subsidy title field is mandatory."));
		}

		log.info("{} ::Validation Result Error list - Either Subsidy Control number or Subsidy title should enter = {}",
				loggingComponentName,validationScNumberScTitlResultList);

		return validationScNumberScTitlResultList;
	}

	/*
	 * 
	 * the below method validate Size of Organization entered or not in the file.
	 */
	private List<SingleAwardValidationResult> validateSizeOfOrg(SingleAward award) {

		/*
		 * validation for Size of Organization entered in the input file.
		 */
		List<SingleAwardValidationResult> validationSizeOfOrgErrorListResultList = new ArrayList<>();
		if(award.getOrgSize() == null || StringUtils.isEmpty(award.getOrgSize())){
			validationSizeOfOrgErrorListResultList.add(new SingleAwardValidationResult("orgSize","Size of Organization field is mandatory."));
		}

		
		log.info("{} ::Validation Result Error list - Size of Organization should enter ={} ", loggingComponentName
				,validationSizeOfOrgErrorListResultList);

		return validationSizeOfOrgErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Subsidy Purpose entered or not in the file.
	 */
	private List<SingleAwardValidationResult> validateSubsidyPurpose(SingleAward award) {

		/*
		 * validation for Size of Organization entered in the input file.
		 */
		List<SingleAwardValidationResult> validationSubsidyObjectiveErrorListResultList = new ArrayList<>();
		
		if(award.getSubsidyObjective() == null || StringUtils.isEmpty(award.getSubsidyObjective())) {
			validationSubsidyObjectiveErrorListResultList.add(new SingleAwardValidationResult("subsidyObjective","Subsidy Objective field is mandatory."));
		}
		if(!StringUtils.isEmpty(award.getSubsidyObjectiveOther()) && award.getSubsidyObjectiveOther() .length() > 255){
			validationSubsidyObjectiveErrorListResultList.add(new SingleAwardValidationResult("Subsidy Objective- other","Subsidy Objective- other field lenth is > 255 characters."));
		}
			
		if(award.getSubsidyObjective()!= null && ("Other".equalsIgnoreCase(award.getSubsidyObjective()) && (award.getSubsidyObjectiveOther()==null || StringUtils.isEmpty(award.getSubsidyObjectiveOther())))){
			
			validationSubsidyObjectiveErrorListResultList.add(new SingleAwardValidationResult("Subsidy Objective- other","Subsidy Objective- other field is mandatory when Subsidy Objective is Other."));
		}
		log.info("{} ::Validation Result Error list - validateSubsidyObjective = ",loggingComponentName);

		return validationSubsidyObjectiveErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Spending Region entered or not in the file.
	 */
	private List<SingleAwardValidationResult> validateSpendingRegion(SingleAward award) {

		/*
		 * validation for Size of SpendingRegion entered in the input file.
		 */

		List<SingleAwardValidationResult> validationspendingRegionErrorListResultList = new ArrayList<>();
		
		if(award.getSpendingRegion() == null || StringUtils.isEmpty(award.getSpendingRegion())) {
			validationspendingRegionErrorListResultList.add(new SingleAwardValidationResult("spendingRegion","Spending Region field is mandatory."));
		}
		if(award.getSpendingRegion()!=null && award.getSpendingRegion().length() > 255){
			validationspendingRegionErrorListResultList.add(new SingleAwardValidationResult("spendingRegion","Spending Region other field length > 255 charactres."));
		}
		
		log.info("{} ::Validation Result Error list - Spending Region should enter = ",loggingComponentName);
		return validationspendingRegionErrorListResultList;
	}

	/*
	 * 
	 * the below method validate Spending sector entered or not in the file.
	 */
	private List<SingleAwardValidationResult> validateSpendingSector(SingleAward award) {

		/*
		 * validation for Size of Spending Sector entered in the input file.
		 */
		List<SingleAwardValidationResult> validationspendingSectorErrorListResultList = new ArrayList<>();

		if(award.getSpendingSector() == null || StringUtils.isEmpty(award.getSpendingSector())) {
			validationspendingSectorErrorListResultList.add(new SingleAwardValidationResult("spendingSector","Spending Sector field is mandatory."));
		}
		
		log.info("{} ::Validation Result Error list - Spending Sector  should enter = ",loggingComponentName);

		return validationspendingSectorErrorListResultList;
	}
	
	
	/*
	 * 
	 * the below method validate Subsidy Amount field.
	 */
	private List<SingleAwardValidationResult> validateSubsidyElementFullAmount(SingleAward award) {

		/*
		 * validation for SubsidyElementFullAmount entered in the input file.
		 */
		List<SingleAwardValidationResult> validationSubsidyAmountExactErrorResultList = new ArrayList<>();
		if((award.getSubsidyInstrument()!=null && !award.getSubsidyInstrument().startsWith("Tax"))&& (award.getSubsidyAmountExact() == null || StringUtils.isEmpty(award.getSubsidyAmountExact()))) {
			validationSubsidyAmountExactErrorResultList.add(new SingleAwardValidationResult("subsidyAmountExact","Subsidy Element Full Amount is mandatory."));
		}
		
		if((award.getSubsidyInstrument()!=null && !award.getSubsidyInstrument().startsWith("Tax"))&& (!award.getSubsidyAmountExact().matches("[0-9]+"))) {
			validationSubsidyAmountExactErrorResultList.add(new SingleAwardValidationResult("subsidyAmountExact","Subsidy Element Full Amount is invalid."));
		}
		
		log.info("{} ::Validation Result Error list - Subsidy Element Full Amount = ", loggingComponentName);

		return validationSubsidyAmountExactErrorResultList;
	}
	
	
	/*
	 * 
	 * the below method validate SubsidyInstrument .
	 */
	private List<SingleAwardValidationResult> validateSubsidyInstrument(SingleAward award) {

		/*
		 * validation for SubsidyInstrument mandatory check .
		 */
		List<SingleAwardValidationResult> validationSubsidyInstrumentErrorListResultList = new ArrayList<>();
		if(award.getSubsidyInstrument()== null || StringUtils.isEmpty(award.getSubsidyInstrument())) {
			validationSubsidyInstrumentErrorListResultList.add(new SingleAwardValidationResult("subsidyInstrument","Subsidy Instrument is Mandatory."));
		}
		
		if(award.getSubsidyInstrument()!= null && ("Other".equalsIgnoreCase(award.getSubsidyInstrument()) && (award.getSubsidyInstrumentOther()==null || StringUtils.isEmpty(award.getSubsidyInstrumentOther())))) {
			
			validationSubsidyInstrumentErrorListResultList.add(new SingleAwardValidationResult("SubsidyInstrument-other","Subsidy Instrument-other field is mandatory when Subsidy Instrument is Other."));
		}
		if(award.getSubsidyInstrumentOther()!=null && award.getSubsidyInstrumentOther().length() > 255){
			
			validationSubsidyInstrumentErrorListResultList.add(new SingleAwardValidationResult("SubsidyInstrument-other","Subsidy Instrument-other length > 255 characters."));
		}
		
		if((award.getSubsidyInstrument()!=null && award.getSubsidyInstrument().startsWith("Tax"))&& (award.getSubsidyAmountRange()==null || StringUtils.isEmpty(award.getSubsidyAmountRange()))) {
			
			validationSubsidyInstrumentErrorListResultList.add(new SingleAwardValidationResult("subsidyAmountRange","Subsidy Element Full Amount Range is mandatory when Subsidy Instrument is Tax Measure."));
		}
		
		
		log.info("Validation Result Error list - Subsidy Instrument "
				+ validationSubsidyInstrumentErrorListResultList);

		return validationSubsidyInstrumentErrorListResultList;
	}
 
	/*
	 * 
	 * the below method validate Goods or Service entered or not in the file.
	 */
	private List<SingleAwardValidationResult> validateGoodsOrService(SingleAward award) {

		/*
		 * validation for GoodsOrService entered in the input file.
		 */
		List<SingleAwardValidationResult> validationgoodsOrServiceErrorListResultList = new ArrayList<>();

		if(award.getGoodsOrServices() == null || StringUtils.isEmpty(award.getGoodsOrServices())){
			validationgoodsOrServiceErrorListResultList.add(new SingleAwardValidationResult("goodsOrServices","Goods or Services field is mandatory"));
		}
		
		log.info("Validation Result Error list - Goods or Service should enter = "
				+ validationgoodsOrServiceErrorListResultList);

		return validationgoodsOrServiceErrorListResultList;
	}

	
	
	/*
	 * 
	 * the below method validate Granting date .
	 */
	private List<SingleAwardValidationResult> validateLegalGrantingDate(SingleAward award) {

		/*
		 * validation for Legal Granting Date  entered in the input file.
		 */
		List<SingleAwardValidationResult> validationlegalGrantingDateErrorListResultList = new ArrayList<>();
		if(award.getLegalGrantingDate() == null || StringUtils.isEmpty(award.getLegalGrantingDate())) {
			validationlegalGrantingDateErrorListResultList.add(new SingleAwardValidationResult("legalGrantingDate","Legal Granting Date is Mandatory."));
		}
		
		if((award.getLegalGrantingDate()!= null && !StringUtils.isEmpty(award.getLegalGrantingDate())) && !validGrantingDate(award.getLegalGrantingDate())) {
			validationlegalGrantingDateErrorListResultList.add(new SingleAwardValidationResult("legalGrantingDate","Legal Granting Date inValid."));
		}
		
		
		log.info("Validation Result Error list - Legal Granting Date is Mandatory = "
				+ validationlegalGrantingDateErrorListResultList);

		return validationlegalGrantingDateErrorListResultList;
	}
	
	
	private List<SingleAwardValidationResult> validateSubsidyControlNumber(SingleAward award) {

		log.info("Calling awardService.getAllSubsidyMeasures()... - start");

		List<SubsidyMeasure> smList = awardService.getAllSubsidyMeasures();

		log.info("Calling processServiceproxy.getAllSubsidyMeasures()... - end");

		List<String> subsidyControlNumberTitleList = smList.stream().map(sm -> sm.getScNumber())
				.collect(Collectors.toList());

		log.info("subsidyControlNumberTitleList - String list " + subsidyControlNumberTitleList);
		//
		List<SingleAwardValidationResult> validationSubsidyControlNumberResultList = new ArrayList<>();

		if (!StringUtils.isEmpty(award.getSubsidyControlNumber()) && award.getSubsidyControlNumber() != null && award.getSubsidyControlNumber().length() > 7) {
			validationSubsidyControlNumberResultList.add(new SingleAwardValidationResult("subsidyControlNumber",
					"Subsidy Control Number must be 7 characters or fewer."));
		
		} else if (award.getSubsidyControlNumber() != null && !StringUtils.isEmpty(award.getSubsidyControlNumber())
				&& !subsidyControlNumberTitleList.contains(award.getSubsidyControlNumber())) {
			validationSubsidyControlNumberResultList
					.add(new SingleAwardValidationResult("subsidyControlNumber", "Subsidy Control number does not exists."));

		} else if ((award.getSubsidyControlNumber() != null && !StringUtils.isEmpty(award.getSubsidyControlTitle())) && smList.stream()
				.noneMatch(bulkAward -> ((bulkAward.getScNumber().equals(award.getSubsidyControlNumber()))
						&& (bulkAward.getSubsidyMeasureTitle().equals(award.getSubsidyControlTitle()))))) {
			validationSubsidyControlNumberResultList.add(new SingleAwardValidationResult("subsidyControlNumber",
					"Subsidy Control number does not match with title."));

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

	private List<SingleAwardValidationResult> validateNationalIdType(SingleAward award) {

		List<SingleAwardValidationResult> validationNationalIdResultList = new ArrayList<>();
		if(award.getNationalIdType() == null || StringUtils.isEmpty(award.getNationalIdType())) {
			validationNationalIdResultList.add(new SingleAwardValidationResult("nationalIdType","National Id Type is mandatory."));
		}
		

		log.info("Validation Result Error list - National ID  missing error = " + validationNationalIdResultList);

		return validationNationalIdResultList;
	}

	private List<SingleAwardValidationResult> validateBeneficiaryAwards(SingleAward award) {

		/*
		 * 2) If the ‘National ID type’ is a UTR or a VAT number, then validate if the
		 * beneficiary name is entered and if not return an error as above. Validation
		 * Error - Row 9 - Beneficiary missing
		 * 
		 */
		// TODO - Validation 2 - National ID Type UTR/ VAT, then check beneficiary
		// present - implement filter method
		
		List<SingleAwardValidationResult> validationBeneficiaryIdResultList = new ArrayList<>();
		if((award.getNationalIdType()!=null) && (award.getNationalIdType().equals("UTR Number")
						|| award.getNationalIdType().equals("VAT Number"))
						&& (award.getBeneficiaryName() == null)) {
			validationBeneficiaryIdResultList.add(new SingleAwardValidationResult("beneficiaryName","Enter the Beneficiary Name."));
		}
		
		log.info(
				"Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);

		return validationBeneficiaryIdResultList;
	}

	/*
	 *
	 * the below method validate the benificiary name length (>255 chars)
	 */
	private List<SingleAwardValidationResult> validateBeneficiaryName(SingleAward award) {

				
		List<SingleAwardValidationResult> validationBeneficiaryIdResultList = new ArrayList<>();
		
		if(award.getBeneficiaryName()==null || StringUtils.isEmpty(award.getBeneficiaryName())) {
			
			validationBeneficiaryIdResultList.add(new SingleAwardValidationResult("beneficiaryName","Beneficiary name field is Mandatory."));
		}
		
		if(award.getBeneficiaryName()!=null && award.getBeneficiaryName().length() > 255) {
			validationBeneficiaryIdResultList.add(new SingleAwardValidationResult("beneficiaryName","Beneficiary name is too long, it should be 255 characters or fewer."));
		}

		
		log.info(
				"Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);

		return validationBeneficiaryIdResultList;
	}

	/*
	 *
	 * the below method validate the AuthorityName name length (>255 chars)
	 */
	private List<SingleAwardValidationResult> validateGrantingAuthorityName(SingleAward award) {

		List<SingleAwardValidationResult> validationGrantingAuthorityResultList = new ArrayList<>();
		
		if(award.getGrantingAuthorityName()==null) {
			validationGrantingAuthorityResultList.add(new SingleAwardValidationResult("grantingAuthorityName","Granting Authority name is Mandatory."));
		}
		
		if(award.getGrantingAuthorityName()!=null && award.getGrantingAuthorityName().length() > 255){
			validationGrantingAuthorityResultList.add(new SingleAwardValidationResult("grantingAuthorityName","Granting authority name should be 255 characters or fewer."));
		}
		
		
		log.info("Validation Result Error list - Granting Authority missing error = "
				+ validationGrantingAuthorityResultList);

		return validationGrantingAuthorityResultList;
	}

	/*
	 *
	 * the below method validate the subsidy Measure Title length (>255 chars)
	 */
	private List<SingleAwardValidationResult> validateSubsidyMeasureNameLength(SingleAward award) {

		List<SingleAwardValidationResult> validationSubsidyMeasureNameResultList = new ArrayList<>();
		if(award.getSubsidyControlTitle().length() > 255) {
			validationSubsidyMeasureNameResultList.add(new SingleAwardValidationResult("SubsidyMeasureTitle ","Subsidy Measure Title must be 255 characters or fewer"));
		}
		
		log.info("Validation Result Error list - Subsidy Measure Title must be 255 characters or fewer = "
				+ validationSubsidyMeasureNameResultList);

		return validationSubsidyMeasureNameResultList;
	}

	/*
	 *
	 * the below method validate the Subsidy number length (>7 chars)
	 */
	private List<SingleAwardValidationResult> validateSubsidyNumberLength(SingleAward award) {
		
		List<SingleAwardValidationResult> validationSubsidyNumberLengthResultList = new ArrayList<>();
		if(award.getSubsidyControlNumber()!=null && award.getSubsidyControlNumber().length() > 7){
			validationSubsidyNumberLengthResultList.add(new SingleAwardValidationResult("subsidyControlNumber","Subsidy Control Number must be 7 characters or fewer."));
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
	private List<SingleAwardValidationResult> validateGrantingAuthorityNameinDb(SingleAward award) {

		log.info("{} ::Calling processServiceproxy.getAllGrantingAuthorities()... - start", loggingComponentName);
		
		List<GrantingAuthority> grantingAuthorityList = awardService.getAllGrantingAuthorities();
		
		log.info("{} ::Calling processServiceproxy.getAllSubsidyMeasures()... - end",loggingComponentName);

		List<String> grantingAuthorityNamesList = grantingAuthorityList.stream()
				.map(grantingAuthority -> grantingAuthority.getGrantingAuthorityName()).collect(Collectors.toList());


		List<SingleAwardValidationResult> validationGrantingAuthorityNameResultList = new ArrayList<>();
		
		if(award.getGrantingAuthorityName() != null
						&& !grantingAuthorityNamesList.contains(award.getGrantingAuthorityName())){
			validationGrantingAuthorityNameResultList.add(new SingleAwardValidationResult("grantingAuthorityName","Please enter a valid Granting Authority."));
			
		}
		
		log.info("{} :: Validation Result Error list - Granting Authority Name error = ", loggingComponentName);

		return validationGrantingAuthorityNameResultList;
	}

	private List<SingleAwardValidationResult> validateNationalIdAwards(SingleAward award) {

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

		List<SingleAwardValidationResult> validationNationalIdResultList = new ArrayList<>();
		
		if(award.getNationalId()==null || StringUtils.isEmpty(award.getNationalId())) {
			validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId","National ID is Mandatory."));
			log.info("validation fail National ID  is Mandatory.");
		}
		
		
		if(!StringUtils.isEmpty(award.getNationalId())&& award.getNationalId()!=null && award.getNationalId().length() > 10) {
			validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId","National ID must be 10 characters or fewer."));
			log.info("validation fail National ID  must be 10 characters or fewer");
		} else {
		 
			if(!StringUtils.isEmpty(award.getNationalId())&& award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("VAT Number") && (award.getNationalId().length() !=9 || !award.getNationalId().matches("[0-9]+"))){
				validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId","invalid VAT number."));
				log.info("invalid VAT number.");
			}
			if(!StringUtils.isEmpty(award.getNationalIdType())&& award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("UTR Number") && (!award.getNationalId().matches("[0-9]+")|| award.getNationalId().length()!=10 )) {
				validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId","invalid UTR Number."));
				log.info("invalid UTR number.");
			}

			if(!StringUtils.isEmpty(award.getNationalIdType())&& award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("Charity Number") && ((!StringUtils.isEmpty(award.getNationalId())&& award.getNationalId()!=null) && (award.getNationalId().length() > 8 || !award.getNationalId().matches("[0-9]+")))){
				validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId","invalid Charity number."));
				log.info("invalid Charity number.");
			}

			if(!StringUtils.isEmpty(award.getNationalIdType())&& award.getNationalIdType()!=null && award.getNationalIdType().equalsIgnoreCase("Company Registration Number") && ((!StringUtils.isEmpty(award.getNationalId())&& award.getNationalId()!=null) && (!validateCompanyNumber(award.getNationalId())))){
				validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId","invalid Company Registration Number."));
				log.info("invalid Charity number.");
			}
		}

		log.info("{} :: Validation Result Error list - National ID  = ", loggingComponentName);

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
	
	/**
	 * 
	 */
	private boolean validGrantingDate(String grantingDate) {
		boolean isValildDate = true;
		for (int i = 0; i < grantingDate.length(); i++) {
			if ('.' == grantingDate.charAt(i) || Character.isLetter(grantingDate.charAt(i))) {

				isValildDate = false;
			}
		}
		return isValildDate;
	}	
	
}
