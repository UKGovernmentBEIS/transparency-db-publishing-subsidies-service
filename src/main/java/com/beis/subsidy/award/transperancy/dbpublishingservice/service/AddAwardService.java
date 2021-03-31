package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import static com.beis.subsidy.award.transperancy.dbpublishingservice.util.JsonFeignResponseUtil.toResponseEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.feign.GraphAPIFeignClient;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AwardRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.GrantingAuthorityRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.SubsidyMeasureRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.EmailUtils;
import feign.FeignException;
import feign.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import uk.gov.service.notify.NotificationClientException;

@Slf4j
@Service
public class AddAwardService {

	@Autowired
	private AwardService awardService;

	@Autowired
	private SubsidyMeasureRepository smRepository;

	@Autowired
	private AwardRepository awardRepository;

	@Autowired
	private GrantingAuthorityRepository gaRepository;

	@Value("${loggingComponentName}")
	private String loggingComponentName;

	@Autowired
	GraphAPIFeignClient graphAPIFeignClient;

	@Autowired
	Environment environment;

	/*
	 * the below method validate the Award given in  request.
	 */
	public SingleAwardValidationResults validateAward(SingleAward award,UserPrinciple userPrinciple, String accessToken) {

		    log.info("{} :: Inside validateAward Award", loggingComponentName);

			// Validation National Id length check
			List<SingleAwardValidationResult> nationalIdMissingList = validateNationalIdAwards(award);

			List<SingleAwardValidationResult> nationalIdTypeMissingList = validateNationalIdType(award);

			// Validation - Beneficiary check
			List<SingleAwardValidationResult> beneficiaryMissingList = validateBeneficiaryAwards(award);

			/*
			 * 3) If incorrect SC number is entered, user system should throw an error
			 * Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<SingleAwardValidationResult> scNumberNameCheckList = validateScNumberScTitle(award);


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
			List<SingleAwardValidationResult> grantingAuthorityErrorList = validateGrantingAuthorityNameInDb(
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
							nationalIdTypeMissingList, nationalIdMissingList, beneficiaryNameErrorList,
							beneficiaryMissingList, subsidyControlNumberMismatchList,
							grantingAuthorityNameErrorList, grantingAuthorityErrorList, sizeOfOrgErrorList,
							spendingRegionErrorList, spendingSectorErrorList, goodsOrServiceErrorList,
							SubsidyInstrumentErrorList,legalGrantingDateErrorList,SubsidyElementFullAmountErrorList)
					.flatMap(x -> x.stream()).collect(Collectors.toList());

		

			log.info("{} :: Final validation errors list ...printing list of errors - start", loggingComponentName);

			SingleAwardValidationResults validationResult = new SingleAwardValidationResults();
			validationResult.setValidationErrorResult(validationErrorResultList);
			if (validationResult.getValidationErrorResult().size() == 0) {
				
				log.info("{}::No validation error in bulk excel template", loggingComponentName);

				Award savedAward =awardService.createAward(award, userPrinciple.getRole());

				log.info("{} :: After calling process api - response = ", loggingComponentName);
				validationResult.setTotalErrors(0);
				validationResult
						.setMessage(savedAward.getAwardNumber()  + " Award saved in Database");
				//notification call START here

				GrantingAuthority gaObj = gaRepository
						.findByGrantingAuthorityName(userPrinciple.getGrantingAuthorityGroupName());
				String groupId = gaObj != null ? gaObj.getAzureGroupId() : null;
				UserDetailsResponse response =  getUserRolesByGrpId(accessToken,gaObj.getAzureGroupId());
				if (Objects.nonNull(response) && !CollectionUtils.isEmpty(response.getUserProfiles())) {

					List<UserResponse> users= response.getUserProfiles();
					for (UserResponse userResponse : users) {
						if (!org.springframework.util.StringUtils.isEmpty(userResponse.getRoleName()) &&
								userResponse.getRoleName().contains("GrantingAuthorityApprovers")) {
							try {
								log.info("{}::email sending to",loggingComponentName);
								EmailUtils.sendSingleAwardEmail(userResponse.getMail(),userPrinciple.getUserName(),savedAward.getAwardNumber(),environment);
							} catch (NotificationClientException e) {
								log.error("{} :: error in sending feedback mail", loggingComponentName,e);
							}
						}

					}
					//end Notification
				}


			}else {
				validationResult.setTotalErrors(validationResult.getValidationErrorResult().size());
				validationResult.setMessage("validation error");
				
			}
			return validationResult;
	}

	/*
	 * 
	 * the below method validate either SC number or Sc Title exist in the file.
	 */
	private List<SingleAwardValidationResult> validateScNumberScTitle(SingleAward award) {

		/*
		 * validation for either Sc number or Sc Title must be exist in the request.
		 */
		
		List<SingleAwardValidationResult> validationScNumberScTitleResultList = new ArrayList<>();
		if(StringUtils.isEmpty(award.getSubsidyControlNumber()) && StringUtils.isEmpty(award.getSubsidyControlTitle())){
			validationScNumberScTitleResultList.add(new SingleAwardValidationResult("subsidyControlNumber or subsidyControlTitle",
					"Either Subsidy Control number or Subsidy title field is mandatory."));
		}

		log.info("{} ::Validation Result Error list - Either Subsidy Control number or Subsidy title should enter = {}",
				loggingComponentName,validationScNumberScTitleResultList);

		return validationScNumberScTitleResultList;
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
		List<SingleAwardValidationResult> validationSubsidyObjErrorResultList = new ArrayList<>();
		
		if(award.getSubsidyObjective() == null || StringUtils.isEmpty(award.getSubsidyObjective())) {
			validationSubsidyObjErrorResultList.add(new SingleAwardValidationResult("subsidyObjective",
					"Subsidy Objective field is mandatory."));
		}
		if(!StringUtils.isEmpty(award.getSubsidyObjectiveOther()) && award.getSubsidyObjectiveOther() .length() > 255){
			validationSubsidyObjErrorResultList.add(new SingleAwardValidationResult("Subsidy Objective- other",
					"Subsidy Objective- other field length is > 255 characters."));
		}
			
		if(award.getSubsidyObjective()!= null && ("Other".equalsIgnoreCase(award.getSubsidyObjective()) &&
				(award.getSubsidyObjectiveOther()==null || StringUtils.isEmpty(award.getSubsidyObjectiveOther())))){

			validationSubsidyObjErrorResultList.add(new SingleAwardValidationResult("Subsidy Objective- other",
					"Subsidy Objective- other field is mandatory when Subsidy Objective is Other."));
		}
		log.info("{} ::Validation Result Error list - validateSubsidyObjective = ",loggingComponentName);

		return validationSubsidyObjErrorResultList;
	}

	/*
	 * 
	 * the below method validate Spending Region entered or not in the file.
	 */
	private List<SingleAwardValidationResult> validateSpendingRegion(SingleAward award) {

		/*
		 * validation for Size of SpendingRegion entered in the input file.
		 */

		List<SingleAwardValidationResult> validationSpendingRegionErrorResultList = new ArrayList<>();
		
		if(award.getSpendingRegion() == null || StringUtils.isEmpty(award.getSpendingRegion())) {
			validationSpendingRegionErrorResultList.add(new SingleAwardValidationResult("spendingRegion",
					"Spending Region field is mandatory."));
		}
		if(award.getSpendingRegion()!=null && award.getSpendingRegion().length() > 255){
			validationSpendingRegionErrorResultList.add(new SingleAwardValidationResult("spendingRegion",
					"Spending Region other field length > 255 characters."));
		}
		
		log.info("{} ::Validation Result Error list - Spending Region should enter = ",loggingComponentName);
		return validationSpendingRegionErrorResultList;
	}

	/*
	 * 
	 * the below method validate Spending sector entered or not in the file.
	 */
	private List<SingleAwardValidationResult> validateSpendingSector(SingleAward award) {

		/*
		 * validation for Size of Spending Sector entered in the input file.
		 */
		List<SingleAwardValidationResult> validationSpendingSectorErrorResultList = new ArrayList<>();

		if(award.getSpendingSector() == null || StringUtils.isEmpty(award.getSpendingSector())) {
			validationSpendingSectorErrorResultList.add(new SingleAwardValidationResult("spendingSector","Spending Sector field is mandatory."));
		}
		
		log.info("{} ::Validation Result Error list - Spending Sector  should enter = ",loggingComponentName);

		return validationSpendingSectorErrorResultList;
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
		if((award.getSubsidyInstrument()!=null && !award.getSubsidyInstrument().startsWith("Tax"))&&
				(award.getSubsidyAmountExact() == null || StringUtils.isEmpty(award.getSubsidyAmountExact()))) {
			validationSubsidyAmountExactErrorResultList.add(new SingleAwardValidationResult("subsidyAmountExact",
					"Subsidy Element Full Amount is mandatory."));
		}
		
		if((award.getSubsidyInstrument()!=null && !award.getSubsidyInstrument().startsWith("Tax"))&&
				(!award.getSubsidyAmountExact().matches("[0-9]+"))) {

			validationSubsidyAmountExactErrorResultList.add(new SingleAwardValidationResult("subsidyAmountExact",
					"Subsidy Element Full Amount is invalid."));
		}

		if((!StringUtils.isEmpty(award.getSubsidyControlTitle())||!StringUtils.isEmpty(award.getSubsidyControlNumber()))
				&& (!StringUtils.isEmpty(award.getSubsidyInstrument())
				&& !award.getSubsidyInstrument().startsWith("Tax")) &&
				(award.getSubsidyAmountExact().matches("[0-9]+"))) {

			SubsidyMeasure subsidyMeasure = getSubsidyMeasureByScNumberOrMeasureTitle(award);
			if (Objects.nonNull(subsidyMeasure) && validateBudget(subsidyMeasure.getBudget(), award.getSubsidyAmountExact())) {


				validationSubsidyAmountExactErrorResultList.add(new SingleAwardValidationResult("subsidyAmountExact",
						"Subsidy Amount Exact is exceeded over the scheme budget amount."));
			}

			if (Objects.nonNull(subsidyMeasure) && subsidyMeasure.isAdhoc()) {

				List<Award> awards = awardRepository.findBySubsidyMeasure(subsidyMeasure);

				if (!CollectionUtils.isEmpty(awards) & awards.size() > 0) {

					validationSubsidyAmountExactErrorResultList.add(
							new SingleAwardValidationResult("subsidyControlNumber or subsidyControlTitle",
							"Scheme type is adhoc hence subsidyControlNumber cannot  assigned to another award."));
				}

			}

		}
		log.info("{} ::Validation Result Error list - Subsidy Element Full Amount = ", loggingComponentName);

		return validationSubsidyAmountExactErrorResultList;
	}

	private boolean validateBudget(String budget, String subsidyAmountExact) {
		boolean isError = false;
		if(!StringUtils.isEmpty(budget) &&
				Long.valueOf(subsidyAmountExact.trim()) > Long.valueOf(budget.trim())) {

			isError = true;
		}
		return isError;
	}


	/*
	 * 
	 * the below method validate SubsidyInstrument .
	 */
	private List<SingleAwardValidationResult> validateSubsidyInstrument(SingleAward award) {

		/*
		 * validation for SubsidyInstrument mandatory check .
		 */
		List<SingleAwardValidationResult> subsidyInstrumentErrorResultList = new ArrayList<>();
		if(award.getSubsidyInstrument()== null || StringUtils.isEmpty(award.getSubsidyInstrument())) {
			subsidyInstrumentErrorResultList.add(new SingleAwardValidationResult("subsidyInstrument",
					"Subsidy Instrument is Mandatory."));
		}
		
		if(award.getSubsidyInstrument()!= null && ("Other".equalsIgnoreCase(award.getSubsidyInstrument()) &&
				(award.getSubsidyInstrumentOther()==null || StringUtils.isEmpty(award.getSubsidyInstrumentOther())))) {

			subsidyInstrumentErrorResultList.add(new SingleAwardValidationResult("SubsidyInstrument-other",
					"Subsidy Instrument-other field is mandatory when Subsidy Instrument is Other."));
		}
		if(award.getSubsidyInstrumentOther()!=null && award.getSubsidyInstrumentOther().length() > 255){

			subsidyInstrumentErrorResultList.add(new SingleAwardValidationResult("SubsidyInstrument-other",
					"Subsidy Instrument-other length > 255 characters."));
		}
		
		if((award.getSubsidyInstrument()!=null && award.getSubsidyInstrument().startsWith("Tax"))&&
				(award.getSubsidyAmountRange()==null || StringUtils.isEmpty(award.getSubsidyAmountRange()))) {

			subsidyInstrumentErrorResultList.add(new SingleAwardValidationResult("subsidyAmountRange",
					"Subsidy Element Full Amount Range is mandatory when Subsidy Instrument is Tax Measure."));
		}
		if((!StringUtils.isEmpty(award.getSubsidyInstrument()) && award.getSubsidyInstrument().startsWith("Tax"))&&
				!StringUtils.isEmpty((award.getSubsidyAmountRange()))) {
			SubsidyMeasure subsidyMeasure = getSubsidyMeasureByScNumberOrMeasureTitle(award);
			if ( Objects.nonNull(subsidyMeasure) && award.getSubsidyAmountRange().contains("-")) {
				String [] amountRange = award.getSubsidyAmountRange().split("-");

				if ( Long.valueOf(amountRange[0].trim()) > Long.valueOf(subsidyMeasure.getBudget().trim()) ||
						Long.valueOf(amountRange[1].trim()) > Long.valueOf(subsidyMeasure.getBudget().trim())) {

					subsidyInstrumentErrorResultList.add(new SingleAwardValidationResult("subsidyAmountRange",
							"Subsidy Element Full Amount Range is exceeded over the budget amount of scheme."));
				}

			}

		}
		
		log.info("Validation Result Error list - Subsidy Instrument {} size =", subsidyInstrumentErrorResultList);
		return subsidyInstrumentErrorResultList;
	}
 
	/*
	 * 
	 * the below method validate Goods or Service entered or not in the file.
	 */
	private List<SingleAwardValidationResult> validateGoodsOrService(SingleAward award) {

		/*
		 * validation for GoodsOrService entered in the input file.
		 */
		List<SingleAwardValidationResult> validationGoodsOrServiceErrorResultList = new ArrayList<>();

		if(award.getGoodsOrServices() == null || StringUtils.isEmpty(award.getGoodsOrServices())){
			validationGoodsOrServiceErrorResultList.add(new SingleAwardValidationResult("goodsOrServices",
					"Goods or Services field is mandatory"));
		}
		
		log.info("Validation Result Error list - Goods or Service should enter = "
				+ validationGoodsOrServiceErrorResultList.size());

		return validationGoodsOrServiceErrorResultList;
	}

	
	
	/*
	 * 
	 * the below method validate Granting date .
	 */
	private List<SingleAwardValidationResult> validateLegalGrantingDate(SingleAward award) {

		/*
		 * validation for Legal Granting Date  entered in the input file.
		 */
		List<SingleAwardValidationResult> legalGrantingDateErrorsResultList = new ArrayList<>();
		if(award.getLegalGrantingDate() == null || StringUtils.isEmpty(award.getLegalGrantingDate())) {
			legalGrantingDateErrorsResultList.add(new SingleAwardValidationResult("legalGrantingDate",
					"Legal Granting Date is Mandatory."));
		}
		
		if((award.getLegalGrantingDate()!= null && !StringUtils.isEmpty(award.getLegalGrantingDate())) &&
				!validGrantingDate(award.getLegalGrantingDate())) {
			legalGrantingDateErrorsResultList.add(new SingleAwardValidationResult("legalGrantingDate",
					"Legal Granting Date inValid."));
		}
		
		
		log.info("Validation Result Error list - Legal Granting Date is Mandatory = "
				+ legalGrantingDateErrorsResultList);

		return legalGrantingDateErrorsResultList;
	}
	
	
	private List<SingleAwardValidationResult> validateSubsidyControlNumber(SingleAward award) {

		log.info("Calling awardService.getAllSubsidyMeasures()... - start");

		List<SubsidyMeasure> smList = awardService.getAllSubsidyMeasures();

		log.info("Calling process Service proxy.getAllSubsidyMeasures()... - end");

		List<String> subsidyControlNumberTitleList = smList.stream().map(sm -> sm.getScNumber())
				.collect(Collectors.toList());

		log.info("subsidyControlNumberTitleList - String list " + subsidyControlNumberTitleList);
		//
		List<SingleAwardValidationResult> validationScNumberResultList = new ArrayList<>();

		if (!StringUtils.isEmpty(award.getSubsidyControlNumber())  &&
				award.getSubsidyControlNumber().length() > 7) {
			validationScNumberResultList.add(new SingleAwardValidationResult("subsidyControlNumber",
					"Subsidy Control Number must be 7 characters or fewer."));
		
		} else if (award.getSubsidyControlNumber() != null && !StringUtils.isEmpty(award.getSubsidyControlNumber())
				&& !subsidyControlNumberTitleList.contains(award.getSubsidyControlNumber())) {
			validationScNumberResultList
					.add(new SingleAwardValidationResult("subsidyControlNumber",
							"Subsidy Control number does not exists."));

		} else if ((!StringUtils.isEmpty(award.getSubsidyControlNumber())&& !StringUtils.isEmpty(award.getSubsidyControlTitle())) && smList.stream()
				.noneMatch(bulkAward -> ((bulkAward.getScNumber().equals(award.getSubsidyControlNumber()))
						&& (bulkAward.getSubsidyMeasureTitle().equals(award.getSubsidyControlTitle()))))) {
			validationScNumberResultList.add(new SingleAwardValidationResult("subsidyControlNumber",
					"Subsidy Control number does not match with title."));

		} else if(isScNumberStatusActive(smList,award.getSubsidyControlNumber())){

			validationScNumberResultList.add(new SingleAwardValidationResult("subsidyControlNumber",
					"Subsidy Control number is in Inactive status."));
		}

		log.info("Validation Result Error list - Subsidy Measure Number mismatch error size = {} ",
				validationScNumberResultList.size());

		return validationScNumberResultList;
	}

	/*
	 * the below method validate the nationalId
	 */

	private List<SingleAwardValidationResult> validateNationalIdType(SingleAward award) {

		List<SingleAwardValidationResult> validationNationalIdResultList = new ArrayList<>();
		if(award.getNationalIdType() == null || StringUtils.isEmpty(award.getNationalIdType())) {
			validationNationalIdResultList.add(new SingleAwardValidationResult("nationalIdType",
					"National Id Type is mandatory."));
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

		// present - implement filter method
		
		List<SingleAwardValidationResult> validationBeneficiaryIdResultList = new ArrayList<>();
		if((award.getNationalIdType()!=null) && (award.getNationalIdType().equals("UTR Number")
						|| award.getNationalIdType().equals("VAT Number"))
						&& (award.getBeneficiaryName() == null)) {
			validationBeneficiaryIdResultList.add(new SingleAwardValidationResult("beneficiaryName",
					"Enter the Beneficiary Name."));
		}
		
		log.info(
				"Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);

		return validationBeneficiaryIdResultList;
	}

	/*
	 *
	 * the below method validate the beneficiary name length (>255 chars)
	 */
	private List<SingleAwardValidationResult> validateBeneficiaryName(SingleAward award) {

				
		List<SingleAwardValidationResult> validationBeneficiaryIdResultList = new ArrayList<>();
		
		if(award.getBeneficiaryName()==null || StringUtils.isEmpty(award.getBeneficiaryName())) {
			
			validationBeneficiaryIdResultList.add(new SingleAwardValidationResult("beneficiaryName",
					"Beneficiary name field is Mandatory."));
		}
		
		if(award.getBeneficiaryName()!=null && award.getBeneficiaryName().length() > 255) {
			validationBeneficiaryIdResultList.add(new SingleAwardValidationResult("beneficiaryName",
					"Beneficiary name is too long, it should be 255 characters or fewer."));
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
			validationGrantingAuthorityResultList.add(new SingleAwardValidationResult("grantingAuthorityName",
					"Granting Authority name is Mandatory."));
		}
		
		if(award.getGrantingAuthorityName()!=null && award.getGrantingAuthorityName().length() > 255){
			validationGrantingAuthorityResultList.add(new SingleAwardValidationResult("grantingAuthorityName",
					"Granting authority name should be 255 characters or fewer."));
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
		if(!StringUtils.isEmpty(award.getSubsidyControlTitle()) &&
			award.getSubsidyControlTitle().length() > 255) {
			validationSubsidyMeasureNameResultList.add(new SingleAwardValidationResult("SubsidyMeasureTitle ",
					"Subsidy Measure Title must be 255 characters or fewer"));
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
		
		List<SingleAwardValidationResult> validationScNumLengthResults = new ArrayList<>();
		if(award.getSubsidyControlNumber()!=null && award.getSubsidyControlNumber().length() > 7){
			validationScNumLengthResults.add(new SingleAwardValidationResult("subsidyControlNumber",
					"Subsidy Control Number must be 7 characters or fewer."));
		}

		log.info("Validation Result Error list - Beneficiary Name missing error = {}", validationScNumLengthResults.size());

		return validationScNumLengthResults;
	}

	/*
	 *
	 * the below method validate the AuthorityName name exist in data base or not
	 * (table=Granting_Authority)
	 */
	private List<SingleAwardValidationResult> validateGrantingAuthorityNameInDb(SingleAward award) {

		log.info("{} ::Calling processServiceproxy.getAllGrantingAuthorities()... - start", loggingComponentName);
		
		List<GrantingAuthority> grantingAuthorityList = awardService.getAllGrantingAuthorities();
		
		log.info("{} ::Calling processServiceproxy.getAllSubsidyMeasures()... - end",loggingComponentName);

		List<String> grantingAuthorityNamesList = grantingAuthorityList.stream()
				.map(grantingAuthority -> grantingAuthority.getGrantingAuthorityName()).collect(Collectors.toList());


		List<SingleAwardValidationResult> validationGrantingAuthorityNameResultList = new ArrayList<>();
		
		if(award.getGrantingAuthorityName() != null
						&& !grantingAuthorityNamesList.contains(award.getGrantingAuthorityName())){
			validationGrantingAuthorityNameResultList.add(new SingleAwardValidationResult("grantingAuthorityName",
					"Please enter a valid Granting Authority."));
			
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
			validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId",
					"National ID is Mandatory."));
			log.info("validation fail National ID  is Mandatory.");
		}
		
		
		if(!StringUtils.isEmpty(award.getNationalId())&& award.getNationalId()!=null &&
				award.getNationalId().length() > 10) {
			validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId",
					"National ID must be 10 characters or fewer."));
			log.info("validation fail National ID  must be 10 characters or fewer");
		} else {
		 
			if(!StringUtils.isEmpty(award.getNationalId())&& award.getNationalIdType()!=null &&
					award.getNationalIdType().equalsIgnoreCase("VAT Number") &&
					(award.getNationalId().length() !=9 || !award.getNationalId().matches("[0-9]+"))){
				validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId",
						"invalid VAT number."));
				log.info("invalid VAT number.");
			}
			if(!StringUtils.isEmpty(award.getNationalIdType())&& award.getNationalIdType()!=null &&
					award.getNationalIdType().equalsIgnoreCase("UTR Number") &&
					(!award.getNationalId().matches("[0-9]+")|| award.getNationalId().length()!=10 )) {
				validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId",
						"invalid UTR Number."));
				log.info("invalid UTR number.");
			}

			if(!StringUtils.isEmpty(award.getNationalIdType())&& award.getNationalIdType()!=null &&
					award.getNationalIdType().equalsIgnoreCase("Charity Number") &&
					((!StringUtils.isEmpty(award.getNationalId())&& award.getNationalId()!=null) &&
							(award.getNationalId().length() > 8 || !award.getNationalId().matches("[0-9]+")))){
				validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId",
						"invalid Charity number."));
				log.info("invalid Charity number.");
			}

			if(!StringUtils.isEmpty(award.getNationalIdType())&& award.getNationalIdType()!=null &&
					award.getNationalIdType().equalsIgnoreCase("Company Registration Number") &&
					((!StringUtils.isEmpty(award.getNationalId())&& award.getNationalId()!=null) &&
							(!validateCompanyNumber(award.getNationalId())))){
				validationNationalIdResultList.add(new SingleAwardValidationResult("nationalId",
						"invalid Company Registration Number."));
				log.info("invalid Charity number.");
			}
		}

		log.info("{} :: Validation Result Error list - National ID  = ", loggingComponentName);

		return validationNationalIdResultList;

	}

	/**
	 * verifying the scNumber status and if is inActive adding that in the validation list
	 * @param subsidyMeasures
	 * @param scNumberReq
	 * @return
	 */
	private boolean isScNumberStatusActive(List<SubsidyMeasure> subsidyMeasures, String scNumberReq) {
		boolean isValid= false;

		for (SubsidyMeasure subsidyMeasure : subsidyMeasures) {
			if(subsidyMeasure.getScNumber().equals(scNumberReq) &&
					"Inactive".equals(subsidyMeasure.getStatus())) {
				isValid = true;
				break;
			}
		}
		return isValid;
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

	public SubsidyMeasure getSubsidyMeasureByScNumberOrMeasureTitle(SingleAward singleAward) {

		SubsidyMeasure subsidyMeasure = null;
		List<SubsidyMeasure> subsidyMeasures = null;
		if (!StringUtils.isEmpty(singleAward.getSubsidyControlNumber())
		 && singleAward.getSubsidyControlNumber().length() <= 7) {

			subsidyMeasure = smRepository.findByScNumber(singleAward.getSubsidyControlNumber());

		} else if (!StringUtils.isEmpty(singleAward.getSubsidyControlTitle())) {

			subsidyMeasures = smRepository.findBySubsidyMeasureTitle(singleAward.getSubsidyControlTitle());

			subsidyMeasure = subsidyMeasures.stream().filter( subsidyMeasureObj -> subsidyMeasureObj.getStatus().equals("Active"))
					.findFirst().get();
		}

		return subsidyMeasure;
	}


	@Value("${graphApiUrl}")
	private String graphApiUrl;

	/**
	 * Get the group info
	 * @param token
	 * @param groupId
	 * @return
	 */
	public UserDetailsResponse getUserRolesByGrpId(String token, String groupId) {
		// Graph API call.
		UserDetailsResponse userDetailsResponse = null;
		Response response = null;
		Object clazz;
		try {
			log.info("{}::before calling to getUsersByGroupId  Graph Api",loggingComponentName);
			response = graphAPIFeignClient.getUsersByGroupId("Bearer " + token,groupId);
			log.info("{}::After calling to getUsersByGroupId  Graph Api", loggingComponentName);

			if (response.status() == 200) {
				clazz = UserDetailsResponse.class;
				ResponseEntity<Object> responseResponseEntity =  toResponseEntity(response, clazz);
				userDetailsResponse
						= (UserDetailsResponse) responseResponseEntity.getBody();
				if (Objects.nonNull(userDetailsResponse)) {
					mapGroupInfoToUser(token,userDetailsResponse.getUserProfiles());
				}

			}  else {
				log.error("{} ::get user details by groupId Graph Api is failed ::{}",loggingComponentName,response.status());

			}

		} catch (FeignException ex) {
			log.error("{}:: get  groupId Graph Api is failed:: status code {} & message {}",
					loggingComponentName, ex.status(), ex.getMessage());
		}
		return userDetailsResponse;
	}

	public  void mapGroupInfoToUser(String token, List<UserResponse> userProfiles) {


		log.info("{}::before calling toGraph Api in the mapGroupInfoToUser",loggingComponentName);
		userProfiles.forEach(userProfile -> {
			UserRolesResponse userRolesResponse = getUserGroup(token,userProfile.getId());
			log.info("{}::in the mapGroupInfoToUser & userRolesResponse{} ::",loggingComponentName, userRolesResponse.getUserRoles());
			if (Objects.nonNull(userRolesResponse) && !CollectionUtils.isEmpty(userRolesResponse.getUserRoles())) {

				String roleName = userRolesResponse.getUserRoles().stream().filter(
						userRole -> userRole.getPrincipalType().equalsIgnoreCase("GROUP"))
						.map(UserRoleResponse::getPrincipalDisplayName).findFirst().get();

				log.info("{}::in the mapGroupInfoToUser {} ::",loggingComponentName, roleName);
				if(!StringUtils.isEmpty(roleName)) {

					userProfile.setRoleName(roleName);
				}

			}
		});
		log.info("{}::After calling toGraph Api in the mapGroupInfoToUser",loggingComponentName);
	}

	/**
	 * This method is used to get the user group based on the userId
	 * @param token
	 * @param userId
	 * @return
	 */
	public UserRolesResponse getUserGroup(String token, String userId) {
		// Graph API call.
		UserRolesResponse userRolesResponse = null;
		Response response = null;
		Object clazz;
		String groupName = null;
		try {
			log.info("{}::Before calling to Graph Api getUserGroup and user id is {}",loggingComponentName, userId);
			response = graphAPIFeignClient.getUserGroupName("Bearer " + token,userId);
			log.info("{}:: After the call Graph Api getUserGroup and  status is {}", loggingComponentName,response.status());

			if (response.status() == 200) {
				clazz = UserRolesResponse.class;
				ResponseEntity<Object> responseResponseEntity =  toResponseEntity(response, clazz);
				userRolesResponse
						= (UserRolesResponse) responseResponseEntity.getBody();
			}

		} catch (FeignException ex) {
			log.error("{}:: get  groupId Graph Api is failed:: status code {} & message {}",
					loggingComponentName, ex.status(), ex.getMessage());
		}
		return userRolesResponse;
	}


}
