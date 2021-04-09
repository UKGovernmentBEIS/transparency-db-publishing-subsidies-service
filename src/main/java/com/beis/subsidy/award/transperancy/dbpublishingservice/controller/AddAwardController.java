package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import javax.validation.Valid;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.feign.GraphAPILoginFeignClient;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.*;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.AddAwardService;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.AwardService;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@RestController
public class AddAwardController {

	@Autowired
	public AddAwardService addAwardService;

	@Autowired
	public AwardService awardService;

	@Autowired
	AuditLogsRepository auditLogsRepository;

	@Value("${loggingComponentName}")
	private String loggingComponentName;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	GraphAPILoginFeignClient graphAPILoginFeignClient;

	@Autowired
	Environment environment;

	public static final String[] All_ROLES = {"BEIS Administrator","Granting Authority Administrator",
			"Granting Authority Approver","Granting Authority Encoder"};
	
	/**
	 * To get Award as input from UI and return Validation results based on input.
	 * 
	 * @param awardInputRequest
	 *            - Input as SingleAward object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PostMapping("addAward")
	public ResponseEntity<SingleAwardValidationResults> addSubsidyAward(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
			@Valid @RequestBody SingleAward awardInputRequest) {
		UserPrinciple userPrincipleObj;
		try {
			log.info("{} :: Before calling add Award",loggingComponentName);
			SingleAwardValidationResults validationResult = new SingleAwardValidationResults();
			String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
			userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
			if (!Arrays.asList(All_ROLES).contains(userPrincipleObj.getRole())) {
				log.error("{} :: role validation  failed in  add Award",loggingComponentName);
				validationResult.setTotalErrors(validationResult.getTotalErrors() + 1);
				validationResult.setMessage("You are not authorised to add single award");
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(validationResult);
			} else if (awardInputRequest == null) {
				log.error("{} :: awardInputRequest is null in  add Award",loggingComponentName);
				validationResult.setTotalErrors(validationResult.getTotalErrors() + 1);
				validationResult.setMessage("awardInputRequest is empty");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
			}

			String accessToken= getBearerToken();
			if (StringUtils.isEmpty(accessToken)) {
				log.error("{} :: bearer token problem in  add Award",loggingComponentName);
				validationResult.setTotalErrors(validationResult.getTotalErrors() + 1);
				validationResult.setMessage("Graph Api Service Failed while bearer token generate");
				return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
			}
			validationResult = addAwardService.validateAward(awardInputRequest, userPrincipleObj,accessToken);

			if ( validationResult.getTotalErrors() == 0) {

				ExcelHelper.saveAuditLog(userPrincipleObj, "Add Award", userPrincipleObj.getRole(), auditLogsRepository);
				log.info("{} :: after saving the audit log in  add Award",loggingComponentName);
			}

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {

			log.error("{} :: Exception block in addSubsidyAward", loggingComponentName,e);
			SingleAwardValidationResults singleAwardValidationResults = new SingleAwardValidationResults();
			//2.0 - CatchException and return validation errors
			List<SingleAwardValidationResult> validationErrorResult = new ArrayList<>();
			SingleAwardValidationResult validationResult = new SingleAwardValidationResult();
			validationResult.setMessage(e.getMessage());
			validationErrorResult.add(validationResult);
			singleAwardValidationResults.setValidationErrorResult(validationErrorResult);
			singleAwardValidationResults.setTotalErrors(validationErrorResult.size());
			singleAwardValidationResults.setMessage("failed");
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(singleAwardValidationResults);
		}

	}

	/**
	 * get the Award as input from UI and update the same in DBand return Validation
	 * results based on input.
	 * 
	 * @param awardInputRequest
	 *            - Input as SingleAward object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PutMapping("award/{awardNumber}")
	public ResponseEntity<SingleAwardValidationResults> updateSubsidyAward(@RequestHeader("userPrinciple") HttpHeaders userPrinciple,
			@Valid @RequestBody SingleAward awardInputRequest,
			 @PathVariable("awardNumber") Long awardNumber) {

		try {
			log.info("{}::Before calling update award",loggingComponentName);

			if (awardInputRequest == null || awardNumber == 0) {
				throw new Exception("awardInputRequest is empty");
			}
			SingleAwardValidationResults validationResult = new SingleAwardValidationResults();
			String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
			UserPrinciple userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
			Award updatedAward = awardService.updateAward(awardNumber,awardInputRequest, userPrincipleObj.getRole());
			validationResult.setMessage(updatedAward.getAwardNumber() + " updated successfully");
			if(!StringUtils.isEmpty(updatedAward.getAwardNumber())) {

				//Audit entry
				StringBuilder eventMsg = new StringBuilder("Award ").append(updatedAward.getAwardNumber())
						.append("is Published");
				ExcelHelper.saveAuditLogForUpdate(userPrincipleObj, "Update Award", updatedAward.getAwardNumber().toString()
						,eventMsg.toString(),auditLogsRepository);
			}

			return ResponseEntity.status(HttpStatus.OK).body(validationResult);
		} catch (Exception e) {
			// 2.0 - CatchException and return validation errors
			log.error("{} :: Exception block in updateSubsidyAward", loggingComponentName,e);
			SingleAwardValidationResults validationResult = new SingleAwardValidationResults();
			return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
		}
	}

	public String getBearerToken() {

		MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
		map.add("grant_type", "client_credentials");
		map.add("client_id", environment.getProperty("client-Id"));
		map.add("client_secret",environment.getProperty("client-secret"));
		map.add("scope", environment.getProperty("graph-api-scope"));

		AccessTokenResponse openIdTokenResponse = graphAPILoginFeignClient
				.getAccessIdToken(environment.getProperty("tenant-id"),map);

	    return  openIdTokenResponse != null ? openIdTokenResponse.getAccessToken() : null ;
	}
}
