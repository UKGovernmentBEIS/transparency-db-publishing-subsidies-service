package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.SingleAwardValidationResults;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.AddAwardService;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.AwardService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class AddAwardController {

	@Autowired
	public AddAwardService addAwardService;

	@Autowired
	public AwardService awardService;
	@Autowired
	Environment environment;

	/*
	 * @GetMapping("/health") public ResponseEntity<String> getHealth() { return new
	 * ResponseEntity<>("Successful health check - AddAward Service API",
	 * HttpStatus.OK); }
	 */

	/**
	 * To get Award as input from UI and return Validation results based on input.
	 * 
	 * @param searchInput
	 *            - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PostMapping("addAward")
	public ResponseEntity<SingleAwardValidationResults> addSubsidyAward(
			@Valid @RequestBody SingleAward awardInputRequest) {

		try {
			log.info("environment ***** " + environment.getProperty("keyvault_connection"));
			log.info("Beofre calling add Award::::");
			// TODO - check if we can result list of errors here it self
			if (awardInputRequest == null) {
				throw new Exception("awardInputRequest is empty");
			}
			SingleAwardValidationResults validationResult = addAwardService.validateAward(awardInputRequest);

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			SingleAwardValidationResults validationResult = new SingleAwardValidationResults();

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}

	}

	/**
	 * get the Award as input from UI and update the same in DBand return Validation
	 * results based on input.
	 * 
	 * @param searchInput
	 *            - Input as SearchInput object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PutMapping("award")
	public ResponseEntity<SingleAwardValidationResults> updateSubsidyAward(
			@Valid @RequestBody SingleAward awardInputRequest) {

		try {
			log.info("Beofre calling update award::::");
			// TODO - check if we can result list of errors here it self
			if (awardInputRequest == null) {
				throw new Exception("awardInputRequest is empty");
			}
			SingleAwardValidationResults validationResult = new SingleAwardValidationResults();

			Award updatedAward = awardService.updateAward(awardInputRequest);

			validationResult.setMessage(updatedAward.getAwardNumber() + " updated successfully");

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			SingleAwardValidationResults validationResult = new SingleAwardValidationResults();

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}

	}

}
