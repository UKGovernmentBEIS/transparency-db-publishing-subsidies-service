package com.beis.subsidy.award.transperancy.dbpublishingservice.controller;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationErrorResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.controller.response.ValidationResult;
import com.beis.subsidy.award.transperancy.dbpublishingservice.service.BulkUploadAwardsService;
import com.beis.subsidy.award.transperancy.dbpublishingservice.util.ExcelHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class BulkUploadAwardsController {

	@Autowired
	public BulkUploadAwardsService bulkUploadAwardsService;
	
	@GetMapping("/health")
	public ResponseEntity<String> getHealth() {
		return new ResponseEntity<>("Successful health check - DB publishing Subsidies Service API", HttpStatus.OK);
	}
	
	
	@PostMapping(value = "/uploadBulkAwards", consumes = { "multipart/form-data" })
	
	public ResponseEntity<ValidationResult> uploadAwardsFile(@RequestParam("file") MultipartFile file ){
		
		//1.0 - Check uploaded file format to be xlsx
		if(ExcelHelper.hasExcelFormat(file)) {
			
		
			try {
				log.info("Beofre calling validateFile::::");
				ValidationResult validationResult = bulkUploadAwardsService.validateFile(file);
				return ResponseEntity.status(HttpStatus.OK).body(validationResult);
			
			} catch (Exception e) {
				
				//2.0 - CatchException and return validation errors 
				ValidationResult validationResult = new ValidationResult();
				
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
