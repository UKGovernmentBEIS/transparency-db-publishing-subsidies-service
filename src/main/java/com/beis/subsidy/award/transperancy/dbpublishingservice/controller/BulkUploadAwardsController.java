package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.MultipartConfigElement;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.UserPrinciple;
import com.beis.subsidy.award.transperancy.dbpublishingservice.repository.AuditLogsRepository;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.BulkUploadMfaAwardsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartResolver;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.BulkUploadAwardsService;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class BulkUploadAwardsController {

	@Value("${loggingComponentName}")
	private String loggingComponentName;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	AuditLogsRepository auditLogsRepository;

	@Autowired
	public BulkUploadAwardsService bulkUploadAwardsService;

	@Autowired
	public BulkUploadMfaAwardsService bulkUploadAwardsMfaService;

	public static final String All_ROLES[]= {"BEIS Administrator","Granting Authority Administrator",
			"Granting Authority Approver","Granting Authority Encoder"};


	@GetMapping("/health")
	public ResponseEntity<String> getHealth() {
		return new ResponseEntity<>("Successful health check - DB publishing Subsidies Service API", HttpStatus.OK);
	}
	
	@Bean
	public MultipartConfigElement multipartConfigElement() {
	   return new MultipartConfigElement("");
	}
	@Bean
	public MultipartResolver multipartResolver() {
	   org.springframework.web.multipart.commons.CommonsMultipartResolver multipartResolver = new org.springframework.web.multipart.commons.CommonsMultipartResolver();
	   multipartResolver.setMaxUploadSize(10000000);
	   return multipartResolver;
	}
	
	@PostMapping(value = "/uploadBulkAwards", consumes = { "multipart/form-data" })
	public ResponseEntity<ValidationResult> uploadAwardsFile(@RequestParam("file") MultipartFile file,
															 @RequestHeader("userPrinciple") HttpHeaders userPrinciple){
		UserPrinciple userPrincipleObj = null;
		//1.0 - Check uploaded file format to be xlsx
		if(ExcelHelper.hasExcelFormat(file)) {
			
		   try {
				log.info("{} :: Before calling validateFile", loggingComponentName);
			   String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
			   userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
			   if (!Arrays.asList(All_ROLES).contains(userPrincipleObj.getRole())) {
				   ValidationResult validationResult = new ValidationResult();
				   validationResult.setMessage("You are not authorised to bulk upload awards");
				   return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
			   }
				ValidationResult validationResult = bulkUploadAwardsService.validateFile(file,
						userPrincipleObj.getRole(), userPrincipleObj);
			   if (validationResult.getErrorRows() == 0) {
				   log.info("Zero errors found, Awards successfully bulk uploaded");
			   }
				return ResponseEntity.status(HttpStatus.OK).body(validationResult);
			
			} catch (Exception e) {

			   log.error("{} :: Exception block in BulkUploadAwardsController", loggingComponentName,e);
		   		//2.0 - CatchException and return validation errors
				ValidationResult validationResult = new ValidationResult();
			    validationResult.setMessage("Bulk upload failed");
			    ValidationErrorResult errorResult = new ValidationErrorResult();
			    errorResult.setErrorMessages(e.getMessage());
			    List<ValidationErrorResult> validationErrorResult = new ArrayList<>();
			    validationErrorResult.add(errorResult);
			    validationResult.setValidationErrorResult(validationErrorResult);
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);	
			}
		} else {
		
			//3.0 - Wrong format file
			ValidationResult validationResult = new ValidationResult();	
			
			ValidationErrorResult validationErrorResult = new ValidationErrorResult();
			validationErrorResult.setRow("All");
			validationErrorResult.setColumns("All");
			validationErrorResult.setErrorMessages("Upload an excel file (in format xlsx) !");
			
			validationResult.setTotalRows(0);
			validationResult.setErrorRows(0);
			validationResult.setValidationErrorResult(Arrays.asList(validationErrorResult));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
		}
	}

	@PostMapping(value = "/uploadBulkMfa", consumes = { "multipart/form-data" })
	public ResponseEntity<ValidationResult> uploadMfaFile(@RequestParam("file") MultipartFile file,
															 @RequestHeader("userPrinciple") HttpHeaders userPrinciple){
		UserPrinciple userPrincipleObj = null;
		//1.0 - Check uploaded file format to be xlsx
		if(ExcelHelper.hasExcelFormat(file)) {

			try {
				log.info("{} :: Before calling validateFile", loggingComponentName);
				String userPrincipleStr = userPrinciple.get("userPrinciple").get(0);
				userPrincipleObj = objectMapper.readValue(userPrincipleStr, UserPrinciple.class);
				if (!Arrays.asList(All_ROLES).contains(userPrincipleObj.getRole())) {
					ValidationResult validationResult = new ValidationResult();
					validationResult.setMessage("You are not authorised to bulk upload awards");
					return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
				}
				ValidationResult validationResult = bulkUploadAwardsMfaService.validateFile(file,
						userPrincipleObj.getRole(), userPrincipleObj);
				if (validationResult.getErrorRows() == 0) {
					log.info("Zero errors found, MFA Awards successfully bulk uploaded");
				}
				return ResponseEntity.status(HttpStatus.OK).body(validationResult);

			} catch (Exception e) {

				log.error("{} :: Exception block in BulkUploadAwardsController", loggingComponentName,e);
				//2.0 - CatchException and return validation errors
				ValidationResult validationResult = new ValidationResult();
				validationResult.setMessage("Bulk upload failed");
				ValidationErrorResult errorResult = new ValidationErrorResult();
				errorResult.setErrorMessages(e.getMessage());
				List<ValidationErrorResult> validationErrorResult = new ArrayList<>();
				validationErrorResult.add(errorResult);
				validationResult.setValidationErrorResult(validationErrorResult);
				return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(validationResult);
			}
		} else {

			//3.0 - Wrong format file
			ValidationResult validationResult = new ValidationResult();

			ValidationErrorResult validationErrorResult = new ValidationErrorResult();
			validationErrorResult.setRow("All");
			validationErrorResult.setColumns("All");
			validationErrorResult.setErrorMessages("Upload an excel file (in format xlsx) !");

			validationResult.setTotalRows(0);
			validationResult.setErrorRows(0);
			validationResult.setValidationErrorResult(Arrays.asList(validationErrorResult));
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(validationResult);
		}
	}
	
}
