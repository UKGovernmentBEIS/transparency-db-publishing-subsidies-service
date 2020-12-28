package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import java.util.Arrays;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.AddAwardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AddAwardController {

	@Autowired
	public AddAwardService addAwardService;
	
	/*@GetMapping("/health")
	public ResponseEntity<String> getHealth() {
		return new ResponseEntity<>("Successful health check - AddAward Service API", HttpStatus.OK);
	}*/
	
	/**
	 * To get Award as input from UI and return Validation results based on input.
	 * 
	 * @param searchInput - Input as SearchInput object from front end 
	 * @return ResponseEntity - Return response status and description
	 */
	@PostMapping("addAward")
	public ResponseEntity<ValidationResult> addSubsidyAward(@Valid @RequestBody SingleAward awardInputRequest ){
		
				
			try {
				log.info("Beofre calling validateFile::::");
				//TODO - check if we can result list of errors here it self
				ValidationResult validationResult = addAwardService.validateAward(awardInputRequest);
			
			/*ValidationErrorResult validationErrorResult = new ValidationErrorResult();
			validationErrorResult.setRow("All");
			validationErrorResult.setColumns("All");
			validationErrorResult.setErrorMessages("Upload an excel file (in format xlsx) !");
			
			validationResult.setTotalRows(0);
			validationResult.setErrorRows(0);*/
			//validationResult.setValidationErrorResult(Arrays.asList(validationErrorResult));
			
			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
			} catch (Exception e) {
				
				//2.0 - CatchException and return validation errors 
				ValidationResult validationResult = new ValidationResult();
				
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);	
			}
		
	}
	
}
