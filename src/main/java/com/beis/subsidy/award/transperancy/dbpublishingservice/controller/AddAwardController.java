package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import java.util.Arrays;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.SingleAwardValidationResult;
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
	public ResponseEntity<SingleAwardValidationResult> addSubsidyAward(@Valid @RequestBody SingleAward awardInputRequest) {

		try {
			log.info("Beofre calling validateFile::::");
			// TODO - check if we can result list of errors here it self
			SingleAwardValidationResult validationResult = addAwardService.validateAward(awardInputRequest);

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			// 2.0 - CatchException and return validation errors
			SingleAwardValidationResult validationResult = new SingleAwardValidationResult();

			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}

	}

}
