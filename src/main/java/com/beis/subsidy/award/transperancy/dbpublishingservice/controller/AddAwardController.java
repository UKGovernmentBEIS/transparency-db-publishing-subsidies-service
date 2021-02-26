package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import javax.validation.Valid;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.UserPrinciple;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.SingleAwardValidationResults;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.Award;
import com.beis.subsidy.award.transperancy.dbpublishingservice.model.SingleAward;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.AddAwardService;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.AwardService;
import com.beis.subsidy.control.accessmanagementservice.controller.feign.GraphAPILoginFeignClient;
import com.beis.subsidy.control.accessmanagementservice.exception.AccessTokenException;
import com.beis.subsidy.control.accessmanagementservice.response.AccessTokenResponse;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@RestController
public class AddAwardController {

	@Autowired
	public AddAwardService addAwardService;

	@Autowired
	public AwardService awardService;

	@Value("${loggingComponentName}")
	private String loggingComponentName;

	@Autowired
	private ObjectMapper objectMapper;

	public static final String All_ROLES[]= {"BEIS Administrator","Granting Authority Administrator",
			"Granting Authority Approver","Granting Authority Encoder"};
	
	@Autowired
    GraphAPILoginFeignClient graphAPILoginFeignClient;

    static final String BEARER = "Bearer ";

  
    @Autowired
    Environment environment;
	
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
		UserPrinciple userPrincipleObj = null;
		try {
			log.info("{} :: Before calling add Award",loggingComponentName);
			String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
			userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
			if (!Arrays.asList(All_ROLES).contains(userPrincipleObj.getRole())) {
				SingleAwardValidationResults validationResult = new SingleAwardValidationResults();
				validationResult.setMessage("You are not authorised to add single award");
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
			}
			if (awardInputRequest == null) {
				throw new Exception("awardInputRequest is empty");
			}
			 String accessToken= getBearerToken();
			SingleAwardValidationResults validationResult = addAwardService.validateAward(awardInputRequest, userPrincipleObj.getRole(),accessToken,String.valueOf(userPrincipleObj.getGrantingAuthorityGroupId()),userPrincipleObj.getUserName());
			

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
	 * @param awardInputRequest
	 *            - Input as SingleAward object from front end
	 * @return ResponseEntity - Return response status and description
	 */
	@PutMapping("award")
	public ResponseEntity<SingleAwardValidationResults> updateSubsidyAward(
			@Valid @RequestBody SingleAward awardInputRequest) {

		try {
			log.info("{}::Before calling update award",loggingComponentName);

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
	
	 public String getBearerToken() throws AccessTokenException {

	        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
	        map.add("grant_type", "client_credentials");
	        map.add("client_id", environment.getProperty("client-Id"));
	        map.add("client_secret",environment.getProperty("client-secret"));
	        map.add("scope", environment.getProperty("graph-api-scope"));

	        AccessTokenResponse openIdTokenResponse = graphAPILoginFeignClient
	                .getAccessIdToken(environment.getProperty("tenant-id"),map);
	        
	        
	        if (openIdTokenResponse == null) {
	            throw new AccessTokenException(HttpStatus.valueOf(500),
	                    "Graph Api Service Failed while bearer token generate");
	        }
	        return openIdTokenResponse.getAccessToken();
	    }

}
