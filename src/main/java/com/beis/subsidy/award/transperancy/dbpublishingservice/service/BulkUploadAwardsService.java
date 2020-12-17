package com.beis.subsidy.award.transperancy.dbpublishingservice.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.BulkUploadAwards;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.GrantingAuthority;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SubsidyMeasure;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;

@Service
public class BulkUploadAwardsService {
	
	@Autowired
	private AwardService awardService;

	public ValidationResult validateFile(MultipartFile file) {
		
		try {
				
			//Read Excel file 
			List<BulkUploadAwards> bulkUploadAwards = ExcelHelper.excelToAwards(file.getInputStream());
			
			System.out.println("Back from Excel to awards...printing list of awards - start");
			bulkUploadAwards.stream().forEach(System.out::println);
			System.out.println("Back from Excel to awards...printed list of awards - end");
			
			//Validation  National Id length check
			List<ValidationErrorResult> nationalIdMisingList = validateNationaIdAwards(bulkUploadAwards);

			//Validation - Beneficiary check
			List<ValidationErrorResult> beneficiaryMisingList = validateBeneficiaryAwards(bulkUploadAwards);
			
			//TODO - Validation  - Make call to process API to get data for subsidy measure
			/* 3) If incorrect SC number is entered, user system should throw an error				
			 * 			Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> scNumberNameCheckList = validateScNumberScTitle(bulkUploadAwards);
			
			List<ValidationErrorResult> subsidyControlNumberLengthList = validateSubsidyNumberLength(bulkUploadAwards);
			
			List<ValidationErrorResult> subsidyControlNumberMismatchList = validateSubsidyControlNumber(bulkUploadAwards);
			
			List<ValidationErrorResult> subsidyMeasureTitleNameLengthList = validateSubsidyMeasureNameLength(bulkUploadAwards);
			
			
			/* validate benificiary name length > 255				
			 * 			Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			
			List<ValidationErrorResult> beneficiaryNameErrorList = validateBeneficiaryName(bulkUploadAwards);

			/*  validate Granting Authority  name length > 255				
			 * 			Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> grantingAuthorityNameErrorList = validateGrantingAuthorityName(bulkUploadAwards);
			

			/* validate Granting Authority  exists in database or not				
			 * 			Validation Error - Row 6 - Incorrect SC Number - Correct one SC10002
			 */
			List<ValidationErrorResult> grantingAuthorityErrorList = validateGrantingAuthorityNameinDb(bulkUploadAwards);
			
			//Merge lists of Validation Errors
			List<ValidationErrorResult> validationErrorResultList = Stream.of(scNumberNameCheckList,subsidyMeasureTitleNameLengthList,nationalIdMisingList, beneficiaryMisingList, subsidyControlNumberLengthList,subsidyControlNumberMismatchList,grantingAuthorityNameErrorList,grantingAuthorityErrorList)
						.flatMap(x -> x.stream())
						.collect(Collectors.toList());
			
			
			System.out.println("Final bulk awards list ...printing list of awards - start");
			bulkUploadAwards.stream().forEach(System.out::println);
			System.out.println("Final bulk awards list ...printed list of awards - end");
			
			System.out.println("Final validation errors list ...printing list of errors - start");
			validationErrorResultList.stream().forEach(System.out::println);
			System.out.println("Final validation errors list ...printing list of errors - end");
			
			ValidationResult validationResult = new ValidationResult();
			validationResult.setValidationErrorResult(validationErrorResultList);
			validationResult.setTotalRows(bulkUploadAwards.size());
			validationResult.setErrorRows(validationErrorResultList.size());
			validationResult.setMessage( (validationErrorResultList.size() > 0) ? "Validation Errors in Uploaded file" : "No errors in Uploaded file" ); 

			System.out.println("Final validation Result object ...printing validationResult - start");
			validationErrorResultList.stream().forEach(System.out::println);
			System.out.println("Final validation Result object ...printing validationResult - start");

			
			//TODO - if no, errors then call process API
			if(validationResult.getValidationErrorResult().size() == 0) {
				
				System.out.println("No validation error in bulk excel template");
				//ResponseEntity<Object> response = awardService.createBulkTemplateAwards(bulkUploadAwards);
				//awardService.saveAwards(bulkUploadAwards);
				awardService.processBulkAwards(bulkUploadAwards);
				
				System.out.println("After calling process api - response = " );
				//validationResult.setMessage( (response.getStatusCode().equals(HttpStatus.CREATED)) ? "All Awards saved in Database" : "Error while saving awards in Database" );
				validationResult.setMessage( (true ? "All Awards saved in Database" : "Error while saving awards in Database" ));
			}
			
			return validationResult;
			
			
		} catch(IOException e) {
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
				.filter( award -> 
					( 
							(award.getSubsidyControlNumber() == null 
							&& award.getSubsidyControlTitle() == null  )
					) )
				.collect(Collectors.toList());
		
		List<ValidationErrorResult> validationScNumberScTitlResultList = new ArrayList<>();
		validationScNumberScTitlResultList = ScNumberScTitleErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E", "Either Subsidy Control number or Subsidy title should enter."))
				.collect(Collectors.toList());
		
		System.out.println("Validation Result Error list - Either Subsidy Control number or Subsidy title should enter = " + validationScNumberScTitlResultList);
		
		return validationScNumberScTitlResultList;
	}
	
	private List<ValidationErrorResult> validateSubsidyControlNumber(List<BulkUploadAwards> bulkUploadAwards) {

		System.out.println("Calling awardService.getAllSubsidyMeasures()... - start");
		//ResponseEntity<List<SubsidyMeasure>>  smListResponse = awardService.getAllSubsidyMeasures();
		List<SubsidyMeasure> smList = awardService.getAllSubsidyMeasures();
		//List<SubsidyMeasure> smList = smListResponse.getBody();
		//System.out.println("smList = " +  smListResponse.getBody());
		//System.out.println("smList = " + smListResponse.getBody());
		System.out.println("Calling processServiceproxy.getAllSubsidyMeasures()... - end");
		
		List<String> subsidyControlNumberTitleList  = smList.stream().map( sm -> sm.getScNumber()).collect(Collectors.toList());
		
		System.out.println("subsidyControlNumberTitleList - String list " + subsidyControlNumberTitleList);
		
		List<BulkUploadAwards> subsidyControlNumberErrorRecordsList = bulkUploadAwards.stream()
				.filter( award -> 
							award.getSubsidyControlNumber() != null 
								&& award.getSubsidyControlTitle() != null
								&& !subsidyControlNumberTitleList.contains(award.getSubsidyControlNumber()))
				.collect(Collectors.toList());
		
		System.out.println("Back validation-3 - subsidy measure title mismatch check...printing list of awards with subsidy measure number error - start");
		subsidyControlNumberErrorRecordsList.stream().forEach(System.out::println);
		System.out.println("Back validation-3 - subsidy measure title mismatch check...printing list of awards with subsidy measure number error - end");
		
		List<ValidationErrorResult> validationSubsidyControlNumberResultList = new ArrayList<>();
		validationSubsidyControlNumberResultList = subsidyControlNumberErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "B", "Subsidy Control doest not exists."))
				.collect(Collectors.toList());
		
		System.out.println("Validation Result Error list - Subsidy Measure Number mismatch error = " + validationSubsidyControlNumberResultList);
		
		return validationSubsidyControlNumberResultList;
	}

	private List<ValidationErrorResult> validateBeneficiaryAwards(List<BulkUploadAwards> bulkUploadAwards) {
		
		/*
		 * 2) If the ‘National ID type’ is a UTR or a VAT number, then validate if the beneficiary name is entered and if not return an error as above. 
		 * 			Validation Error - Row 9 - Beneficiary missing
		 * 
		 */
		//TODO - Validation 2 - National ID Type UTR/ VAT, then check beneficiary present - implement filter method
		List<BulkUploadAwards> beneficiaryMissingErrorRecordsList = bulkUploadAwards.stream()
				.filter( award -> 
					( 
							(award.getNationalIdType().equals("UTR Number") || award.getNationalIdType().equals("VAT Number"))
							&&	( award.getBeneficiaryName().equals("") || award.getBeneficiaryName() == null  )
					) )
				.collect(Collectors.toList());
		
		List<ValidationErrorResult> validationBeneficiaryIdResultList = new ArrayList<>();
		validationBeneficiaryIdResultList = beneficiaryMissingErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E", "Enter the Beneficiary Name."))
				.collect(Collectors.toList());
		
		System.out.println("Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);
		
		return validationBeneficiaryIdResultList;
	}
/*
 *
 * the below method validate the benificiary name length (>255 chars)	
 */
private List<ValidationErrorResult> validateBeneficiaryName(List<BulkUploadAwards> bulkUploadAwards) {
		
		List<BulkUploadAwards> beneficiaryNameErrorRecordsList = bulkUploadAwards.stream()
				.filter( award -> 
					( 
							(award.getBeneficiaryName().length()> 255 )
					) )
				.collect(Collectors.toList());
		
		List<ValidationErrorResult> validationBeneficiaryIdResultList = new ArrayList<>();
		validationBeneficiaryIdResultList = beneficiaryNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E", "Beneficiary name is too long, it should be 255 characters or fewer"))
				.collect(Collectors.toList());
		
		System.out.println("Validation Result Error list - Beneficiary Name missing error = " + validationBeneficiaryIdResultList);
		
		return validationBeneficiaryIdResultList;
	}

/*
*
* the below method validate the AuthorityName name length (>255 chars)	
*/
private List<ValidationErrorResult> validateGrantingAuthorityName(List<BulkUploadAwards> bulkUploadAwards) {
		
		List<BulkUploadAwards> validateGrantingAuthorityNameErrorRecordsList = bulkUploadAwards.stream()
				.filter( award -> 
					( 
							(award.getBeneficiaryName().length()> 255 )
					) )
				.collect(Collectors.toList());
		
		List<ValidationErrorResult> validationGrantingAuthorityResultList = new ArrayList<>();
		validationGrantingAuthorityResultList = validateGrantingAuthorityNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E", "Please enter a valid Granting Authority"))
				.collect(Collectors.toList());
		
		System.out.println("Validation Result Error list - Beneficiary Name missing error = " + validationGrantingAuthorityResultList);
		
		return validationGrantingAuthorityResultList;
	}

/*
*
* the below method validate the subsidy Measure Title  length (>255 chars)	
*/
private List<ValidationErrorResult> validateSubsidyMeasureNameLength(List<BulkUploadAwards> bulkUploadAwards) {
		
		List<BulkUploadAwards> validateSubsidyMeasureNameErrorRecordsList = bulkUploadAwards.stream()
				.filter( award -> 
					( 
							(award.getSubsidyControlTitle().length()> 255 )
					) )
				.collect(Collectors.toList());
		
		List<ValidationErrorResult> validationSubsidyMeasureNameResultList = new ArrayList<>();
		validationSubsidyMeasureNameResultList = validateSubsidyMeasureNameErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E", "Subsidy Measure Title must be 255 characters or fewer "))
				.collect(Collectors.toList());
		
		System.out.println("Validation Result Error list - Subsidy Measure Title must be 255 characters or fewer = " + validationSubsidyMeasureNameResultList);
		
		return validationSubsidyMeasureNameResultList;
	}
/*
*
* the below method validate the Subsidy number length (>7 chars)	
*/
private List<ValidationErrorResult> validateSubsidyNumberLength(List<BulkUploadAwards> bulkUploadAwards) {
		
		List<BulkUploadAwards> validateSubsidyNumberLengthErrorRecordsList = bulkUploadAwards.stream()
				.filter( award -> 
					( 
							(award.getSubsidyControlNumber().length()> 7 )
					) )
				.collect(Collectors.toList());
		
		List<ValidationErrorResult> validationSubsidyNumberLengthResultList = new ArrayList<>();
		validationSubsidyNumberLengthResultList = validateSubsidyNumberLengthErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "E", "Subsidy Control Number must be 7 characters or fewer"))
				.collect(Collectors.toList());
		
		System.out.println("Validation Result Error list - Beneficiary Name missing error = " + validationSubsidyNumberLengthResultList);
		
		return validationSubsidyNumberLengthResultList;
	}

/*
*
* the below method validate the AuthorityName name exist in data base or not (table=Granting_Authority)	
*/
private List<ValidationErrorResult> validateGrantingAuthorityNameinDb(List<BulkUploadAwards> bulkUploadAwards) {
		
	System.out.println("Calling processServiceproxy.getAllGrantingAuthorities()... - start");
	//ResponseEntity<List<GrantingAuthority>>  grantingAuthorityListResponse = awardService.getAllGrantingAuthorities();
	List<GrantingAuthority> grantingAuthorityList= awardService.getAllGrantingAuthorities();
	//List<GrantingAuthority> grantingAuthorityList = grantingAuthorityListResponse.getBody();
	//System.out.println("smListResponse = " +  grantingAuthorityListResponse.getBody());
	System.out.println("smList = " +  grantingAuthorityList);
	System.out.println("Calling processServiceproxy.getAllSubsidyMeasures()... - end");
	
	List<String> grantingAuthorityNamesList  = grantingAuthorityList.stream().map( grantingAuthority -> grantingAuthority.getGrantingAuthorityName()).collect(Collectors.toList());
	
	System.out.println("Granting Authority - String list " + grantingAuthorityNamesList);
	
	List<BulkUploadAwards> grantingAuthorityNameErrorRecordsList = bulkUploadAwards.stream()
			.filter( award -> 
						award.getGrantingAuthorityName() != null 
							&& !grantingAuthorityNamesList.contains(award.getGrantingAuthorityName()))
			.collect(Collectors.toList());
	
	System.out.println("Back validation-6 - Granting Authority Name  measure check...with data base table  error - start");
	grantingAuthorityNameErrorRecordsList.stream().forEach(System.out::println);
	System.out.println("Back validation-6 - Granting Authority Name  measure check...with data base table  error - start - end");
	
	List<ValidationErrorResult> validationGrantingAuthorityNameResultList = new ArrayList<>();
	validationGrantingAuthorityNameResultList = grantingAuthorityNameErrorRecordsList.stream()
			.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "B", "Please enter a valid Granting Authority."))
			.collect(Collectors.toList());
	
	System.out.println("Validation Result Error list - Granting Authority Name error = " + validationGrantingAuthorityNameResultList);
	
	return validationGrantingAuthorityNameResultList;
	}

	private List<ValidationErrorResult> validateNationaIdAwards(List<BulkUploadAwards> bulkUploadAwards) {

		/*
		 * 1) National ID MUST consist of 8 numbers or 2 letters followed by 6 numbers e.g. 12345678, AB123456 				
		 * 			Validation Error - Row 5 - National ID less than 8 char
		 * 
		 * 2) If the ‘National ID type’ is a UTR or a VAT number, then validate if the beneficiary name is entered and if not return an error as above. 
		 * 			Validation Error - Row 9 - Beneficiary missing
		 * 
		 */
		
		//TODO - Validation 1 - National ID length check - implement filter method
		List<BulkUploadAwards> nationsIdErrorRecordsList = bulkUploadAwards.stream()
				.filter( award -> award.getNationalId().length() > 10 )
				.collect(Collectors.toList());
		
		System.out.println("Back validation-1 - national ID length check...printing list of awards with nation id length error - start");
		nationsIdErrorRecordsList.stream().forEach(System.out::println);
		System.out.println("Back validation-1 - national ID length check...printing list of awards with nation id length error - end");
		
		List<ValidationErrorResult> validationNationalIdResultList = new ArrayList<>();
		validationNationalIdResultList = nationsIdErrorRecordsList.stream()
				.map(award -> new ValidationErrorResult(String.valueOf(award.getRow()), "D", "National ID  must be 10 characters or fewer."))
				.collect(Collectors.toList());
		
		System.out.println("Validation Result Error list - National ID missing error = " + validationNationalIdResultList);
		
		return validationNationalIdResultList; 
		
	}

	public List<Award> getallAwards() {
		
		return awardService.getallAwards();
		
	}

}
